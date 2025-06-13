package com.yl.deepseekxunfei;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY_VERSION = "1.0";
    private SharedPreferences sharedPreferences;
    private Button agreeButton;
    private Button disagreeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        agreeButton = findViewById(R.id.agreeButton);
        disagreeButton = findViewById(R.id.disagreeButton);

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
        final ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

        agreeButton.setEnabled(false);
        agreeButton.setAlpha(0.5f);

        // 滚动监听
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isScrollToBottom(scrollView)) {
                agreeButton.setEnabled(true);
                agreeButton.setAlpha(1f);
            }
        });

        // 同意按钮点击事件
        agreeButton.setOnClickListener(v -> acceptPolicy());

        // 拒绝按钮点击事件
        disagreeButton.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private boolean isScrollToBottom(ScrollView scrollView) {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
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
