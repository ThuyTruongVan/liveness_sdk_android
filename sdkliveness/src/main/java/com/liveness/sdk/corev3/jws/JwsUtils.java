package com.liveness.sdk.corev3.jws;

import android.util.Base64;
import android.util.Log;

import com.liveness.sdk.corev3.utils.AppConfig;
import com.liveness.sdk.corev3.utils.AppUtils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class JwsUtils {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    private static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----";
    private static final String END_PUBLIC_KEY = "-----END PUBLIC KEY-----";
    private static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    private static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    private static JwsUtils jwsUtils;

    private static final String EID_PUB = "";
    private String appLicense = "";

    public static JwsUtils getInstance() {
        if (jwsUtils == null) {
            jwsUtils = new JwsUtils();
        }
        return jwsUtils;
    }

    public void setAppLicense(String license) {
        this.appLicense = String.valueOf(license);
    }

    public String encrypt(JSONObject value) {
        JWSObject jwsObject = null;
        JWSSigner signer = null;
        JWEObject jweObject = null;
        JWEHeader jweHeader = null;
        RSAPrivateKey keySignJWS = null;//sign prikey

        PublicKeyReader pubkeyEidReader_jws = new PublicKeyReader();
        PrivateKeyReader privatekeyEidReader_jwe = new PrivateKeyReader();


        String senderPayload = null;
        Payload senderJwePayload = null;
        String senderJws = null;
        String senderJwe = null;
        RSAEncrypter encrypter = null;

        try {

            senderPayload = value.toString();
            senderJwePayload = new Payload(senderPayload);
            // pack JWE
            jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
//			jweHeader.setKeyID("PK.SD.KEK");
            jweObject = new JWEObject(jweHeader, senderJwePayload);
            String publicKey = EID_PUB;
            if (AppConfig.INSTANCE.getMLivenessRequest() != null) {
                publicKey = AppConfig.INSTANCE.getMLivenessRequest().getPublicKey();
            }
            encrypter = new RSAEncrypter((RSAPublicKey) pubkeyEidReader_jws.getPublicKeyFromCertString(publicKey));
            jweObject.encrypt(encrypter);
            senderJwe = jweObject.serialize();

            // pack JWS
            keySignJWS = (RSAPrivateKey) privatekeyEidReader_jwe.getPrivateKeyFromString(this.appLicense);
            signer = new RSASSASigner(keySignJWS);
            JWSHeader header = new JWSHeader(JWSAlgorithm.PS256);
//			header.set("SK.OCE.AUT");

            Payload pl = new Payload(senderJwe);
            jwsObject = new JWSObject(header, pl);
//			System.out.println("signer"+signer);
            jwsObject.sign(signer);
//			System.out.println("JWS: Header: " + header.toJSONObject().toString());
            senderJws = jwsObject.serialize();
//            System.out.print("JWS : " + senderJws + "\n");
            return senderJws;

        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public String decryptJWS(String jwsString) {
        String responseJwe = decryptJWE(jwsString);
        AppUtils.INSTANCE.showLog("-decryptJWS----responseJwe:" + responseJwe);
        if (!responseJwe.isEmpty()) {
            String privateKey = "";
            if (AppConfig.INSTANCE.getMLivenessRequest() != null) {
                privateKey = AppConfig.INSTANCE.getMLivenessRequest().getPrivateKey();
            }
            AppUtils.INSTANCE.showLog("-decryptJWS----privateKey:" + privateKey);
            try {
                PrivateKeyReader privatekeyEidReader_jwe = new PrivateKeyReader();
                RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privatekeyEidReader_jwe.getPrivateKeyFromString(privateKey);

                JWEObject jweObject = JWEObject.parse(responseJwe);
                AppUtils.INSTANCE.showLog("-decryptJWS----44:");
                JWEHeader header = jweObject.getHeader();
                JWEAlgorithm jweAlgorithm = header.getAlgorithm();
                AppUtils.INSTANCE.showLog("-decryptJWS----55:" + JWEAlgorithm.RSA_OAEP_256.equals(jweAlgorithm));
                if (JWEAlgorithm.RSA_OAEP_256.equals(jweAlgorithm)) {
                    jweObject.decrypt(new RSADecrypter(rsaPrivateKey));
                    return jweObject.getPayload().toString();
                } else {
                    return "";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }
        }
        return "";

    }

    //    public String decryptJWS(String encryptedMessage) {
////        initFromStrings()
//        byte[] encryptedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT);
//        Cipher cipher = null ;
//        try {
//            String privateKey = "";
//            if (AppConfig.INSTANCE.getMLivenessRequest() != null) {
//                privateKey = AppConfig.INSTANCE.getMLivenessRequest().getPrivateKey();
//            }
//            PrivateKeyReader privatekeyEidReader_jwe = new PrivateKeyReader();
//            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privatekeyEidReader_jwe.getPrivateKeyFromString(privateKey);
//
//            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
//            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
//            return new String(decryptedMessage, StandardCharsets.UTF_8);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
    private String decryptJWE(String jweString) {
        try {
            String publicKey = "";
            if (AppConfig.INSTANCE.getMLivenessRequest() != null) {
                publicKey = AppConfig.INSTANCE.getMLivenessRequest().getPublicKey();
            }
            AppUtils.INSTANCE.showLog("-decryptJWE----publicKey:" + publicKey);
            PublicKeyReader pubkeyEidReader_jws = new PublicKeyReader();

            RSAPublicKey rsaPublicKey = (RSAPublicKey) pubkeyEidReader_jws.getPublicKeyFromCertString(publicKey);


            JWSObject jwsObject = JWSObject.parse(jweString);
            AppUtils.INSTANCE.showLog("-decryptJWE----3:");


            JWSHeader header = jwsObject.getHeader();

//            JWEAlgorithm jweAlgorithm = header.getAlgorithm();
            if (jwsObject.verify(new RSASSAVerifier(rsaPublicKey))) {
//            jweObject.(new RSADecrypter(rsaPrivateKey));
                String data = jwsObject.getPayload().toString();
                return data;
            } else {
                return "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


}

