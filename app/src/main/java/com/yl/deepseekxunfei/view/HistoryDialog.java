package com.yl.deepseekxunfei.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.adapter.HistoryRecyAdapter;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;
import com.yl.ylcommon.utlis.DeleteDialogHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.http2.Header;

public class HistoryDialog extends Dialog implements View.OnClickListener, HistoryRecyAdapter.onButtonClick {

    private RecyclerView recyclerView;
    private Button customDialogBtn;
    private HistoryRecyAdapter historyRecyAdapter;
    private List<ChatHistoryEntity> mData;
    private onDialogDataBack onDialogDataBack;
    private TextView noHistoryTips;
    private MainActivity mainActivity;

    public HistoryDialog(@NonNull Context context, List<ChatHistoryEntity> data,MainActivity mainActivity) {
        super(context, R.style.CustomDialog);
        if (data != null) {
            mData = data;
        } else {
            mData = new ArrayList<>();
        }
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_dialog_layout);
        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.custom_dialog_recy);
        customDialogBtn = findViewById(R.id.custom_dialog_btn);
        noHistoryTips = findViewById(R.id.no_history_tips);
        if (!mData.isEmpty()) {
            noHistoryTips.setVisibility(View.GONE);
        } else {
            noHistoryTips.setVisibility(View.VISIBLE);
        }
        customDialogBtn.setOnClickListener(this);
        historyRecyAdapter = new HistoryRecyAdapter(mData, getContext());
        historyRecyAdapter.setOnButtonClick(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyRecyAdapter);
    }
    //历史记录确定按钮
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.custom_dialog_btn) {
            mainActivity.handleSendButtonClick();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mData.isEmpty()) {
                        ChatHistoryEntity chatHistoryEntity = mData.get(historyRecyAdapter.getClickPosition());
                        if (onDialogDataBack != null) {
                            onDialogDataBack.dataBack(chatHistoryEntity);
                        }
                    }
                    dismiss();
                    mainActivity.getChatMessages().get(mainActivity.getChatMessagesSizeIndex()).setOver(true);
                }
            },200);

        }
    }

    public void setOnDialogDataBack(onDialogDataBack onDialogDataBack) {
        this.onDialogDataBack = onDialogDataBack;
    }

    @Override
    public void onRenameBtnClick(ChatHistoryEntity chatHistoryEntity, int position) {

    }

    @Override
    public void onDeleteBtnClick(ChatHistoryEntity chatHistoryEntity, int position) {
        confirmDelete(chatHistoryEntity, position);
    }

    public interface onDialogDataBack {
        void dataBack(ChatHistoryEntity chatHistoryEntity);
    }


    private void confirmDelete(ChatHistoryEntity chatHistoryEntity, int position) {
        // 在Activity中调用
        DeleteDialogHelper.showDeleteConfirmationDialog(
                getContext(),
                "永久删除对话",
                "删除后，该对话将不可恢复。确认删除吗？",
                ContextCompat.getDrawable(getContext(), R.drawable.free1),
                new DeleteDialogHelper.DeleteDialogListener() {
                    @Override
                    public void onDeleteConfirmed() {
                        // 执行删除操作
                        AppDatabase.Delete(chatHistoryEntity);
                        mData.remove(position);
                        historyRecyAdapter.notifyItemRemoved(position);
                        if (mData.isEmpty()) {
                            noHistoryTips.setVisibility(View.VISIBLE);
                        } else {
                            noHistoryTips.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onDeleteCancelled() {
                    }
                }
        );
    }

}
