package com.yemyatthu.spotifystreamer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.yemyatthu.spotifystreamer.R;
import java.io.IOException;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class AudioService extends Service
    implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {
  private static final int NOTIFY_ID = 1;
  private static final int PLAY_NOTI_REQUEST = 1000;
  private static final int QUIT_NOTI_REQUEST = 1001;
  private static final String QUIT_FILTER = "quit_filter";
  private static final String PLAY_FILTER = "play_filter";
  private final IBinder audioBind = new AudioBinder();
  private MediaPlayer mediaPlayer;
  private String url;

 private final BroadcastReceiver quitReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(QUIT_FILTER)) {
        stopForeground(true);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        stopSelf();
      } else if (intent.getAction().equals(PLAY_FILTER)) {
        if (isPng()) {
          pausePlayer();
        } else {
          go();
        }
      }
    }
  };

  public String getUrl(){
    return this.url;
  }
  private RemoteViews remoteView;

  private static AudioService instance = null;

  public static boolean isInstanceCreated() {
    return instance != null;
  }

  public static AudioService getAudioService(){
    return instance;
  }

  @Override public void onCreate() {
    super.onCreate();
    instance = this;
    mediaPlayer = new MediaPlayer();
    initMusicPlayer();
    //registerReceiver(quitReceiver, new IntentFilter(QUIT_FILTER));
    //registerReceiver(quitReceiver, new IntentFilter(PLAY_FILTER));
  }

  @Override public void onCompletion(MediaPlayer mediaPlayer) {
    Intent completeIntent = new Intent();
    completeIntent.setAction(getString(R.string.complete_filter));
    sendBroadcast(completeIntent);
    //stopForeground(true);
  }

  @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
    this.mediaPlayer.reset();
    Intent errorIntent = new Intent();
    errorIntent.setAction(getString(R.string.error_filter));
    sendBroadcast(errorIntent);
    return false;
  }



  @Override public void onPrepared(MediaPlayer mediaPlayer) {
    mediaPlayer.start();
    Intent intent = new Intent();
    intent.setAction(getString(R.string.prepare_filter));
    sendBroadcast(intent);
    //Intent notIntent = new Intent(this, MyBooksActivity.class);
    //notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //PendingIntent pendingIntent =
    //    PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    //
    //remoteView = new RemoteViews(getPackageName(), R.layout.audio_noti);
    //remoteView.setTextViewText(R.id.song_title_noti, book.getName());
    //Intent quitIntent = new Intent(QUIT_FILTER);
    //PendingIntent quitPendingIntent =
    //    PendingIntent.getBroadcast(this, QUIT_NOTI_REQUEST, quitIntent,
    //        PendingIntent.FLAG_CANCEL_CURRENT);
    //
    //Intent playIntent = new Intent(PLAY_FILTER);
    //PendingIntent playPendingIntent =
    //    PendingIntent.getBroadcast(this, PLAY_NOTI_REQUEST, playIntent,
    //        PendingIntent.FLAG_UPDATE_CURRENT);
    //remoteView.setOnClickPendingIntent(R.id.play_noti, playPendingIntent);
    //remoteView.setOnClickPendingIntent(R.id.quit_noti, quitPendingIntent);
    //NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    //builder.setContentIntent(pendingIntent)
    //    .setSmallIcon(R.mipmap.ic_launcher)
    //    .setOngoing(true)
    //    .setTicker(book.getName())
    //    .setContent(remoteView);
    //Notification not = builder.build();
    //startForeground(NOTIFY_ID, not);
  }

  @Override public IBinder onBind(Intent intent) {
    return audioBind;
  }

  @Override public boolean onUnbind(Intent intent) {
    // if music is not playing, destroy it.
    if(!mediaPlayer.isPlaying()){
      onDestroy();
    }
    return true;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopSelf();
    //stopForeground(true);
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
    }
    instance = null;
    //unregisterReceiver(quitReceiver);
  }

  public void initMusicPlayer() {
    //set player properties
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);
  }

  public void playSong(String url) {
    if(url==null || url.length()==0 ){
      return;
    }
    this.url = url;
    //play a song
    if (mediaPlayer == null) {
      mediaPlayer = new MediaPlayer();
      initMusicPlayer();
    } else {
      if(mediaPlayer.isPlaying()){
        mediaPlayer.stop();

        /*** Only resetting, without releasing and starting a new media player,
         * This error occurred on my two test devices - both from Xiaomi
         * But working fine in emulator
         * Not sure because of MIUI bug or App bug.
         *
         *   MediaPlayer
         *   error (-38, 0)
         *   Error (-38,0)
         *   error (1, -107)
         *
         ***/

        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
      }else {
        mediaPlayer.reset();
      }
    }
    try {
      mediaPlayer.setDataSource(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent();
    intent.setAction(getString(R.string.play_song));
    sendBroadcast(intent);
    mediaPlayer.prepareAsync();
  }

  public int getPosn() {
    return mediaPlayer.getCurrentPosition();
  }

  public int getDur() {
    return mediaPlayer.getDuration();
  }

  public boolean isPng() {
    return mediaPlayer != null && mediaPlayer.isPlaying();
  }

  public void pausePlayer() {
    mediaPlayer.pause();
  }

  public void seek(int posn) {
    mediaPlayer.seekTo(posn);
  }

  public void go() {
    mediaPlayer.start();
  }

  public class AudioBinder extends Binder {
    public AudioService getService() {
      return AudioService.this;
    }
  }
}
