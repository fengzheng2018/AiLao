package com.android.ailao.voice;

import android.os.SystemClock;
import android.widget.Chronometer;

public class Timer {
    private Chronometer chronometer;
    private long pauseTime;

    public Timer(Chronometer chronometer) {
        this.chronometer = chronometer;
        pauseTime = 0;
    }

    /**
     * 开始计时
     */
    public void timerStart(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /**
     * 暂停计时
     */
    public void timerPause(){
        chronometer.stop();
        pauseTime = SystemClock.elapsedRealtime();
    }

    /**
     * 恢复计时
     */
    public void timerResume(){
        chronometer.setBase(chronometer.getBase()
            + (SystemClock.elapsedRealtime() - pauseTime));
        chronometer.start();
    }

    /**
     * 停止计时
     */
    public void timerStop(){
        chronometer.stop();
    }

    /**
     * 重新开始
     */
    public void timerReStart(){
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }
}
