package com.liveness.flash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.liveness.sdk.core.R

/**
 * Created by Thuytv on 18/05/2024.
 */
class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(com.liveness.flash.R.layout.ui_custom_view, container, false)


        return view
    }
}