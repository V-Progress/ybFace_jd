package com.yunbiao.ybsmartcheckin_live_id.common;

import android.content.Context;
import android.media.AudioManager;

import com.yunbiao.ybsmartcheckin_live_id.APP;


public class SoundControl {

    public static Integer CURRENT_SOUND = 0;

    public static void setMusicSound(Double volume) {
        AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//volume 0.0-1.0  volumD1-15
        Integer volumD = ((Double) (volume * max)).intValue();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumD, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
    }

    public static void stopCurrentVolume() {
        AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            CURRENT_SOUND =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }


    /**
     * 获取当前音量
     *
     * @return
     */
    public static int getCurrentVolume() {
        AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化
        int currentSound = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentSound;
    }

}
