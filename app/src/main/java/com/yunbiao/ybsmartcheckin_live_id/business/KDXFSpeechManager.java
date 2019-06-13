package com.yunbiao.ybsmartcheckin_live_id.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.Window;

import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.HandleMessageUtils;

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
    private static KDXFSpeechManager instance;
    private static AudioManager audioManager = null; // 音频
    private static TextToSpeech mTextToSpeech;//语音类
    private boolean isInited = false;
    private static HashMap<String, String> textToSpeechMap = new HashMap<>();
    private static List<String> ontimeList = new ArrayList<>();
    private static Integer CURRENT_SOUND = 0;
    private final String UTTERANCE_WELCOME = "欢迎使用云标智能签到系统";
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

    private KDXFSpeechManager() {
    }

    public KDXFSpeechManager init(Activity context){
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
                                    int supported = mTextToSpeech.setLanguage(Locale.CHINA);
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
                                        alertDialog.show();
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
    private void checkQueue(){
        if(!(utterQueue.size()>0)){
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
    class UtterBean{
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
     * @param listener 回调，can be null
     */
    public void welcome(final VoicePlayListener listener){
        playText(UTTERANCE_WELCOME,listener);
    }

    /***
     * 播放文字
     * @param message
     */
    public void playText(final String message){
        if ((mTTSSupport != TextToSpeech.LANG_AVAILABLE)
                && (mTTSSupport != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
            playRing();
            return;
        }

        playText(message,null);
    }

    /***
     * 播放文字并回调
     * @param message
     * @param listener，can be null
     */
    public void playText(final String message, final VoicePlayListener listener) {

        if(TextUtils.isEmpty(message)){
            return;
        }
        if(!isInited){
            UtterBean u = new UtterBean(message,listener);
            utterQueue.offer(u);
            return;
        }

        ontimeList.add(message);
        /**
         * 如果有背景音乐的处理
         */
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            CURRENT_SOUND = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
        textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));

        mTextToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if(TextUtils.equals(message,utteranceId) && listener != null){
                    listener.playComplete(utteranceId);
                }
                restoreVolumn();
            }
        });

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD, textToSpeechMap);
    }

    public void destroy(){
        if (mTextToSpeech != null) {
            //停止TextToSpeech
            mTextToSpeech.stop();
            //释放TextToSpeech占用的资源
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
            isInited = false;
        }
    }

    private void restoreVolumn(){
        if (ontimeList.size() > 0) {
            ontimeList.remove(0);
            if (ontimeList.size() > 0) {
//                                HandleMessageUtils.getInstance().sendHandler(SHOUT, uihandler, ontimeList.get(0));
            } else {
                //播放完成后恢复后台音乐或者视频（如果有的话）
                HandleMessageUtils.getInstance().runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (CURRENT_SOUND > 0)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, CURRENT_SOUND, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                });
            }
        }
    }

    public interface VoicePlayListener{
        void playComplete(String uttId);
    }
    private void playRing(){
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(APP.getContext(), uri);
        rt.play();
    }
}
