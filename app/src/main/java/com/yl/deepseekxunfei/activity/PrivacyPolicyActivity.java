package com.yl.deepseekxunfei.activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yl.deepseekxunfei.R;

import java.util.ArrayList;
import java.util.List;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY_VERSION = "1.0";
    private static final int REQUEST_CODE_MAC_ADDRESS = 1001;
    private boolean mDeniedMacAddressPermission = false;
    private SharedPreferences sharedPreferences;
    private Button agreeButton;
    private Button disagreeButton;
    private CheckBox consentCheckbox;
    private ScrollView scrollView;
    private AlertDialog macAddressDialog;
    private Button macAddressDialogPositiveButton;
    private boolean hasScrolledToBottom = false;
    private final int DELAY_MILLIS = 5000;
    private boolean hasExecuted = false;

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

        // 先请求MAC地址权限
        requestMacAddressPermission();
    }

    /**
     * 请求MAC地址权限
     */
    private void requestMacAddressPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 显示自定义权限说明弹窗，并强制阅读5秒
            showMacAddressPermissionDialog(new PermissionRequestCallback() {
                @Override
                public void onPermissionResult(boolean isGranted) {
                    if (isGranted) {
                        // 用户同意后，再检查实际权限状态
                        if (ActivityCompat.checkSelfPermission(PrivacyPolicyActivity.this,
                                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                            // 已有权限，直接继续
                            setupUI();
                        } else {
                            // 请求系统权限
                            ActivityCompat.requestPermissions(
                                    PrivacyPolicyActivity.this,
                                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                                    REQUEST_CODE_MAC_ADDRESS);
                        }
                    } else {
                        // 用户拒绝，记录状态但仍继续
                        mDeniedMacAddressPermission = true;
                        setupUI();
                    }
                }
            });
        } else {
            // Android 6.0以下无需动态权限
            setupUI();
        }
    }

    /**
     * MAC地址权限弹窗（带强制阅读5秒）
     */
    /**
     * MAC地址权限弹窗（带强制阅读5秒）
     */
    public void showMacAddressPermissionDialog(PermissionRequestCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        macAddressDialog = builder.create();
        adaptDialogToScreen(macAddressDialog); // 添加适配调用
        macAddressDialog.show();
        builder.setTitle("设备信息收集与使用说明");

        // 加载自定义布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mac_permission, null);
        TextView messageTextView = dialogView.findViewById(R.id.messageTextView);
        CheckBox dialogConsentCheckbox = dialogView.findViewById(R.id.dialogConsentCheckbox);
        ScrollView scrollView = dialogView.findViewById(R.id.scrollViews);
        Button rejectButton = dialogView.findViewById(R.id.jujue);
        Button acceptButton = dialogView.findViewById(R.id.tongyi);

        // 设置权限说明文本
        messageTextView.setText(getMacPermissionText());

        builder.setView(dialogView);
        macAddressDialog = builder.create();
        macAddressDialog.setCancelable(false);

        // 移除自动生成的按钮
        macAddressDialog.setButton(DialogInterface.BUTTON_POSITIVE, null, (Message) null);
        macAddressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, null, (Message) null);

        macAddressDialog.show();

        // 初始禁用接受按钮和复选框
        acceptButton.setEnabled(false);
        acceptButton.setAlpha(0.5f);
        dialogConsentCheckbox.setEnabled(false);
        dialogConsentCheckbox.setChecked(false);

        // 监听滚动事件
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isScrollToBottom(scrollView)) {
                // 滚动到底部后启用复选框
                dialogConsentCheckbox.setEnabled(true);
                if (!hasExecuted) {
                    hasExecuted = true;
                    Toast.makeText(this, "请勾选（我已阅读并同意上述权限说明）", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> hasExecuted = false, DELAY_MILLIS);
                }
            }
        });

        // 复选框状态变化监听
        dialogConsentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isScrollToBottom(scrollView)) {
                acceptButton.setEnabled(isChecked);
                acceptButton.setAlpha(isChecked ? 1f : 0.5f);
            } else {
                dialogConsentCheckbox.setChecked(false);
                Toast.makeText(this, "请先阅读完权限说明", Toast.LENGTH_SHORT).show();
            }
        });

        // 拒绝按钮点击事件
        rejectButton.setOnClickListener(v -> {
            callback.onPermissionResult(false);
            macAddressDialog.dismiss();
            Toast.makeText(this, "您已拒绝授权，相关功能将受限", Toast.LENGTH_SHORT).show();
        });

        // 启动5秒倒计时
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                acceptButton.setText("同意 (" + seconds + ")");
            }

            @Override
            public void onFinish() {
                acceptButton.setText("同意");
                // 倒计时结束后，只有滚动到底部且勾选了复选框才能点击
                boolean canEnable = isScrollToBottom(scrollView) && dialogConsentCheckbox.isChecked();
                acceptButton.setEnabled(canEnable);
                acceptButton.setAlpha(canEnable ? 1f : 0.5f);
            }
        }.start();

        // 同意按钮点击事件
        acceptButton.setOnClickListener(v -> {
            if (isScrollToBottom(scrollView) && dialogConsentCheckbox.isChecked()) {
                callback.onPermissionResult(true);
                macAddressDialog.dismiss();
            } else {
                Toast.makeText(this, "请先阅读完权限说明并勾选同意", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isScrollToBottom(ScrollView scrollView) {
        if (scrollView.getChildCount() == 0) return false;
        View view = scrollView.getChildAt(0);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff <= 0;
    }

    private String getMacPermissionText() {
        return  "我们承诺严格遵守《网络安全法》《数据安全法》《个人信息保护法》等法律法规，切实保护您的隐私与数据安全。\n" +
                "一、我们收集的设备信息\n" +
                "设备标识符\n" +
                "包括但不限于DEVICEID,Android ID、设备序列号等\n" +
                "用途：用于账号安全绑定、设备唯一性识别、防止恶意攻击\n" +
                "处理方式：加密存储，不与个人身份直接关联\n" +
                "设备基础信息\n" +
                "包括 设备型号、系统版本、屏幕分辨率\n" +
                "用途：优化应用兼容性，确保功能正常运行\n" +
                "网络与连接信息\n" +
                "包括 IP 地址、网络类型、Wi-Fi 状态、MAC 地址\n" +
                "用途：\n" +
                "服务优化（如内容缓存、网络连接稳定性）\n" +
                "网络故障诊断与安全审计\n" +
                "反作弊检测（如多设备异常登录监测）\n" +
                "二、MAC 地址特别说明\n" +
                "1. 收集目的\n" +
                "\n" +
                "设备安全绑定：通过 MAC 地址唯一性增强账号安全性\n" +
                "网络故障诊断：识别网络连接问题的根源\n" +
                "反作弊与防刷机制：检测并阻止自动化脚本和恶意操作\n" +
                "\n" +
                "2. 处理方式\n" +
                "\n" +
                "加密存储：获取后立即进行 SHA-256 不可逆加密，原始 MAC 地址不保留\n" +
                "数据隔离：加密后的 MAC 地址与您的个人身份信息（如姓名、手机号）分开存储\n" +
                "定期删除：数据存储期限最长不超过 180 天，到期自动删除\n" +
                "匿名化处理：所有分析和统计均基于匿名化数据，无法追溯到具体用户\n" +
                "\n" +
                "3. 法律依据\n" +
                "\n" +
                "基于《个人信息保护法》第十三条第（三）项：为履行法定职责或者法定义务所必需\n" +
                "三、您的权利与选择\n" +
                "访问与更正权\n" +
                "您可通过【设置 - 隐私中心】查看和管理您的个人信息\n" +
                "删除权\n" +
                "您有权要求删除已收集的设备信息，删除后可能影响部分功能使用\n" +
                "拒绝权\n" +
                "您可拒绝授权 MAC 地址权限，但可能导致以下功能受限：\n" +
                "账号安全保护机制（如多设备登录提醒）\n" +
                "网络连接稳定性优化\n" +
                "异常操作检测与拦截\n" +
                "投诉与建议\n" +
                "如对我们的隐私政策或数据处理有任何疑问，可通过【设置 - 帮助与反馈】联系我们\n" +
                "四、数据安全承诺\n" +
                "我们承诺：\n" +
                "• 不将设备标识符与身份信息关联分析\n" +
                "• 每年接受第三方隐私安全审计\n" +
                "• 严格遵守《个人信息保护法》要求\n"+
                "我们采用行业标准的安全技术（如 SSL 加密、数据脱敏）保护您的信息\n" +
                "数据仅用于提供服务和改进产品，不与第三方共享（除非法律法规要求）\n" +
                "定期进行安全审计和风险评估，确保数据处理合规\n" +
                "五、DEVICEID 特别说明\n" +
                "**1. 收集目的与必要性\n" +
                "1.1 服务保障\n" +
                "收集 DEVICEID 的核心目的在于实现设备兼容性适配，确保应用在各类终端设备上稳定、流畅运行。以屏幕分辨率适配为例，不同品牌、型号的设备屏幕分辨率差异巨大，如常见的手机屏幕分辨率从 720×1280 到 3200×1440 不等，平板电脑、智能电视等设备的分辨率更是复杂多样。通过 DEVICEID，应用能够精准识别设备型号、屏幕参数等信息，自动调整界面布局、图片尺寸、视频播放比例等，避免出现画面拉伸、元素显示不全等问题，为用户提供统一且优质的视觉体验。此外，在应用功能调用方面，不同设备的传感器、硬件性能不同，DEVICEID 辅助应用判断设备能力，合理分配资源，如在高性能设备上启用更复杂的图形渲染效果，在低配置设备上采用轻量化运行模式，从而提升整体服务质量和用户满意度。\n" +
                "2. 收集方式与处理措施\n" +
                "2.1 收集方式\n" +
                "本应用通过系统安全接口获取 DEVICEID，此过程严格遵循行业安全规范和数据保护要求。系统安全接口经过多重安全验证和防护，仅允许应用获取实现设备兼容性适配及其他核心功能所必需的基础设备标识信息，坚决杜绝 IMEI（国际移动设备识别码）、MEID（移动设备识别码）等敏感信息的收集。IMEI 和 MEID 等信息具有高度唯一性和不可变更性，一旦泄露可能被不法分子用于恶意追踪用户设备、实施诈骗等违法犯罪行为。而本应用获取的 DEVICEID 仅为设备的临时、非敏感标识，最大程度降低用户数据泄露风险。\n" +
                "2.2 安全处理\n" +
                "为进一步保障数据安全，获取的 DEVICEID 将立即进行双重安全处理。首先采用 SHA-256 加密算法对数据进行加密处理，SHA-256 是一种被广泛认可的安全哈希函数，能够将任意长度的数据映射为固定长度（256 位）的哈希值，该哈希值具有不可逆性，即无法通过哈希值还原原始数据。同时，为防止彩虹表攻击等破解手段，还会进行盐值混淆处理，在原始数据中添加一段随机生成的字符串（盐值），使相同的 DEVICEID 在不同设备或不同时间获取时生成不同的哈希值，极大增强数据安全性。\n" +
                "3. 使用范围与共享规则\n" +
                "3.1 使用范围\n" +
                "DEVICEID 仅限用于本应用的以下核心功能模块：\n" +
                "服务稳定性监控：实时收集设备的 DEVICEID 及相关运行数据，监控应用在不同设备上的运行状态，如 CPU 使用率、内存占用、网络连接情况等。一旦发现异常，如大量设备出现闪退、卡顿等问题，可快速定位问题设备类型和范围，及时进行故障排查和修复，保障服务的持续稳定运行。\n" +
                "3.2 共享规则\n" +
                "本应用承诺绝对不与第三方 SDK 共享原始 DEVICEID 标识符，除非法律法规有明确要求。第三方 SDK 通常用于实现广告投放、地图导航、支付等扩展功能，但由于其开发主体和数据使用目的的多样性，共享原始标识符可能导致用户数据泄露风险增加。若因法律规定需要共享数据，本应用将严格按照法定程序和要求进行操作，并在共享前通过公告、站内信等方式向用户告知共享目的、接收方、共享数据范围等详细信息，充分保障用户的知情权。\n" +
                "4. 存储与留存政策\n" +
                "4.1 存储方式\n" +
                "加密后的 DEVICEID 将存储在安全隔离区，该区域采用先进的物理隔离和网络安全防护技术，与其他数据存储区域完全分离。同时，安全隔离区配备严格的访问控制机制，只有经过授权的特定人员在特定操作场景下才能访问数据，且每次访问都会进行详细的日志记录，以便进行审计和追溯。\n" +
                "4.2 留存期限\n" +
                "DEVICEID 的留存期限严格限定为 180 天，自用户最后一次使用应用相关功能起算。超过留存期限后，系统将自动删除相关数据，避免不必要的数据留存带来的安全风险和存储成本。\n" +
                "4.3 注销处理\n" +
                "当用户注销账号后，本应用将在 24 小时内完成与该账号关联的所有 DEVICEID 数据删除工作。数据删除采用不可恢复的删除方式，确保用户数据彻底从系统中清除，保护用户隐私安全。\n" +
                "5. 用户权利与选择\n" +
                "用户有权通过应用内【我的 - 隐私设置】随时关闭 DEVICEID 收集功能。关闭后，可能会影响部分依赖 DEVICEID 实现的功能正常使用，如设备兼容性适配效果可能下降，账号安全验证的准确性可能降低等。但用户依然可以正常使用应用的其他基础功能。同时，本应用会定期对隐私设置相关功能进行优化和完善，确保用户能够便捷、自主地管理个人数据权益。\n"; // 保持原有文本内容
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MAC_ADDRESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mDeniedMacAddressPermission = false;
            } else {
                mDeniedMacAddressPermission = true;
                Toast.makeText(this, "您已拒绝授权，相关功能将受限2", Toast.LENGTH_SHORT).show();
            }
            // 无论是否同意，都继续显示隐私政策
            setupUI();
        }
    }

    private void setupUI() {
        // 初始设置同意按钮不可用
        agreeButton.setEnabled(false);
        agreeButton.setAlpha(0.5f);
        consentCheckbox.setEnabled(false); // 初始禁用复选框

        // 滚动到最底部后启用复选框
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isScrollToBottom(scrollView)) {
                hasScrolledToBottom = true;
                consentCheckbox.setEnabled(true);
                if (!hasExecuted) {
                    hasExecuted = true; // 设置已执行标记
                    Toast.makeText(this, "您已阅读完隐私政策，请勾选同意", Toast.LENGTH_SHORT).show();
                    // 使用 Handler 设置定时器
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hasExecuted = false; // 重置已执行标记
                        }
                    }, DELAY_MILLIS);
                }
            } else {
                consentCheckbox.setEnabled(false);
            }
        });

        // 复选框状态变化监听
        consentCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 只有在滚动到底部后才能启用同意按钮
            if (hasScrolledToBottom) {
                agreeButton.setEnabled(isChecked);
                agreeButton.setAlpha(isChecked ? 1f : 0.5f);
            } else {
                consentCheckbox.setChecked(false);
                Toast.makeText(this, "请先阅读完隐私政策", Toast.LENGTH_SHORT).show();
            }
        });

        // 同意按钮点击事件
        agreeButton.setOnClickListener(v -> {
            if (consentCheckbox.isChecked() && hasScrolledToBottom) {
                Log.d("同意隐私政策", "setupUI: 进入");
                acceptPolicy();
                proceedToMainActivity();
            } else {
                Toast.makeText(this, "请先勾选同意隐私政策并阅读完所有内容", Toast.LENGTH_SHORT).show();
            }
        });

        // 拒绝按钮点击事件
        disagreeButton.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private boolean isPolicyAccepted() {
        String acceptedVersion = sharedPreferences.getString("privacy_policy_version", "");
        return acceptedVersion.equals(PRIVACY_POLICY_VERSION);
    }

    private boolean shouldShowAnyPermissionRationale(List<String> permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }



    private void acceptPolicy() {
        sharedPreferences.edit()
                .putBoolean("privacy_policy_accepted", true)
                .putString("privacy_policy_version", PRIVACY_POLICY_VERSION)
                .apply();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁时关闭对话框，避免内存泄漏
        if (macAddressDialog != null && macAddressDialog.isShowing()) {
            macAddressDialog.dismiss();
        }
    }

    public interface PermissionRequestCallback {
        void onPermissionResult(boolean isGranted);
    }
    private void adaptDialogToScreen(AlertDialog dialog) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        float density = metrics.density;

        // 动态设置对话框宽度（屏幕宽度的80%-90%）
        int dialogWidth = (int) (screenWidth * 0.85);
        if (screenWidth > 1080) { // 大屏设备
            dialogWidth = (int) (screenWidth * 0.7);
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // 动态调整字体大小
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);
        if (messageTextView != null) {
            float textSize = (screenWidth < 600) ? 12 : 14; // 根据宽度选择字号
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }
}