package com.liveness.sdk.corev4.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liveness.sdk.corev4.R;

/**
 * Created by Hieudt43 on 14/12/2024.
 */
public class DialogUtils {
  public static void showConfirmDialog(Activity activity, String title, String message, String positiveText, String negativeText, InformationDialogListener listener) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog_Alert);
    View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_confirm_dialog, null);
    AlertDialog diaglog = builder.setView(dialogView)
            .create();
    diaglog.setCancelable(false);
    TextView tvTitle = dialogView.findViewById(R.id.tv_title);
    TextView tvContent = dialogView.findViewById(R.id.tvContent);
    Button btnPositive = dialogView.findViewById(R.id.btn_positive);
    Button btnNegative = dialogView.findViewById(R.id.btnNegative);

    tvTitle.setText(title);
    tvContent.setText(message);
    btnPositive.setText(positiveText);
    btnNegative.setText(negativeText);
    btnPositive.setOnClickListener(v -> {
      if (listener != null) {
        listener.onPositiveClick();
      }
      diaglog.hide();
      diaglog.dismiss();
    });
    btnNegative.setOnClickListener(v ->{
      if (listener != null) {
        listener.onNegativeClick();
      }
      diaglog.dismiss();
    });
    diaglog.show();
  }
}
