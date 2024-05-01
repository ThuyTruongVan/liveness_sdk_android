package com.liveness.flash

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.liveness.sdk.core.LiveNessSDK
import com.liveness.sdk.core.model.LivenessModel
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.CallbackLivenessListener

/**
 * Created by Thuytv on 15/04/2024.
 */
class MainActivity : AppCompatActivity() {
    private var deviceId = ""
    private lateinit var btnRegisterFace: Button
    private lateinit var btnLiveNessFlash: Button
    private lateinit var vlContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_main_activity)
        btnRegisterFace = findViewById(R.id.btn_register_face)
        btnLiveNessFlash = findViewById(R.id.btn_live_ness_flash)
        vlContent = findViewById(R.id.vl_content)
        
    }

}