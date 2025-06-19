package com.yl.deepseekxunfei.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Toast;

import com.yl.basemvp.BaseActivity;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.presenter.PrivacyPolicyPresenter;

public class PrivacyPolicyActivity extends BaseActivity<PrivacyPolicyPresenter> {

    private Button agreeButton;
    private Button disagreeButton;
    private CheckBox consentCheckbox;
    private ScrollView scrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.privacy_policy;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new PrivacyPolicyPresenter();
        mPresenter.attach(this);
    }

    @Override
    protected void initData() {
        mPresenter.initData();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        agreeButton = findViewById(R.id.agreeButton);
        disagreeButton = findViewById(R.id.disagreeButton);
        consentCheckbox = findViewById(R.id.consentCheckbox);
        scrollView = findViewById(R.id.scrollView);
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
                mPresenter.acceptPolicy();
            } else {
                Toast.makeText(this, "请先勾选同意隐私政策", Toast.LENGTH_SHORT).show();
            }
        });

        // 拒绝按钮点击事件
        disagreeButton.setOnClickListener(v -> mPresenter.showExitConfirmationDialog());
    }

    private boolean isScrollToBottom(ScrollView scrollView) {
        if (scrollView.getChildCount() == 0) return false;
        View view = scrollView.getChildAt(0);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff <= 0;
    }

}