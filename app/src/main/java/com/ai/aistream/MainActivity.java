package com.ai.aistream;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "AI_STREAM";

    private ProgressBar loading;
    private Switch stStream;
    private Spinner spPlatform;
    private EditText etInput;
    private TextView tvGen;
    private TextView tvClear;
    private PrinterTextView tvResult;

    /**
     * 业务数据
     */
    private ArrayAdapter<String> platformArrayAdapter;
    private int selectPlatform;
    private static String[] PLATFORM_DATA = {"文心一言", "讯飞星火", "智谱AI", "通义千问"};

    private NetworkService networkService;
    private static String GENERATE_API = "/example/v1/chat/do";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        bindListen();
    }

    private void initView() {
        loading = (ProgressBar) findViewById(R.id.loading);
        stStream = (Switch) findViewById(R.id.st_stream);
        spPlatform = (Spinner) findViewById(R.id.sp_platform);
        etInput = (EditText) findViewById(R.id.et_input);
        tvGen = (TextView) findViewById(R.id.tv_gen);
        tvClear = (TextView) findViewById(R.id.tv_clear);
        tvResult = (PrinterTextView) findViewById(R.id.tv_result);
    }

    private void initData() {
        // 初始化网络请求库
        networkService = new NetworkService();

        // 选择框
        platformArrayAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.item_spinner, PLATFORM_DATA);
        spPlatform.setAdapter(platformArrayAdapter);
        spPlatform.setSelection(1);
    }

    private void bindListen() {
        spPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPlatform = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tvGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用AI生成结果
                showLoading();
                tvResult.setText("");
                generate();
            }
        });

        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etInput.setText("");
                tvResult.setText("");
            }
        });

        tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取剪贴板管理器
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建ClipData对象
                ClipData clip = ClipData.newPlainText("label", tvResult.getText().toString());
                // 设置剪贴板的主剪贴数据
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 生成方法
     */
    private void generate() {
        ChatRequestEntity request = new ChatRequestEntity(selectPlatform, stStream.isChecked(), etInput.getText().toString());
        Gson gson = new Gson();
        String json = gson.toJson(request);
        Log.d(TAG, "generate: params-> " + json);

        // 发起生成 请求
        networkService.post(GENERATE_API, json, request.isStream(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                hideLoading();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                hideLoading();
                if (response.isSuccessful()) {
                    // 读取响应
                    readResponse(response);
                } else {
                    Log.e(TAG, "Request failed with code: " + response.code());
                }
            }
        });
    }

    /**
     * 处理响应
     *
     * @param response
     * @throws IOException
     */
    private void readResponse(Response response) throws IOException {
        // 获取输入流
        InputStream inputStream = response.body().byteStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                final String messageLine = line;
                Log.d(TAG, "readResponse: line-> " + line);

                JSONObject jsonObject = new JSONObject(messageLine);
                String message = jsonObject.getString("message");

                // 检查是否是结束信号
                if (message.equals("[DONE]")) {
                    // 如果收到结束信号，退出循环
                    break;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.receiveText(message);
                    }
                });
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            // 确保关闭流
            inputStream.close();
        }
    }
}