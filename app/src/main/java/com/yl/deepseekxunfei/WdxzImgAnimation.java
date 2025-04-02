package com.yl.deepseekxunfei;

import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class WdxzImgAnimation {
    private ImageView imageView;
    private ObjectAnimator animator;

    // 构造函数，传入 ImageView
    public WdxzImgAnimation(ImageView imageView) {
        this.imageView = imageView;
    }

    public void animation() {
        if (imageView != null) {
            // 创建一个ObjectAnimator，设置属性为translationY（垂直方向的平移）
            animator = ObjectAnimator.ofFloat(imageView, "translationY", 0f, -10f, 0f);
            // 设置动画持续时间（毫秒）
            animator.setDuration(1000);
            // 设置动画重复模式为循环
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            // 设置动画重复次数为无限次
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            // 设置动画插值器，这里使用线性插值器
            animator.setInterpolator(new LinearInterpolator());
            // 启动动画
            animator.start();
        }
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
}