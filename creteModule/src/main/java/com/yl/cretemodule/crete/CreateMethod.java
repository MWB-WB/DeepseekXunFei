package com.yl.cretemodule.crete;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.yl.cretemodule.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;

import java.util.concurrent.atomic.AtomicInteger;

public class CreateMethod extends AppCompatActivity {
    //初始话科大讯飞声纹识别
    public String APP_ID;
    public String APISecret;
    public String APIKey;
    public String requestUrl;
    //声纹识别所需文件保存路径
    public String creteFlies;
    public boolean fig = true;
    public int createOne = 0;//是否第一次注册，第一次注册不进行判断
    public HashMap<String, String> mIatResults = new LinkedHashMap<>();
    //对比声纹文件
    String contrastFies;
    //全局声纹识别创建文件工具类
    public CreteUtlis creteUtlis = new CreteUtlis();
    public CreateLogotype createLogotype = new CreateLogotype();
    public boolean deleteFig = false;//是否进入删除成功回调
    int che = 0;//判断是不是第一次注册车主声纹
    int i = 1;
    final Map<String, String>[] SearchOneFeatureList = new Map[]{new HashMap<>()}; //1:1服务结果
    final Map<String, String>[] result = new Map[]{new HashMap<>()};//1:N服务结果
    final Map<String, String>[] group = new Map[]{new HashMap<>()};//创建特征库服务结果
    List<String> groupIdList = new ArrayList<>();//分组标识
    List<String> groupNameList = new ArrayList<>();//声纹分组名称
    List<String> groupInfoLsit = new ArrayList<>();//分组描述信息
    List<String> featureIdList = new ArrayList<>();//特征唯一标识
    List<String> featureInfoList = new ArrayList<>();//特征描述
    public RelativeLayout relativeLayout;
    Dialog customDialog;
    RadioGroup radio_teacher;
    RadioButton radioButtonChe;
    RadioButton radioButtonPy;
    RadioButton radioButtonJr;
    public Button btnStart;
    Button selectCrete;//删除声纹信息
    public boolean isRecording = false;
    MediaRecorder mediaRecorder = new MediaRecorder();
    String selectedText;
    public RecognizerDialog mIatDialog;// 语音听写UI
    public Context contexts;
    Button closeDialogButton;

    public CreateMethod() {

    }

    public void init(Context context) {
        contexts = context;
        APP_ID = "27b3a946";
        APISecret = "MGNhOTM2Yjg3MmVhMTFjYzhhODQzMTYw";
        APIKey = "06224092793087296b1f47c96e0133bc";
        requestUrl = "http://api.xf-yun.com/v1/private/s782b4996";
    }

    //查询声纹特征的方法
    public boolean seleteCrete() {
        // 检查groupId是否存在
        if (createLogotype.getGroupId() != null && !createLogotype.getGroupId().isEmpty()) {
            // 开始查询前清空并显示容器
            relativeLayout.removeAllViews();
            relativeLayout.setVisibility(View.VISIBLE);

            // 用于存储所有结果的列表
            final List<JSONObject> allResults = new ArrayList<>();


            // 记录groupId总数和已完成的查询数
            final int totalGroups = createLogotype.getGroupId().size();
            final AtomicInteger completedGroups = new AtomicInteger(0);

            // 遍历所有groupId进行查询
            for (int j = 0; j < totalGroups; j++) {
                final String groupId = createLogotype.getGroupId().get(j);
                QueryFeatureList.doQueryFeatureList(requestUrl, APP_ID, APISecret, APIKey, createLogotype, groupId,
                        new QueryFeatureList.NetCall() {
                            @Override
                            public void OnSuccess(String success) {
                                try {
                                    JSONArray jsonArray = new JSONArray(success);

                                    // 将当前groupId的结果添加到总结果列表
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject item = jsonArray.getJSONObject(i);
                                        item.put("groupId", groupId); // 保存groupId到结果中
                                        allResults.add(item);
                                    }

                                    // 检查是否所有groupId都查询完成
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        // 所有查询完成后，统一更新UI
                                        runOnUiThread(() -> updateUIWithResults(allResults));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "解析JSON失败", e);
                                }
                            }

                            @Override
                            public void OnError() {
                                runOnUiThread(() -> {
                                    Toast.makeText(contexts, "查询groupId " + groupId + " 失败", Toast.LENGTH_SHORT).show();
                                    // 即使部分查询失败，也继续处理已有的结果
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        updateUIWithResults(allResults);
                                    }
                                });
                            }
                        });
            }
            return true;
        } else {
            if (!deleteFig) {
                Toast.makeText(contexts, "暂无声纹信息，请注册", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    // 删除声纹特征的方法
    public void deleteFeature(String groupId, String featureId, int viewId) {
        // 显示加载中
        ProgressDialog progressDialog = new ProgressDialog(contexts);
        progressDialog.setMessage("正在删除...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // 执行删除请求
        DeleteFeature.doDeleteFeature(requestUrl, APP_ID, APISecret, APIKey, createLogotype, groupId, featureId, new DeleteFeature.NetCallDeleteCrete() {
            @Override
            public void OnSuccess(String success) {
                DeleteGroup.doDeleteGroup(requestUrl, APP_ID, APISecret, APIKey, groupId, new DeleteGroup.NetDeleteGroup() {
                    @Override
                    public void OnSuccessGroup(String success) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(contexts, "删除成功", Toast.LENGTH_SHORT).show();
                            deleteFig = true;
                            che = 0;
                            i = 1;
                            //根据删除的id获取到对应的下标，在根据下标删除对应的描述信息和声纹唯一标识
                            createLogotype.getGroupInfo().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getGroupName().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getFeatureInfo().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getFeatureId().remove(featureId);
                            createLogotype.getGroupId().remove(groupId);
                            Log.d(TAG, "OnSuccessGroup: " + createLogotype);
                            boolean fig = seleteCrete();
                            //u1FamilyMember0
                            if (!fig) {
                                // 关闭弹窗
                                customDialog.dismiss();
                                if (isRecording) {
                                    try {
                                        mediaRecorder.stop();
                                        mediaRecorder.release();
                                        isRecording = false;

                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void OnErrorGroup() {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(contexts, "删除特征库失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void OnError() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(contexts, "删除声纹特征失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 统一更新UI的方法
    public void updateUIWithResults(List<JSONObject> results) {
        // 清空之前的视图
        relativeLayout.removeAllViews();

        // 记录上一个视图的ID
        int lastViewId = 0;

        // 遍历所有结果，创建视图
        for (JSONObject item : results) {
            try {
                String featureInfo = item.getString("featureInfo");
                String featureId = item.getString("featureId");
                String groupId = item.getString("groupId"); // 获取保存的groupId

                // 创建一个线性布局作为条目容器
                LinearLayout itemLayout = new LinearLayout(contexts);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 0, 0, 10);

                // 生成唯一的viewId
                int itemViewId = View.generateViewId();
                itemLayout.setId(itemViewId);

                // 设置布局参数
                RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                // 如果不是第一个视图，则放在上一个视图的下方
                if (lastViewId != 0) {
                    itemParams.addRule(RelativeLayout.BELOW, lastViewId);
                }

                itemLayout.setLayoutParams(itemParams);

                // 创建文本视图
                TextView textView = new TextView(contexts);
                textView.setText(featureInfo);
                textView.setTextSize(18);
                textView.setPadding(16, 16, 16, 16);
                textView.setBackgroundResource(android.R.color.holo_blue_light);

                // 设置文本视图的布局参数，使其占据大部分空间
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0, // 宽度为0，由weight决定
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f // 权重为1，占据剩余空间
                );
                textView.setLayoutParams(textParams);

                // 创建删除按钮
                Button deleteButton = new Button(contexts);
                deleteButton.setText("删除");
                deleteButton.setBackgroundResource(android.R.color.holo_red_light);
                deleteButton.setTextColor(Color.WHITE);

                // 设置删除按钮的布局参数
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.setMargins(10, 0, 0, 0);
                deleteButton.setLayoutParams(buttonParams);

                // 为删除按钮设置点击事件，绑定groupId和featureId
                deleteButton.setOnClickListener(v2 -> {
                    try {
                        // 显示确认对话框
                        new AlertDialog.Builder(contexts)
                                .setTitle("确认删除")
                                .setMessage("确定要删除此声纹吗？\nGroup ID: " + groupId + "\nFeature ID: " + featureId)
                                .setPositiveButton("确认", (dialog, which) -> {
                                    // 执行删除操作
                                    deleteFeature(groupId, featureId, itemViewId);
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } catch (Exception e) {
                        Log.e(TAG, "删除按钮点击事件异常", e);
                    }
                });
                // 将文本视图和删除按钮添加到条目布局
                itemLayout.addView(textView);
                itemLayout.addView(deleteButton);

                // 将条目布局添加到主布局
                relativeLayout.addView(itemLayout);

                // 更新最后一个视图的ID
                lastViewId = itemViewId;

            } catch (JSONException e) {
                Log.e(TAG, "处理结果失败", e);
            }
        }
    }

    //判断是否已注册声纹
    public boolean createFig() {
        if (createLogotype.getGroupId() != null) {
            if (createLogotype.getGroupId().size() > 1) {
                for (int j = 0; j < createLogotype.getGroupId().size(); j++) {
                    result[0] = SearchFeature.doSearchFeature(requestUrl, APP_ID, APISecret, APIKey, contrastFies, createLogotype, createLogotype.getGroupId().get(j));//1:N比对
                    if (result[0] != null) {
                        if (Double.parseDouble(Objects.requireNonNull(result[0].get("score"))) >= 0.35) {
                            fig = false;
                            break;
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(contexts, "没有匹配的声纹信息，请注册1：N", Toast.LENGTH_SHORT).show());
                        fig = true;
                    }
                }
            } else if (createLogotype.getGroupId().size() == 1) {
                for (int j = 0; j < createLogotype.getGroupId().size(); j++) {
                    for (int k = 0; k < createLogotype.getFeatureId().size(); k++) {
                        SearchOneFeatureList[0] = SearchOneFeature.doSearchOneFeature(requestUrl, APP_ID, APISecret, APIKey, contrastFies, createLogotype, createLogotype.getGroupId().get(j), createLogotype.getFeatureId().get(k));//1:1
                        if (SearchOneFeatureList[0] != null && SearchOneFeatureList[0].get("score") != null) {
                            if (Double.parseDouble(Objects.requireNonNull(SearchOneFeatureList[0].get("score"))) >= 0.35) {
                                fig = false;
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(contexts, "没有匹配的声纹信息，请注册1:1", Toast.LENGTH_SHORT).show());
                            fig = true;
                        }
                    }
                }
            }
        } else {
            runOnUiThread(() -> Toast.makeText(contexts, "请先注册声纹信息", Toast.LENGTH_SHORT).show());
            fig = true;
        }
        return fig;
    }

    public void kaishi() {
        mIatDialog = new RecognizerDialog(contexts, mInitListener);
        customDialog = new Dialog(contexts);
        customDialog.setContentView(R.layout.media_ecorder_recording);
        //初始化单选按钮
        radio_teacher = customDialog.findViewById(R.id.radio_group_identity);
        radioButtonChe = customDialog.findViewById(R.id.radio_teacher);
        radioButtonPy = customDialog.findViewById(R.id.radio_friend);
        radioButtonJr = customDialog.findViewById(R.id.radio_student);

        // 获取 media_ecorder_recording.xml 布局中的按钮和文本控件
        btnStart = customDialog.findViewById(R.id.btn_start);
        // 找到关闭按钮
        closeDialogButton = customDialog.findViewById(R.id.btn_stop);
        selectCrete = customDialog.findViewById(R.id.query_selete);
        relativeLayout = customDialog.findViewById(R.id.pathRelative);
        // 设置关闭按钮点击事件
        closeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭弹窗
                customDialog.dismiss();
                if (isRecording) {
                    try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        isRecording = false;
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //开始录音
        btnStart.setOnClickListener(v1 -> {
            if (radioButtonChe.isChecked()) {
                selectedText = radioButtonChe.getText().toString().trim();
            } else if (radioButtonPy.isChecked()) {
                selectedText = radioButtonPy.getText().toString().trim();
            } else if (radioButtonJr.isChecked()) {
                selectedText = radioButtonJr.getText().toString().trim();
            } else {
                runOnUiThread(() -> Toast.makeText(contexts, "请先选择身份！！", Toast.LENGTH_SHORT).show());
                return;
            }
            //创建声纹文件
            creteFlies = creteUtlis.createAudioFilePath(contexts, selectedText + "_" + i);
            creteUtlis.startRecord(new WeakReference<>(this), creteFlies);
            //对比声纹文件
            contrastFies = creteUtlis.createAudioFilePath(contexts, selectedText + "_" + i);
            creteUtlis.startRecord(new WeakReference<>(this), contrastFies);
            mIatDialog.setListener(new RecognizerDialogListener() {
                @Override
                public void onResult(RecognizerResult recognizerResult, boolean b) {
                    try {
                        createVoiceprint(recognizerResult, b);//调用解析，解析中调用创建声纹库，对比等操作
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(SpeechError speechError) {

                }
            });
            mIatDialog.show();
            //获取字体所在控件
            TextView txt = (TextView) mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
            txt.setText("您可以朗读一段文本，为保证声纹识别的准确度，请保证录音时长在5到10秒之间");
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //设置为点击无反应，避免跳转到讯飞平台
                }
            });
        });
        //查询声纹信息
        selectCrete.setOnClickListener(select -> {
            seleteCrete();
        });
        // 显示自定义 Dialog
        customDialog.show();
    }

    //注册声纹
    public void create() {
        boolean createEnrollFig = true;
        // 判断是否已注册声纹信息
//        if (createOne == 1) {
//            createEnrollFig = createFig();
//        }
        if (createEnrollFig) {
            //匹配成功返回false失败返回true
            // 打印或使用选中的按钮文本

            String name = selectedText;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    groupIdList.add("u1" + translateToEnglish(name) + i);
                    groupNameList.add(name + i);
                    groupInfoLsit.add(name + "hello" + i);
                    createLogotype.setGroupId(groupIdList);// 分组标识
                    createLogotype.setGroupName(groupNameList);//声纹分组名称
                    createLogotype.setGroupInfo(groupInfoLsit);//分组描述信息
                    if (!name.equals("车主")) {
                        createOne = 1;
                        featureIdList.add(translateToEnglish(name) + "number" + i);
                        featureInfoList.add(name + "Num" + i);
                        createLogotype.setFeatureId(featureIdList);//特征唯一标识
                        createLogotype.setFeatureInfo(featureInfoList); //特征描述
                        group[0] = CreateGroup.doCreateGroup(requestUrl, APP_ID, APISecret, APIKey, createLogotype);//创建声纹特征库
                        if (group[0] != null) {
                            if (group[0].get("code").equals("0") && group[0].get("message").equals("success")) {
                                // 关闭弹窗
                                customDialog.dismiss();
                                if (isRecording) {
                                    try {
                                        mediaRecorder.stop();
                                        mediaRecorder.release();
                                        isRecording = false;
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                                runOnUiThread(() -> Toast.makeText(contexts, "注册声纹成功", Toast.LENGTH_SHORT).show());
//                                Intent intent = new Intent("com.yl.deepseekxunfei.cretemodule.CUSTOM_ACTION");
//                                intent.putExtra("key", creteFlies); // 可携带数据
//                                sendBroadcast(intent);
                            } else {
                                runOnUiThread(() -> Toast.makeText(contexts, "注册声纹失败，请重新注册", Toast.LENGTH_SHORT).show());
                            }
                        }
                    } else {
                        createOne = 1;
                        featureIdList.add(translateToEnglish(name) + "number" + i);
                        featureInfoList.add(name + i);
                        createLogotype.setFeatureId(featureIdList);//特征唯一标识
                        createLogotype.setFeatureInfo(featureInfoList); //特征描述
                        if (name.equals("车主") && che == 0) {
                            group[0] = CreateGroup.doCreateGroup(requestUrl, APP_ID, APISecret, APIKey, createLogotype);//创建声纹特征库
                            if (group[0] != null) {
                                if (group[0].get("code").equals("0") && group[0].get("message").equals("success")) {
                                    // 关闭弹窗
                                    customDialog.dismiss();
                                    if (isRecording) {
                                        try {
                                            mediaRecorder.stop();
                                            mediaRecorder.release();
                                            isRecording = false;
                                        } catch (IllegalStateException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    runOnUiThread(() -> Toast.makeText(contexts, "注册声纹成功", Toast.LENGTH_SHORT).show());
                                    che++;
                                    i++;
                                } else {
                                    runOnUiThread(() -> Toast.makeText(contexts, "注册声纹失败，请重新注册", Toast.LENGTH_SHORT).show());
                                }
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(contexts, "只能注册一个车主声纹", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                    CreateFeature.doCreateFeature(requestUrl, APP_ID, APISecret, APIKey, creteFlies, createLogotype);// 添加声纹特征
                    if (!name.equals("车主") && che != 0) {
                        i++;
                    }
                    creteUtlis.stopRecording();
                }
            });
            thread.start();
        } else {
            runOnUiThread(() -> Toast.makeText(contexts, "已有您的声纹信息，请不要重复注册", Toast.LENGTH_SHORT).show());
        }
    }

    public void cloneMIatDisalogs() {
        if (mIatDialog != null && mIatDialog.isShowing()) {
            mIatDialog.dismiss(); // 关闭对话框
        }
    }

    /**
     * 数据解析
     *
     * @param results
     */
    public void createVoiceprint(RecognizerResult results, boolean isLast) throws JSONException {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        // 只有最后一段才做最终判断
        if (isLast) {
            create();
        }
    }

    public static String translateToEnglish(String input) {
        // 定义一个简单的映射关系
        String[] chineseWords = {"车主", "家人", "朋友"};
        String[] englishWords = {"CarOwner", "FamilyMember", "Friend"};

        // 替换中文字符串为英文
        for (int j = 0; j < chineseWords.length; j++) {
            input = input.replace(chineseWords[j], englishWords[j]);
        }
        return input;
    }

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "初始化失败，错误码：\" + " + code + " + \",请联系开发人员解决方案\": ");
        }
    };
}
