package com.yunbiao.ybsmartcheckin_live_id.temper_5inch;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.printer.T;

import java.io.IOException;
import java.util.Locale;

public class SpeechManager {

    private static SpeechManager instance;

    private SoundPool mSoundPool;

    private static AudioManager audioManager = null;
    private static TextToSpeech mTextToSpeech;//语音类
    private float mSpeed = 1.8f;
    private boolean isInit = false;
    private int mTTSSupport = 0;
    //    private static HashMap<String, String> textToSpeechMap = new HashMap<>();
    private static Bundle textToSpeechBundle = new Bundle();

    private int mPlayId = -1;

    private final int mWarningRingId;
    private final int mPassId;
    private final int mDingDongId;

    public static long lastSpeechTime = 0;

    public static SpeechManager getInstance() {
        if (instance == null) {
            synchronized (SpeechManager.class) {
                if (instance == null) {
                    instance = new SpeechManager();
                }
            }
        }
        return instance;
    }

    private SpeechManager() {
        AudioAttributes.Builder ab = new AudioAttributes.Builder();
        ab.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        ab.setUsage(AudioAttributes.USAGE_MEDIA);
        mSoundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(ab.build()).build();
        mWarningRingId = mSoundPool.load(APP.getContext(), R.raw.warning_ring, 1);
        mPassId = mSoundPool.load(APP.getContext(), R.raw.pass, 1);
        mDingDongId = mSoundPool.load(APP.getContext(),R.raw.dingdong,1);
    }

    public void init() {
        audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (!isInit) {
            try {
                mTextToSpeech = new TextToSpeech(APP.getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            isInit = true;
                            // 设置朗读语言
                            Locale locale = APP.getContext().getResources().getConfiguration().locale;
                            mTTSSupport = mTextToSpeech.setLanguage(locale);

                            if (mTTSSupport == TextToSpeech.LANG_NOT_SUPPORTED) {
                                T.showShort(APP.getContext(), "not_support_speech");
                            }  else if (mTTSSupport == TextToSpeech.LANG_MISSING_DATA) {
                                T.showShort(APP.getContext(), "miss_speech_data");
                            } else {
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playNormal(final String message, boolean tag) {
        playNormalAddCallback(TextToSpeech.QUEUE_ADD, message, tag, null);
    }

    public void playNormal(final String message, boolean tag, Runnable runnable) {
        playNormalAddCallback(TextToSpeech.QUEUE_FLUSH, message, tag, runnable);
    }

    private void playNormalAddCallback(int queueMode, final String message, boolean tag, final Runnable runnable) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE) && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            if (tag) {
                playPassRing();
            }
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        if (TextUtils.isEmpty(message)) {
            return;
        }

        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {}
            @Override
            public void onDone(String s) {
                if (runnable != null) {
                    runnable.run();
                }
                lastSpeechTime = System.currentTimeMillis();
            }
            @Override
            public void onError(String s) {}
        });
        mTextToSpeech.setSpeechRate(mSpeed);
        textToSpeechBundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
        mTextToSpeech.speak(message, queueMode, textToSpeechBundle, message);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            textToSpeechBundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
//            mTextToSpeech.speak(message, queueMode, textToSpeechBundle, message);
//        } else {
//            textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
//            textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
//            mTextToSpeech.speak(message, queueMode, textToSpeechMap);
//        }
    }

    public void stopNormal() {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE) && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            return;
        }
        if (!mTextToSpeech.isSpeaking()) {
            return;
        }
        mTextToSpeech.stop();
    }

    private int getAudioDuration(int rawId) {
        int mediaPlayerDuration = 0;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            Uri uri = Uri.parse("android.resource://" + APP.getContext().getPackageName() + "/" + rawId);
            mediaPlayer.setDataSource(APP.getContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayerDuration = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return mediaPlayerDuration;
    }

    public void setSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
    }

    public void playDingDong(){
        if(mSoundPool == null){
            return;
        }
        mPlayId = mSoundPool.play(mDingDongId,1,1,1,0,1);
    }

    public void playPassRing() {
        if(mSoundPool == null){
            return;
        }
        mPlayId = mSoundPool.play(mPassId, 1, 1, 1, 0, 1);
    }

    public void playWaningRing() {
        if(mSoundPool == null){
            return;
        }
        mPlayId = mSoundPool.play(mWarningRingId, 1, 1, 1, 0, 1);
    }

    public void stopCurrent() {
        if (mPlayId != -1 && mSoundPool != null) {
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
            isInit = false;
        }
    }

}
