package com.liveness.sdk.corev4

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

/**
 * Created by Hieudt43 on 26/09/2024.
 */
internal class FaceMatchActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_activity_fm)

        val transaction = supportFragmentManager.beginTransaction()
        val fragment = FaceMatchFragment()
        fragment.arguments = intent.extras
        transaction.replace(R.id.fmContainer, fragment)
        transaction.addToBackStack(FaceMatchFragment::class.java.name)
        transaction.commit()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}