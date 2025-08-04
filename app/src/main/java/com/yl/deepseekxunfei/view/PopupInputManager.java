package com.yl.deepseekxunfei.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yl.deepseekxunfei.R;

public class PopupInputManager {
    private PopupWindow popupWindow;
    private EditText hiddenInput;
    private InputCallback callback;

    // 输入回调接口
    public interface InputCallback {
        void onInputChanged(String text); // 输入变化时回调
        void onInputCompleted(String text); // 输入完成（点击完成按钮）时回调
    }

    public PopupInputManager(Activity activity, InputCallback callback) {
        this.callback = callback;
        initPopupWindow(activity);
    }

    // 初始化PopupWindow
    private void initPopupWindow(Activity activity) {
        // 加载弹窗布局
        View popupView = LayoutInflater.from(activity).inflate(R.layout.popup_input, null);
        hiddenInput = popupView.findViewById(R.id.et_hidden_input);

        // 初始化PopupWindow（宽高设为1x1，避免影响界面，实际可根据需求调整）
        popupWindow = new PopupWindow(
                popupView,
                500, 200, // 宽高为1像素，视觉上几乎不可见
                true // 允许弹窗获取焦点
        );

        // 设置输入监听
        setupInputListener();
    }

    // 设置输入监听
    private void setupInputListener() {
        // 实时监听文本变化
        hiddenInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (callback != null) {
                    callback.onInputChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 监听键盘“完成”按钮
        hiddenInput.setOnEditorActionListener((v, actionId, event) -> {
            if (callback != null) {
                callback.onInputCompleted(v.getText().toString());
            }
            dismiss(); // 完成后关闭弹窗和键盘
            return true;
        });
    }

    // 显示弹窗并唤起键盘
    public void show(Activity activity) {
        if (popupWindow.isShowing()) return;

        // 显示弹窗（位置随意，因尺寸极小不影响界面）
        popupWindow.showAtLocation(
                activity.findViewById(android.R.id.content),
                Gravity.CENTER,
                0, 0
        );

        // 延迟唤起键盘，确保弹窗已显示
        hiddenInput.postDelayed(() -> {
            hiddenInput.requestFocus(); // 让输入框获取焦点
            // 强制唤起键盘
            InputMethodManager imm = (InputMethodManager) hiddenInput.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_FORCED);
        }, 300);
    }

    // 关闭弹窗和键盘
    public void dismiss() {
        if (popupWindow.isShowing()) {
            // 隐藏键盘
            InputMethodManager imm = (InputMethodManager) hiddenInput.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(hiddenInput.getWindowToken(), 0);

            // 清空输入并关闭弹窗
            hiddenInput.setText("");
            popupWindow.dismiss();
        }
    }
}
