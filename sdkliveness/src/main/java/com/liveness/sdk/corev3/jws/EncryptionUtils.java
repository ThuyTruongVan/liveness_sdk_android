package com.liveness.sdk.corev3.jws;

import android.util.Log;

import com.google.android.gms.common.api.Response;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.EncryptedJWT;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;

/**
 * @author speerbuc@visa.com
 **/
public final class EncryptionUtils {

    private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
    private static final String CONTENT_TYPE_JWE = "JWE";
    private static final String CONTENT_TYPE_XML = "application/xml";
    private static final String SHA_256 = "SHA-256";
    private static final String ERROR_MESSAGE_INVALID_SIGNATURE = "Invalid signature";
    private static final String HEADER_CTY = "cty";
    private static final String HEADER_IAT = "iat";
    private static final String HEADER_EXP = "exp";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private EncryptionUtils() {
    }


    public static String decryptJwe(String jweString, String sharedSecret) {
        try {
            EncryptedJWT encryptedJWT = EncryptedJWT.parse(jweString);
            encryptedJWT.decrypt(new AESDecrypter(sha256(sharedSecret)));
            return encryptedJWT.getPayload().toString();
        } catch (Exception e) {
            Log.d("Thuytv", "-------decryptJwe--error");
            e.printStackTrace();
        }
        return "";
    }

    public static String verifyAndExtractJweFromJWS(String jws, String sharedSecret) {
        try {
            JWSObject jwsObject = JWSObject.parse(jws);
            if (!jwsObject.verify(new MACVerifier(sharedSecret.getBytes(CHARSET_UTF_8)))) {
                return "";
            }
            Map<String, Object> customParameters = jwsObject.getHeader().getCustomParams();
            Long now = System.currentTimeMillis() / 1000;
            if (customParameters != null && customParameters.get(HEADER_IAT) != null
                    && ((Long) customParameters.get(HEADER_IAT) > now || (Long) customParameters.get(HEADER_EXP) < now)) {
                return "";
            }
            return jwsObject.getPayload().toString();
        } catch (Exception e) {
            Log.d("Thuytv", "-------verifyAndExtractJweFromJWS--error");
            e.printStackTrace();
        }
        return "";
    }

    public static String decryptJwe(String jweString, RSAPrivateKey rsaPrivateKey) {
        try {
            JWEObject jweObject = JWEObject.parse(jweString);
            JWEHeader header = jweObject.getHeader();
            JWEAlgorithm jweAlgorithm = header.getAlgorithm();
            if (JWEAlgorithm.RSA_OAEP_256.equals(jweAlgorithm)) {
                jweObject.decrypt(new RSADecrypter(rsaPrivateKey));
                return jweObject.getPayload().toString();
            }
        } catch (Exception e) {
            Log.d("Thuytv", "-------verifyAndExtractJweFromJWS--error");

            e.printStackTrace();
        }
        return "";

    }


    public static String verifyAndExtractJweFromJWS(String jws, RSAPublicKey publicKey) {
        try {
            JWSObject jwsObject = JWSObject.parse(jws);
            if (!jwsObject.verify(new RSASSAVerifier(publicKey))) {
                return "";
            }
            return jwsObject.getPayload().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * Construct the JWE Header
     *
     * @param kid               -The Key User ID
     * @param jweAlgorithm      - The JWE Encryption Algorithm
     * @param encryptionMethod  - The JWE Encryption Method
     * @param additionalHeaders - Additional JWE Headers
     * @return {@link JWEHeader}
     */
    private static JWEHeader header(String kid, JWEAlgorithm jweAlgorithm, EncryptionMethod encryptionMethod, Map<String, Object> additionalHeaders) {
        JWEHeader.Builder builder = new JWEHeader.Builder(jweAlgorithm, encryptionMethod).keyID(kid).type(JOSEObjectType.JOSE);
        if (additionalHeaders != null && additionalHeaders.size() > 0) {
            for (String k : additionalHeaders.keySet()) {
                Object value = additionalHeaders.get(k);
                if (HEADER_CTY.equalsIgnoreCase(k)) {
                    builder.contentType(value.toString());
                } else {
                    builder.customParam(k, additionalHeaders.get(k));
                }
            }
        }
        return builder.build();
    }


    private static byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            md.update(input.getBytes(CHARSET_UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
//            throw new GenericSecurityException("No Such Algorithm", e);
            e.printStackTrace();
        }
        return null;
    }

//    public void createAndDecryptJweTestUsingRSAPKI() throws Exception {
//        //Generate a random key pair for unit test
//
//
//        //If you have a private key file, please use below code to load the private key. Private key should be in PEM format
//        String dir = System.getProperty("user.dir");
//        dir += "/src/main/java/com/visa/ddp/cise/utils/";
//        RSAPrivateKey privateKey = CertificateUtils.loadPrivateKeyFromFile(dir + "com.pvcb");
////        PrivateKey privateKey = keyPair.getPrivate();
////        LOGGER.info("Generated Private Key: " + DatatypeConverter.printBase64Binary(privateKey.getEncoded()));
//
//        //If you have a public key or certificate file, please use below code to load the private key. Public key or certificate should be in PEM format
//        RSAPublicKey publicKey = CertificateUtils.loadPublicKeyFromFile(dir + "eid.pub");
////        LOGGER.info("Generated Public Key: " + DatatypeConverter.printBase64Binary(publicKey.getEncoded()));
//
//        //Generate Random KID
//        String kid = UUID.randomUUID().toString();
//
//        Map<String, Object> jweHeaders = new HashMap<String, Object>();
//        jweHeaders.put("iat", System.currentTimeMillis());
//        ObjectMapper objectMapper = new ObjectMapper();
//        String testString = new String(Files.readAllBytes(Paths.get(dir + "input.txt")));
//
//        String jwe = EncryptionUtils.createJwe(testString, kid, (RSAPublicKey) publicKey, JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM, jweHeaders);
//        Map<String, Object> jwsHeaders = new HashMap<String, Object>();
//        long iat = System.currentTimeMillis() / 1000;
//        Long exp = iat + 120;
//        jwsHeaders.put("iat", iat);
//        jwsHeaders.put("exp", exp);
//
//        String signingKid = UUID.randomUUID().toString();
//        String jws = EncryptionUtils.createJws(jwe, signingKid, (RSAPrivateKey) privateKey, jwsHeaders);
//        OkHttpClient client = new OkHttpClient();
//        MediaType mediaType = MediaType.parse("application/json");
//        String bodyStringg = "{\n" +
//                "    \"jws\": \"" + jws + "\"\n" +
//                "}";
//        RequestBody body = RequestBody.create(mediaType, bodyStringg);
//        Request request = new Request.Builder()
//                .url("https://ekyc-sandbox.eidas.vn/eid/v3/read")
//                .method("POST", body)
//                .addHeader("appid", "com.pvcb")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("encrypted", "true")
//                .build();
//        try {
//
//            Response response = client.newCall(request).execute();
//            String responseString = response.body().string();
//            System.out.println("Original response");
//            System.out.printf(responseString);
//            System.out.printf("=======================================================");
//            Map<String, String> hashMap = objectMapper.readValue(responseString, Map.class);
//            String responseJws = hashMap.get("jws");
//            String responseJwe = verifyAndExtractJweFromJWS(responseJws, publicKey);
//            String payload = decryptJwe(responseJwe, privateKey);
//            System.out.println(payload);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static void main(String[] args) {
//        try {
//            new EncryptionUtils().createAndDecryptJweTestUsingRSAPKI();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}