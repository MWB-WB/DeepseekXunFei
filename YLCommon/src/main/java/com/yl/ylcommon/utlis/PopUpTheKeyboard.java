package com.yl.ylcommon.utlis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.yl.ylcommon.R;

/**
 * 强制键盘弹出
 *
 */
public class PopUpTheKeyboard {
    public static void forceShowKeyboard(Activity activity, EditText editText) {
        editText.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }, 100);
    }
}
