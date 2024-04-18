package com.ai.aistream;

/**
 * @Author swxctx
 * @Date 2024-04-18
 * @Describe:
 */
public class ChatRequestEntity {
    private int platform;
    private boolean stream;
    private String content;

    public ChatRequestEntity(int platform, boolean stream, String content) {
        this.platform = platform;
        this.stream = stream;
        this.content = content;
    }

    // Getter and Setter methods
    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatRequestEntity{" +
                "platform=" + platform +
                ", stream=" + stream +
                ", content='" + content + '\'' +
                '}';
    }


}
