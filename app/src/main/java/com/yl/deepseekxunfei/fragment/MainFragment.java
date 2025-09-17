package com.yl.deepseekxunfei.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.R;

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
                view.findViewById(R.id.navigate_gas), view.findViewById(R.id.navigate_world_window), view.findViewById(R.id.play_music),
                view.findViewById(R.id.recent_movies), view.findViewById(R.id.today_weather), view.findViewById(R.id.beijing_weather)
        };
        for (TextView textView : textViews) {
            textView.setOnClickListener(v -> {
                ((MainActivity) getActivity()).commitText(((TextView) v).getText().toString());
            });
        }
    }
}
