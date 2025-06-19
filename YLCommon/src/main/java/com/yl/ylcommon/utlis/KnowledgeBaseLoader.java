package com.yl.ylcommon.utlis;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseLoader {
    public static List<KnowledgeEntry> loadKnowledgeBase(Context context) {
        List<KnowledgeEntry> knowledgeEntries = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("knowledge_base.json")))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String question = jsonObject.getString("question");
                String answer = jsonObject.getString("answer");
                knowledgeEntries.add(new KnowledgeEntry(question, answer));
            }
        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return knowledgeEntries;
    }
}