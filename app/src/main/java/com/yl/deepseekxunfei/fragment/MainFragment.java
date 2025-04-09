package com.yl.deepseekxunfei.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yl.deepseekxunfei.MainActivity;
import com.yl.deepseekxunfei.R;

import org.w3c.dom.Text;

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        // 推荐问题点击区域
        TextView[] textViews = {
                view.findViewById(R.id.t1), view.findViewById(R.id.t2), view.findViewById(R.id.t3),
                view.findViewById(R.id.t4), view.findViewById(R.id.t5), view.findViewById(R.id.t6),
                view.findViewById(R.id.t7), view.findViewById(R.id.t8), view.findViewById(R.id.t9),
                view.findViewById(R.id.t10), view.findViewById(R.id.t11), view.findViewById(R.id.t12)
        };
        for (TextView textView : textViews) {
            textView.setOnClickListener(v -> {
                ((MainActivity) getActivity()).commitText(((TextView) v).getText().toString());
            });
        }
    }

}
