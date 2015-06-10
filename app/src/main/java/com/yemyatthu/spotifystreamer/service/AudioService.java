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
  private MediaPlayer mMediaPlayer;
  private String url;

 private final BroadcastReceiver quitReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(QUIT_FILTER)) {
        stopForeground(true);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
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
  private RemoteViews remoteView;

  @Override public void onCreate() {
    super.onCreate();
    mMediaPlayer = new MediaPlayer();
    initMusicPlayer();
    //registerReceiver(quitReceiver, new IntentFilter(QUIT_FILTER));
    //registerReceiver(quitReceiver, new IntentFilter(PLAY_FILTER));
  }

  @Override public void onCompletion(MediaPlayer mediaPlayer) {
    mMediaPlayer.release();
    mMediaPlayer = null;
    stopSelf();
    //stopForeground(true);
  }

  @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
    mMediaPlayer.reset();
    return false;
  }



  @Override public void onPrepared(MediaPlayer mediaPlayer) {
    Intent intent = new Intent();
    intent.setAction(getString(R.string.prepare_filter));
    sendBroadcast(intent);
    mediaPlayer.start();

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
    return false;
  }

  @Override public void onDestroy() {
    stopSelf();
    //stopForeground(true);
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
    unregisterReceiver(quitReceiver);
  }

  public void initMusicPlayer() {
    //set player properties
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mMediaPlayer.setOnPreparedListener(this);
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnErrorListener(this);
  }

  public void getUrl(String url) {
    this.url = url;
  }

  public void playSong() {

    //play a song
    if (mMediaPlayer == null) {
      mMediaPlayer = new MediaPlayer();
      initMusicPlayer();
    } else {
      mMediaPlayer.reset();
    }
    try {
      mMediaPlayer.setDataSource(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mMediaPlayer.prepareAsync();
  }

  public int getPosn() {
    return mMediaPlayer.getCurrentPosition();
  }

  public int getDur() {
    return mMediaPlayer.getDuration();
  }

  public boolean isPng() {
    return mMediaPlayer != null && mMediaPlayer.isPlaying();
  }

  public void pausePlayer() {
    mMediaPlayer.pause();
  }

  public void seek(int posn) {
    mMediaPlayer.seekTo(posn);
  }

  public void go() {
    mMediaPlayer.start();
  }

  public class AudioBinder extends Binder {
    public AudioService getService() {
      return AudioService.this;
    }
  }
}
