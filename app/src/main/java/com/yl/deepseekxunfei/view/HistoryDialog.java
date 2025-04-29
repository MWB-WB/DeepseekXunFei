package com.yl.deepseekxunfei.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.adapter.HistoryRecyAdapter;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;

import java.util.List;

public class HistoryDialog extends Dialog implements View.OnClickListener {

    private RecyclerView recyclerView;
    private Button customDialogBtn;
    private HistoryRecyAdapter historyRecyAdapter;
    private List<ChatHistoryEntity> mData;

    public HistoryDialog(@NonNull Context context, List<ChatHistoryEntity> data) {
        super(context, R.style.CustomDialog);
        mData = data;
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
        customDialogBtn.setOnClickListener(this);
        historyRecyAdapter = new HistoryRecyAdapter(mData, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyRecyAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.custom_dialog_btn) {
            ChatHistoryEntity chatHistoryEntity = mData.get(historyRecyAdapter.getClickPosition());

        }
    }
}
