package com.yl.ylcommon.utlis;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 权限管理工具类 (按需申请)
 */
public class PermissionManager {

    // 各功能对应的权限组
    private static final String[] AUDIO_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 检查并申请录音权限
     */
    public static void requestAudioPermission(Activity activity, PermissionCallback callback) {
        if (checkPermissions(activity, AUDIO_PERMISSIONS)) {
            callback.onPermissionGranted();
        } else {
            requestPermissions(activity, AUDIO_PERMISSIONS, 1001, callback);
        }
    }

    /**
     * 检查并申请位置权限
     */
    public static void requestLocationPermission(Activity activity, PermissionCallback callback) {
        if (checkPermissions(activity, LOCATION_PERMISSIONS)) {
            callback.onPermissionGranted();
        } else {
            requestPermissions(activity, LOCATION_PERMISSIONS, 1002, callback);
        }
    }

    /**
     * 检查并申请存储权限
     */
    public static void requestStoragePermission(Activity activity, PermissionCallback callback) {
        if (checkPermissions(activity, STORAGE_PERMISSIONS)) {
            callback.onPermissionGranted();
        } else {
            requestPermissions(activity, STORAGE_PERMISSIONS, 1003, callback);
        }
    }

    // 检查权限是否已授予
    private static boolean checkPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // 发起权限请求
    private static void requestPermissions(Activity activity, String[] permissions,
                                           int requestCode, PermissionCallback callback) {

        // 先解释权限用途（可选）
        if (shouldShowRationale(activity, permissions)) {
            showPermissionRationaleDialog(activity, permissions, requestCode, callback);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    // 处理权限请求结果
    public static void handlePermissionsResult(Activity activity, int requestCode,
                                               String[] permissions, int[] grantResults, PermissionCallback callback) {

        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            callback.onPermissionGranted();
        } else {
            if (shouldShowRationale(activity, permissions)) {
                callback.onPermissionDenied();
            } else {
                callback.onPermissionPermanentlyDenied();
            }
        }
    }

    // 显示权限解释对话框
    private static void showPermissionRationaleDialog(Activity activity, String[] permissions,
                                                      int requestCode, PermissionCallback callback) {

        new AlertDialog.Builder(activity)
                .setTitle("权限说明")
                .setMessage(getRationaleMessage(permissions))
                .setPositiveButton("继续", (d, w) ->
                        ActivityCompat.requestPermissions(activity, permissions, requestCode))
                .setNegativeButton("取消", (d, w) -> callback.onPermissionDenied())
                .show();
    }

    // 获取权限说明文本
    private static String getRationaleMessage(String[] permissions) {
        StringBuilder builder = new StringBuilder("需要以下权限才能继续：\n");
        for (String perm : permissions) {
            switch (perm) {
                case Manifest.permission.RECORD_AUDIO:
                    builder.append("• 麦克风权限（用于语音输入）\n");
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    builder.append("• 位置权限（用于定位服务）\n");
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    builder.append("• 存储权限（用于文件存取）\n");
                    break;
            }
        }
        return builder.toString();
    }

    // 检查是否需要显示权限解释
    private static boolean shouldShowRationale(Activity activity, String[] permissions) {
        for (String perm : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                return true;
            }
        }
        return false;
    }

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
        void onPermissionPermanentlyDenied();
    }
}