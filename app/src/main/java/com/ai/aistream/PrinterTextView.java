package com.ai.aistream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author swxctx
 * @Date 2024-04-18
 * @Describe:
 */
public class PrinterTextView extends androidx.appcompat.widget.AppCompatTextView {
    /**
     * TAG
     */
    private static final String TAG = "PrinterTextView";
    /**
     * 默认打字字符
     */
    private final String DEFAULT_INTERVAL_CHAR = "_";
    /**
     * 默认打字间隔时间
     */
    private final int DEFAULT_TIME_DELAY = 80;
    /**
     * 计时器
     */
    private Timer mTimer;
    /**
     * 需要打字的文字
     */
    private String mPrintStr;
    /**
     * 间隔时间
     */
    private int intervalTime = DEFAULT_TIME_DELAY;
    /**
     * 间隔时间
     */
    private String intervalChar = DEFAULT_INTERVAL_CHAR;
    // 用于流式处理
    private Queue<Character> charQueue = new LinkedList<>();


    public PrinterTextView(Context context) {
        super(context);
    }

    public PrinterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrinterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 设置要打字的文字
     *
     * @param str
     */
    public void setPrintText(String str) {
        setPrintText(str, DEFAULT_TIME_DELAY);
    }

    /**
     * 设置需要打字的文字及打字间隔
     *
     * @param str  打字文字
     * @param time 打字间隔(ms)
     */
    public void setPrintText(String str, int time) {
        setPrintText(str, time, DEFAULT_INTERVAL_CHAR);
    }

    /**
     * 设置需要打字的文字,打字间隔,间隔符号
     *
     * @param str          打字文字
     * @param time         打字间隔(ms)
     * @param intervalChar 间隔符号("_")
     */
    public void setPrintText(String str, int time, String intervalChar) {
        if (strIsEmpty(str) || 0 == time || strIsEmpty(intervalChar)) {
            return;
        }
        this.mPrintStr = str;
        this.intervalTime = time;
        this.intervalChar = intervalChar;
    }

    /**
     * 开始打字
     */
    public void startPrint() {
        // 判空处理
        if (strIsEmpty(mPrintStr)) {
            if (!strIsEmpty(getText().toString())) {
                this.mPrintStr = getText().toString();
            } else {
                return;
            }
        }
        // 重置相关信息
        setText("");
        stopPrint();
        // 开始打字处理
        receiveText(mPrintStr);
    }

    /**
     * 判断str是否为空
     *
     * @param str
     * @return
     */
    private boolean strIsEmpty(String str) {
        if (null != str && !"".equals(str)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 接收新文本并开始打字效果。
     * @param newText 新文本
     */
    public void receiveText(String newText) {
        for (char ch : newText.toCharArray()) {
            charQueue.offer(ch);
        }
        startPrintIfNeeded();
    }

    /**
     * 如果没有正在打字，则开始打字。
     */
    private void startPrintIfNeeded() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new PrinterTimeTask(), 0, intervalTime);
        }
    }

    /**
     * 停止打字。
     */
    public void stopPrint() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 打字计时器任务
     */
    private class PrinterTimeTask extends TimerTask {
        @Override
        public void run() {
            post(() -> {
                if (!charQueue.isEmpty()) {
                    setText(getText().toString() + charQueue.poll());
                } else {
                    stopPrint();  // 停止计时器如果没有更多字符显示
                }
            });
        }
    }
}
