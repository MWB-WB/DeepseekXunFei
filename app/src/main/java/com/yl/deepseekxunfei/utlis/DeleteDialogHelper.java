package com.yl.deepseekxunfei.utlis;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yl.deepseekxunfei.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DeleteDialogHelper {

    public static void showDeleteConfirmationDialog(Context context, String title,
                                                    String message, Drawable icon, final DeleteDialogListener listener) {

        new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_Rounded)
                .setTitle(title)
                .setMessage(message)
                .setIcon(icon)
                .setPositiveButton("删除", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteConfirmed();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteCancelled();
                    }
                })
                .setCancelable(true)
                .show();
    }

    public interface DeleteDialogListener {
        void onDeleteConfirmed();

        void onDeleteCancelled();
    }
}
