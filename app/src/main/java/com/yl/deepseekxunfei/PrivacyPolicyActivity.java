package com.yl.deepseekxunfei;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY_VERSION = "1.0";
    private SharedPreferences sharedPreferences;
    private Button agreeButton;
    private Button disagreeButton;
    private CheckBox consentCheckbox;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        agreeButton = findViewById(R.id.agreeButton);
        disagreeButton = findViewById(R.id.disagreeButton);
        consentCheckbox = findViewById(R.id.consentCheckbox);
        scrollView = findViewById(R.id.scrollView);

        // 检查是否已经同意当前版本的隐私政策
        if (isPolicyAccepted()) {
            proceedToMainActivity();
            return;
        }

        setupUI();
    }

    private boolean isPolicyAccepted() {
        String acceptedVersion = sharedPreferences.getString("privacy_policy_version", "");
        return acceptedVersion.equals(PRIVACY_POLICY_VERSION);
    }

    private void setupUI() {
        // 初始设置同意按钮不可用
        agreeButton.setEnabled(false);
        agreeButton.setAlpha(0.5f);

        // 滚动到最底部后启用复选框
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isScrollToBottom(scrollView)) {
                consentCheckbox.setEnabled(true);
            }
        });

        // 复选框状态变化监听
        consentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            agreeButton.setEnabled(isChecked);
            agreeButton.setAlpha(isChecked ? 1f : 0.5f);
        });

        // 同意按钮点击事件
        agreeButton.setOnClickListener(v -> {
            if (consentCheckbox.isChecked()) {
                acceptPolicy();
            } else {
                Toast.makeText(this, "请先勾选同意隐私政策", Toast.LENGTH_SHORT).show();
            }
        });

        // 拒绝按钮点击事件
        disagreeButton.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private boolean isScrollToBottom(ScrollView scrollView) {
        if (scrollView.getChildCount() == 0) return false;
        View view = scrollView.getChildAt(0);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff <= 0;
    }

    private void acceptPolicy() {
        sharedPreferences.edit()
                .putBoolean("privacy_policy_accepted", true)
                .putString("privacy_policy_version", PRIVACY_POLICY_VERSION)
                .apply();
        proceedToMainActivity();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出确认")
                .setMessage("您需要同意隐私政策才能使用本应用，确定要退出吗？")
                .setPositiveButton("确定退出", (dialog, which) -> finish())
                .setNegativeButton("取消", null)
                .show();
    }

    private void proceedToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}