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

import com.yunbiao.ybsmartcheckin_live_id.APP;
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
    private boolean isInited = false;
    private static HashMap<String, String> textToSpeechMap = new HashMap<>();
    private static List<String> ontimeList = new ArrayList<>();
    private int mTTSSupport = 0;

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

    private boolean soundPoolLoadCompleted = false;
    private SoundPool mSoundPool;
    private int mVoiceId = -1;
    private int mPlayId = -1;

    private KDXFSpeechManager() {
        AudioAttributes.Builder ab = new AudioAttributes.Builder();
        ab.setLegacyStreamType(AudioManager.STREAM_ALARM);
        ab.setUsage(AudioAttributes.USAGE_ALARM);
        mSoundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(ab.build()).build();
        mVoiceId = mSoundPool.load(APP.getContext(), R.raw.warning_ring, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPoolLoadCompleted = true;
                Log.e(TAG, "onLoadComplete: 警报音已加载完毕");
            }
        });
    }

    public KDXFSpeechManager init(Activity context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initTextToSpeech(context);
        return instance;
    }

    private void initTextToSpeech(final Activity context) {//初始化语音
        if (!isInited) {
            try {
                mTextToSpeech = new TextToSpeech(context,
                        new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status == TextToSpeech.SUCCESS) {
                                    isInited = true;
                                    // 设置朗读语言
                                    Locale locale = APP.getContext().getResources().getConfiguration().locale;
                                    Log.e(TAG, "onInit: " + locale.getCountry());
                                    int supported = mTextToSpeech.setLanguage(locale);
                                    Log.e(TAG, "onInit: ------- " + supported);
                                    mTTSSupport = supported;

                                    checkQueue();

                                    if ((supported != TextToSpeech.LANG_AVAILABLE)
                                            && (supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("警告");
                                        builder.setCancelable(false);
                                        builder.setMessage("语音引擎初始化失败，请设置支持中文语音\n不设置引擎，将默认使用系统通知铃声");
                                        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                context.startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
                                            }
                                        });

                                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });

                                        AlertDialog alertDialog = builder.create();
                                        Window window = alertDialog.getWindow();
                                        window.setWindowAnimations(R.style.mystyle);  //添加动画
//                                        alertDialog.show();
                                    }
                                }
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //初始化完成后检查队列
    private void checkQueue() {
        if (!(utterQueue.size() > 0)) {
            return;
        }
        final UtterBean utterBean = utterQueue.poll();
        playText(utterBean.utter, new VoicePlayListener() {
            @Override
            public void playComplete(String uttId) {
                if (utterBean.listener != null) {
                    utterBean.listener.playComplete(uttId);
                }
                checkQueue();
            }
        });
    }

    //语音和回调模型
    class UtterBean {
        String utter;
        VoicePlayListener listener;

        public UtterBean(String utter, VoicePlayListener listener) {
            this.utter = utter;
            this.listener = listener;
        }
    }

    //消息队列
    private Queue<UtterBean> utterQueue = new LinkedList<>();

    /***
     * 播放欢迎语
     */
    public void welcome() {
        String welcomeTips = SpUtils.getStr(SpUtils.WELCOM_TIPS, APP.getContext().getResources().getString(R.string.setting_default_welcome_tip));
        if (TextUtils.isEmpty(welcomeTips)) {
            return;
        }
        playText(welcomeTips, null);
    }

    /***
     * 播放文字
     * @param message
     */
    public void playText(final String message) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            playRing();
            return;
        }

        playText(message, null);
    }

    /***
     * 播放文字并回调
     * @param message
     * @param listener，can be null
     */
    public void playText(final String message, final VoicePlayListener listener) {

        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (!isInited) {
            UtterBean u = new UtterBean(message, listener);
            utterQueue.offer(u);
            return;
        }

        ontimeList.add(message);

        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));

        mTextToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if (TextUtils.equals(message, utteranceId) && listener != null) {
                    listener.playComplete(utteranceId);
                }
//                restoreVolumn();
            }
        });

//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD, textToSpeechMap);
    }

    public void destroy() {
        if (mTextToSpeech != null) {
            //停止TextToSpeech
            mTextToSpeech.stop();
            //释放TextToSpeech占用的资源
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
            isInited = false;
        }
    }

    public interface VoicePlayListener {
        void playComplete(String uttId);
    }

    private void playRing() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(APP.getContext(), uri);
        rt.play();
    }

    public void playWaningRing() {
        stopWarningRing();
        if (soundPoolLoadCompleted && mVoiceId != -1) {
            mPlayId = mSoundPool.play(mVoiceId, 1, 1, 1, 0, 1);
        }
    }

    public void stopWarningRing() {
        if (mPlayId != -1) {
            mSoundPool.stop(mPlayId);
            mPlayId = -1;
        }
    }


    public void playNormal(final String message) {
        playNormalAddCallback(TextToSpeech.QUEUE_ADD, message, null);
    }

    public void playNormal(final String message, Runnable runnable) {
        playNormalAddCallback(TextToSpeech.QUEUE_ADD, message, runnable);
    }

    private String mMessage;

    private void playNormalAddCallback(int queueMode, final String message, final Runnable runnable) {
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
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
        mTextToSpeech.setSpeechRate(1.5f);
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
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
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

}
