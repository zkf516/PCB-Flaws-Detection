package com.example.pcb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import okhttp3.*;
import org.json.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import android.app.DatePickerDialog;

// 主页
public class MainActivity extends AppCompatActivity {
    // UI
    private LinearLayout page0, page1, page2;
    private LinearLayout tabHome, tabDetect, tabHistory;
    private TextView tabHomeText, tabDetectText, tabHistoryText;
    private ImageView tabHomeIcon, tabDetectIcon, tabHistoryIcon;
    private ImageView lastImage, fullScreenImage;
    private ProgressBar loadingProgress;
    private RecyclerView recentImagesRecycler, historyList;
    private FrameLayout fullScreenContainer;
    private Button takePhotoBtn, page2Button;
    private EditText page2Input;

    // 数据
    private List<String> recentImages = new ArrayList<>();
    private int currentIndex = 0;
    private String lastImageUrl = "";
    private boolean isLoading = false;
    private int percent = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)   // 连接超时
            .writeTimeout(30, TimeUnit.SECONDS)     // 写入超时
            .readTimeout(60, TimeUnit.SECONDS)      // ✅ 读取超时调大
            .build();
    private WebSocket webSocket;
    private int dialogShow = 0;
    private String realTimeImageUrl = "";
    private HistoryAdapter historyAdapter;
    private RecentImagesAdapter recentImagesAdapter;

    private Uri photoUri;
    private File photoFile;

    // 日期
    private String selectedDate = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()); // 默认今天

    // 权限
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_TAKE_PHOTO = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        // 获取状态栏高度
//        int statusBarHeight = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
//        }
//
//        // 为内容区加 paddingTop，防止被状态栏遮挡
//        View tabContent = findViewById(R.id.tabContent);
//        if (tabContent != null) {
//            tabContent.setPadding(
//                    tabContent.getPaddingLeft(),
//                    statusBarHeight,
//                    tabContent.getPaddingRight(),
//                    tabContent.getPaddingBottom()
//            );
//        }
        // 1. 绑定控件
        bindViews();

        // 2. Tab切换
        setupTabs();

        // 3. 图片轮播
        setupRecentImages();

        // 4. 历史记录列表
        setupHistoryList();

        // 5. 拍照按钮
        takePhotoBtn.setOnClickListener(v -> checkCameraPermissionAndTakePhoto());

        // 6. 全屏图片关闭
        fullScreenContainer.setOnClickListener(v -> fullScreenContainer.setVisibility(View.GONE));

        // 7. 历史记录查询
        getAllHistories();
        page2Button.setOnClickListener(v -> getAllHistories());

        // 8. 日期选择
        page2Input.setOnClickListener(v -> showDatePicker());

        // 9. 启动进度条和WebSocket
        startLoading();
        flawsDetection();
    }

    private void bindViews() {
        page0 = findViewById(R.id.Page0);
        page1 = findViewById(R.id.Page1);
        page2 = findViewById(R.id.Page2);
        tabHome = findViewById(R.id.tabHome);
        tabDetect = findViewById(R.id.tabDetect);
        tabHistory = findViewById(R.id.tabHistory);
        tabHomeText = findViewById(R.id.tabHomeText);
        tabDetectText = findViewById(R.id.tabDetectText);
        tabHistoryText = findViewById(R.id.tabHistoryText);
        tabHomeIcon = findViewById(R.id.tabHomeIcon);
        tabDetectIcon = findViewById(R.id.tabDetectIcon);
        tabHistoryIcon = findViewById(R.id.tabHistoryIcon);
        lastImage = findViewById(R.id.lastImage);
        loadingProgress = findViewById(R.id.loadingProgress);
        recentImagesRecycler = findViewById(R.id.recentImagesRecycler);
        fullScreenContainer = findViewById(R.id.fullScreenContainer);
        fullScreenImage = findViewById(R.id.fullScreenImage);
        takePhotoBtn = findViewById(R.id.takePhotoBtn);
        historyList = findViewById(R.id.historyList);
        page2Button = findViewById(R.id.Page2Button);
        page2Input = findViewById(R.id.Page2Input);
    }

    // 页面切换
    private void setupTabs() {
        tabHome.setOnClickListener(v -> switchPages(0));
        tabDetect.setOnClickListener(v -> switchPages(1));
        tabHistory.setOnClickListener(v -> switchPages(2));
        switchPages(0); // 默认首页
    }

    private void switchPages(int page) {
        page0.setVisibility(page == 0 ? View.VISIBLE : View.GONE);
        page1.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        page2.setVisibility(page == 2 ? View.VISIBLE : View.GONE);

        tabHomeText.setTextColor(getResources().getColor(page == 0 ? R.color.selected_color : R.color.tab_unselected));
        tabDetectText.setTextColor(getResources().getColor(page == 1 ? R.color.selected_color : R.color.tab_unselected));
        tabHistoryText.setTextColor(getResources().getColor(page == 2 ? R.color.selected_color : R.color.tab_unselected));
        // 可切换icon资源
    }


    // 主页
    private void setupRecentImages() {
        recentImagesAdapter = new RecentImagesAdapter(recentImages, image -> showFullScreen(image));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recentImagesRecycler.setLayoutManager(layoutManager);
        recentImagesRecycler.setAdapter(recentImagesAdapter);
    }

    private void showFullScreen(String imageUrl) {
        fullScreenContainer.setVisibility(View.VISIBLE);
        Glide.with(this).load(imageUrl).into(fullScreenImage);
    }

    // 进度条动画
    private void startLoading() {
        isLoading = true;
        percent = 0;
        loadingProgress.setVisibility(View.VISIBLE);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isLoading) {
                    percent = (percent + 1) % 101;
                    loadingProgress.setProgress(percent);
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    private void stopLoading() {
        isLoading = false;
        percent = 0;
        loadingProgress.setProgress(0);
        loadingProgress.setVisibility(View.GONE);
        handler.removeCallbacksAndMessages(null);
    }


    // 拍照界面
    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = createImageFile();
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
    }

    // 创建图片文件
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && photoFile != null && photoFile.exists()) {
            uploadImage(photoFile);
        }
    }

    // 上传原图文件
    private void uploadImage(File imageFile) {
        startLoading(); // 显示进度条

        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/jpg"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://10.21.207.212:5001/api/v1/inference?filename=" + imageFile.getName())
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> stopLoading());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> stopLoading());
                try {
                    JSONObject obj = new JSONObject(result);
                    if (!obj.optBoolean("success", false)) {    // 2. 后端返回 success=false
                        String err = obj.optString("error_msg", "未知错误");
                        Toast.makeText(MainActivity.this, "服务器处理失败：" + err, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    int detectedFlaws = obj.getJSONObject("result").getJSONArray("detection_classes").length();
                    runOnUiThread(() -> showFlawDialog(detectedFlaws));

                    String lastImageID = obj.getString("filename");
                    lastImageUrl = "http://10.21.207.212:5001/images/" + lastImageID;
                    runOnUiThread(() -> {
                        Glide.with(MainActivity.this).load(lastImageUrl).into(lastImage);
                        recentImages.add(0, lastImageUrl);
                        if (recentImages.size() > 5) recentImages.remove(recentImages.size() - 1);
                        recentImagesAdapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "返回错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // WebSocket实时检测
    private void flawsDetection() {
        Request request = new Request.Builder()
                .url("ws://10.21.207.212:5002/66b8276ed8c5af5f58f200d3_PCB_ON_BELT")
                .build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject res = new JSONObject(text);
                    int totalFlaws = res.optInt("total_flaws_detected");
                    String streamingUrl = res.optString("streaming_url");
                    runOnUiThread(() -> {
                        if (totalFlaws != dialogShow) {
                            showFlawDialog(totalFlaws);
                            dialogShow = totalFlaws;
                        }
                        realTimeImageUrl = streamingUrl;
                        // Glide.with(MainActivity.this).load(realTimeImageUrl).into(realTimeImageView);
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void showFlawDialog(int totalFlaws) {
        new AlertDialog.Builder(this)
                .setTitle("检测到新的异常pcb板!")
                .setMessage("当前异常数：" + totalFlaws)
                .setPositiveButton("确定", null)
                .show();
    }



    // 历史记录查询
    private void setupHistoryList() {
        historyAdapter = new HistoryAdapter(new ArrayList<>(), this::onHistoryItemClick);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        historyList.setAdapter(historyAdapter);
    }

    private void onHistoryItemClick(String imageName) {
        // 跳转到详情页
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("imageName", imageName);
        startActivity(intent);
    }

    private void getAllHistories() {
        String url = "http://10.21.207.212:5001/api/v1/get_all_histories?page=1&limit=50&date=" + selectedDate;
        Request request = new Request.Builder().url(url).get().build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("History", "Network error", e);   // ← 关键日志
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //if (!response.isSuccessful()) return;
                String result = response.body().string();
                Log.d("History", "raw=" + result);
                try {
                    JSONObject obj = new JSONObject(result);
                    JSONArray arr = obj.getJSONArray("result");
                    List<HistoryItem> items = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        items.add(new HistoryItem(
                                i + 1,
                                item.getString("image_id"),
                                item.getInt("detected_flaws")
                        ));
                    }
                    runOnUiThread(() -> historyAdapter.updateData(items));
                } catch (Exception e) {
                    Log.e("History", "Parse error", e);
                    e.printStackTrace();
                }
            }
        });
    }

    // 日期选择
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    page2Input.setText(dateStr);
                    selectedDate = String.format(Locale.CHINA, "%04d%02d%02d", year, month + 1, dayOfMonth);
                    getAllHistories();
                    // 可自动触发历史记录查询
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) webSocket.close(1000, null);
        handler.removeCallbacksAndMessages(null);
    }
}