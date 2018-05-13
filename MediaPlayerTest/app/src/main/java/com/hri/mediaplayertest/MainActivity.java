package com.hri.mediaplayertest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private SoundPool waitSoundPool;
    private AudioTrack trackplayer;
    private SurfaceHolder surfaceHolder;
    private Button upVolBtn, downVolBtn, playSource, soundPlayBtn, audioTrackPlayBtn;
    private ImageView imageView;
    private String videoFilePath = "/sdcard/allen/3.mp4";
    private int waitSoundId;
    private AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        upVolBtn = findViewById(R.id.button);
        downVolBtn = findViewById(R.id.button2);
        playSource = findViewById(R.id.button3);
        soundPlayBtn = findViewById(R.id.button4);
        audioTrackPlayBtn = findViewById(R.id.button5);
        imageView = findViewById(R.id.imageView);
        upVolBtn.setOnClickListener(this);
        downVolBtn.setOnClickListener(this);
        playSource.setOnClickListener(this);
        soundPlayBtn.setOnClickListener(this);
        audioTrackPlayBtn.setOnClickListener(this);

        requestPermission();
        initAction();
    }

    public void initAction(){
        mediaPlayer = new MediaPlayer();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new MyCallBack());

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i("mediaPlayer", "onPrepared");

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.i("mediaPlayer", "onCompletion");
                mediaPlayer.reset();
                handler.sendEmptyMessage(1);
            }
        });
        //
        waitSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        waitSoundId = waitSoundPool.load(this, R.raw.cn, 1);

        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(am.isSpeakerphoneOn()){
            am.setSpeakerphoneOn(false);
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        am.setMode(AudioManager.MODE_IN_CALL);

    }

    private class MyCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("MyCallBack", "surfaceCreated");
            mediaPlayer.setDisplay(holder);
//            mediaPlayer.prepareAsync();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i("MyCallBack", "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("MyCallBack", "surfaceDestroyed");
        }
    }

    private void playVideo() {
        Log.i("playVideo", " mediaPlayer:"+mediaPlayer);
        if (mediaPlayer == null) {
            return;
        }
        imageView.setVisibility(View.GONE);
        surfaceView.setVisibility(View.VISIBLE);
        try {
            mediaPlayer.setDataSource(videoFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showImage() {
        surfaceView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    handler.sendEmptyMessage(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case 1:
                    showImage();
                    break;
                case 2:
                    playVideo();
                    break;
            }
            return false;
        }
    });

    public void requestPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                mediaPlayer.setVolume(0.9f, 0.9f);
                break;
            case R.id.button2:
                mediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case R.id.button3:
                playVideo();
                break;
            case R.id.button4:
                soundPlay();
                break;
            case R.id.button5:
                jetPlayStream();
        }
    }

    private void jetPlayStream(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                // 获取最小缓冲区
                int bufSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                // 实例化AudioTrack(设置缓冲区为最小缓冲区的2倍，至少要等于最小缓冲区)
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, bufSize*2, AudioTrack.MODE_STREAM);
                Log.d("", " volume:"+AudioTrack.getMaxVolume());
                // 设置音量
//                audioTrack.setVolume(AudioTrack.getMaxVolume()) ;
                // 设置播放频率
//                audioTrack.setPlaybackRate(10) ;
                InputStream is = getResources().openRawResource(R.raw.cnwav);
                audioTrack.play();
                // 获取音乐文件输入流

                byte[] buffer = new byte[bufSize*2] ;
                int len ;
                try {
                    while((len=is.read(buffer,0,buffer.length)) != -1){
                        System.out.println("读取数据中...:"+len);
                        // 将读取的数据，写入Audiotrack
                        audioTrack.write(buffer,0,len) ;
                    }
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioTrack.play();
            }
        }).start();
    }

    private void soundPlay() {
        waitSoundPool.play(waitSoundId, 0.9f, 0.9f, 1, 10, 1);
    }
}
