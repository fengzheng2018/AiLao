package com.android.ailao.voice;

public class VoiceItem {
    private String voiceUri;
    private String voiceTime;

    public VoiceItem(String voiceUri, String voiceTime) {
        this.voiceUri = voiceUri;
        this.voiceTime = voiceTime;
    }

    public String getVoiceUri() {
        return voiceUri;
    }

    public String getVoiceTime() {
        return voiceTime;
    }
}
