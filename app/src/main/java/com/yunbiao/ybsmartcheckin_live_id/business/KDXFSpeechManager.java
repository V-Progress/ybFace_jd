package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.HandleMessageUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by Administrator on 2019/3/16.
 */

public class KDXFSpeechManager {
    private static final String TAG = "KDXFSpeechManager";

    private static KDXFSpeechManager instance;
    private static AudioManager audioManager = null; // 音频
    private static TextToSpeech mTextToSpeech;//语音类
    private final int mDingDongId;
    private boolean isInited = false;
    private static HashMap<String, String> textToSpeechMap = new HashMap<>();
    private int mTTSSupport = 0;
    private boolean soundPoolLoadCompleted = false;
    private SoundPool mSoundPool;
    private int mVoiceId = -1;
    private int mPlayId = -1;
    private int mPassId = -1;
    private int mPlayPassId = -1;
    private float mSpeed = 2.5f;

    public void setSpeed(float speed){
        mSpeed = speed;
    }

    public static KDXFSpeechManager instance() {
        if (instance == null) {
            synchronized (KDXFSpeechManager.class) {
                if (instance == null) {
                    instance = new KDXFSpeechManager();
                }
            }
        }
        return instance;
    }

    private String mCurrentLanguage = "";
    private int mNormalSpeech = -1;
    private int mWarningSpeech = -1;

    private KDXFSpeechManager() {
        AudioAttributes.Builder ab = new AudioAttributes.Builder();
        ab.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        ab.setUsage(AudioAttributes.USAGE_MEDIA);
        mSoundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(ab.build()).build();
        mVoiceId = mSoundPool.load(APP.getContext(), R.raw.warning_ring, 1);
        mPassId = mSoundPool.load(APP.getContext(), R.raw.pass, 1);
        mDingDongId = mSoundPool.load(APP.getContext(),R.raw.dingdong,1);

        if(TextUtils.equals("sl",getCurrentLanguage())){
            mNormalSpeech = mSoundPool.load(APP.getContext(),R.raw.normal_speech,1);
            mWarningSpeech = mSoundPool.load(APP.getContext(),R.raw.warning_speech,1);
        }

        mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPoolLoadCompleted = true);
    }

    public String getCurrentLanguage(){
        if(TextUtils.isEmpty(mCurrentLanguage)){
            mCurrentLanguage = Locale.getDefault().getLanguage();
        }
        return mCurrentLanguage;
    }

    public void playNormalSound(){
        if(mSoundPool == null || mNormalSpeech == -1){
            return;
        }
        mSoundPool.play(mNormalSpeech, 1, 1, 1, 0, 1);
    }

    public void playWarningSound(){
        if(mSoundPool == null || mWarningSpeech == -1){
            return;
        }
        mSoundPool.play(mWarningSpeech, 1, 1, 1, 0, 1);
    }

    private MediaPlayer mediaPlayer;
    private boolean isApprochSoundPlaying = false;
    public void playApprochSound(Runnable runnable){
        if(isApprochSoundPlaying){
            return;
        }
        isApprochSoundPlaying = true;
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(APP.getContext(), R.raw.please_approch);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            isApprochSoundPlaying = false;
            if(runnable != null){
                runnable.run();
            }
        });
    }

    public void init(final Activity context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (!isInited) {
            try {
                mTextToSpeech = new TextToSpeech(context,
                        status -> {
                            if (status == TextToSpeech.SUCCESS) {
                                isInited = true;
                                // 设置朗读语言
                                Locale locale = APP.getContext().getResources().getConfiguration().locale;
                                mTTSSupport = mTextToSpeech.setLanguage(locale);

                                if (mTTSSupport == TextToSpeech.LANG_NOT_SUPPORTED) {
                                    String notSupport = APP.getContext().getResources().getString(R.string.not_support_speech);
                                    Toast.makeText(context, notSupport + locale.getCountry(), Toast.LENGTH_LONG).show();
                                } else if (mTTSSupport == TextToSpeech.LANG_MISSING_DATA) {
                                    String missData = APP.getContext().getResources().getString(R.string.miss_speech_data);
                                    Toast.makeText(context, missData + locale.getCountry(), Toast.LENGTH_LONG).show();
                                } else {
                                    welcome();
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 播放欢迎语
     */
    public void welcome() {
        String welcomeTips = SpUtils.getStr(SpUtils.WELCOM_TIPS, APP.getContext().getResources().getString(R.string.setting_default_welcome_tip));
        if (Constants.DEVICE_TYPE == Constants.DeviceType.MULTIPLE_THERMAL) {
            welcomeTips = APP.getContext().getResources().getString(R.string.setting_default_welcome_tip4);
            mSpeed = 2.0f;
        }
        if (TextUtils.isEmpty(welcomeTips)) {
            return;
        }
        playNormal(welcomeTips);
    }

    public void playNormal(final String message) {
        playNormalAddCallback(TextToSpeech.QUEUE_ADD, message, null);
    }

    public void playNormal(final String message, Runnable runnable) {
        playNormalAddCallback(TextToSpeech.QUEUE_FLUSH, message, runnable);
    }

    public void playNormalAdd(String message,Runnable runnable){
        playNormalAddCallback(TextToSpeech.QUEUE_ADD,message,runnable);
    }

    private String mMessage;

    private void playNormalAddCallback(int queueMode, final String message, final Runnable runnable) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
//            playPassRing();
            Log.e(TAG, "playNormalAddCallback: 当前语言不支持：" + getCurrentLanguage());
            return;
        }

        if (TextUtils.isEmpty(message)) {
            return;
        }

        if (mTextToSpeech.isSpeaking() && TextUtils.equals(message, mMessage)) {
            return;
        } else {
            mMessage = message;
        }
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        mTextToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        mTextToSpeech.setSpeechRate(mSpeed);
        mTextToSpeech.speak(message, queueMode, textToSpeechMap);
    }
    public void playNormalForSpeed(final String message,float speed) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            playPassRing();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            return;
        }

        if (mTextToSpeech.isSpeaking() && TextUtils.equals(message, mMessage)) {
            return;
        } else {
            mMessage = message;
        }
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        mTextToSpeech.setSpeechRate(speed);
        mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD, textToSpeechMap);
    }

    public void playNormalNoCheck(String mMessage, Runnable runnable) {
        playNormalNoCheck(TextToSpeech.QUEUE_FLUSH, mMessage, runnable);
    }

    public void playNormalNoCheck(int queueMode, String message, final Runnable runnable) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            playPassRing();
            return;
        }
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mTextToSpeech.setSpeechRate(mSpeed);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        mTextToSpeech.setOnUtteranceCompletedListener(utteranceId -> {
            if (runnable != null) {
                runnable.run();
            }
        });
        mTextToSpeech.speak(message, queueMode, textToSpeechMap);
    }

    public void stopNormal() {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            return;
        }
        if (!mTextToSpeech.isSpeaking()) {
            return;
        }
        mTextToSpeech.stop();
    }

    public void playDingDong(){
        if(mSoundPool == null){
            return;
        }
        mSoundPool.play(mDingDongId,1,1,1,0,1);
    }

    public void playPassRing() {
        /*stopPassRing();
        if (soundPoolLoadCompleted && mPassId != -1) {
            mPlayPassId = mSoundPool.play(mPassId, 1, 1, 1, 0, 1);
        }*/
        if(mSoundPool == null){
            return;
        }
        mSoundPool.play(mPassId, 1, 1, 1, 0, 1);
    }

    public void stopPassRing() {
        if (mPlayPassId != -1) {
            mSoundPool.stop(mPlayPassId);
            mPlayPassId = -1;
        }
    }

    public void playWaningRing() {
        /*stopWarningRing();
        if (soundPoolLoadCompleted && mVoiceId != -1) {
            mPlayId = mSoundPool.play(mVoiceId, 1, 1, 1, 0, 1);
        }*/
        if(mSoundPool == null){
            return;
        }
        mSoundPool.play(mVoiceId, 1, 1, 1, 0, 1);
    }

    public void playWaningRingNoStop() {
        /*if (soundPoolLoadCompleted && mVoiceId != -1) {
            mPlayId = mSoundPool.play(mVoiceId, 1, 1, 1, 0, 1);
        }*/
        if(mSoundPool == null){
            return;
        }
        mSoundPool.play(mVoiceId, 1, 1, 1, 0, 1);
    }

    public void stopWarningRing() {
        if (mPlayId != -1) {
            mSoundPool.stop(mPlayId);
            mPlayId = -1;
        }
    }

    public void destroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        if (mTextToSpeech != null) {
            //停止TextToSpeech
            mTextToSpeech.stop();
            //释放TextToSpeech占用的资源
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
            isInited = false;
        }
    }
}
