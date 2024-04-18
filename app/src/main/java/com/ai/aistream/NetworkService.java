package com.ai.aistream;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @Author swxctx
 * @Date 2024-04-18
 * @Describe:
 */
public class NetworkService {
    // 默认超时时间
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    // 服务器地址
    private static final String API_BASE_UTL = "https://api.developer.icu";
    private OkHttpClient client;

    public NetworkService() {
        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 发起post请求
     * @param api
     * @param json
     * @param isStream
     * @param callback
     */
    public void post(String api, String json, boolean isStream, Callback callback) {
        long timeout = isStream ? 0 : 30;
        client = client.newBuilder().readTimeout(timeout, TimeUnit.SECONDS).build();

        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE_UTL + api)
                .post(body);

        if (isStream) {
            requestBuilder.header("Connection", "keep-alive");
        }

        client.newCall(requestBuilder.build()).enqueue(callback);
    }
}
