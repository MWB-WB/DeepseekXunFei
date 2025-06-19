package com.yl.deepseekxunfei.presenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.yl.basemvp.BasePresenter;
import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.activity.PrivacyPolicyActivity;

public class PrivacyPolicyPresenter extends BasePresenter<PrivacyPolicyActivity> {

    private static final String PRIVACY_POLICY_VERSION = "1.0";
    private SharedPreferences sharedPreferences;

    public void initData() {
        sharedPreferences = mActivity.get().getSharedPreferences("AppPreferences", mActivity.get().MODE_PRIVATE);
        // 检查是否已经同意当前版本的隐私政策
        if (isPolicyAccepted()) {
            proceedToMainActivity();
            return;
        }
    }

    @Override
    protected void onItemClick(View view) {

    }

    public boolean isPolicyAccepted() {
        String acceptedVersion = sharedPreferences.getString("privacy_policy_version", "");
        return acceptedVersion.equals(PRIVACY_POLICY_VERSION);
    }

    public void acceptPolicy() {
        sharedPreferences.edit()
                .putBoolean("privacy_policy_accepted", true)
                .putString("privacy_policy_version", PRIVACY_POLICY_VERSION)
                .apply();
        proceedToMainActivity();
    }

    private void proceedToMainActivity() {
        mActivity.get().startActivity(new Intent(mActivity.get(), MainActivity.class));
        mActivity.get().finish();
    }

    public void showExitConfirmationDialog() {
        new AlertDialog.Builder(mActivity.get())
                .setTitle("退出确认")
                .setMessage("您需要同意隐私政策才能使用本应用，确定要退出吗？")
                .setPositiveButton("确定退出", (dialog, which) -> mActivity.get().finish())
                .setNegativeButton("取消", null)
                .show();
    }

}
