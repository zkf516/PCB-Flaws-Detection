package com.example.pcb;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import okhttp3.*;

public class DetailActivity extends AppCompatActivity {
    private String imageId;
    private String imageUrl;
    private final String getOneImageHistory = "http://10.21.207.212:5001/api/v1/get_one_history";
    private TextView detailIdText;
    private ImageView imageView;
    private ListView classListView;
    private ProgressBar progressBar;
    private final List<String> classStats = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailIdText = findViewById(R.id.detailIdText);
        imageView = findViewById(R.id.detailImage);
        classListView = findViewById(R.id.classListView);
        progressBar = findViewById(R.id.progressBar);

        // 获取 imageId
        imageId = getIntent().getStringExtra("imageName");
        if (imageId == null) imageId = "";
        imageUrl = "http://10.21.207.212:5001/images/" + imageId;

        // 加载图片
        Glide.with(this).load(imageUrl).into(imageView);

        // 初始化列表
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classStats);
        classListView.setAdapter(adapter);

        // 获取检测历史
        getOneHistory();
    }

    private void getOneHistory() {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = getOneImageHistory + "?image_id=" + URLEncoder.encode(imageId);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                if (!response.isSuccessful()) return;
                try {
                    String result = response.body().string();
                    JSONObject parsedResult = new JSONObject(result);
                    String detailId = parsedResult.getJSONObject("result").getString("image_id");
                    JSONArray detectionClasses = parsedResult.getJSONObject("result")
                            .getJSONObject("inference_result")
                            .getJSONObject("result")
                            .getJSONArray("detection_classes");
                    int totalCount = detectionClasses.length();
                    Map<String, Integer> classCount = new HashMap<>();
                    for (int i = 0; i < detectionClasses.length(); i++) {
                        String className = detectionClasses.getString(i);
                        classCount.put(className, classCount.getOrDefault(className, 0) + 1);
                    }
                    List<String> stats = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : classCount.entrySet()) {
                        int percent = Math.round(entry.getValue() * 100f / totalCount);
                        stats.add(entry.getKey() + ": " + percent + "%");
                    }
                    runOnUiThread(() -> {
                        detailIdText.setText("图片ID: " + detailId);
                        classStats.clear();
                        classStats.addAll(stats);
                        adapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(DetailActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}