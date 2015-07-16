package com.yemyatthu.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.yemyatthu.spotifystreamer.Config;
import com.yemyatthu.spotifystreamer.R;
import com.yemyatthu.spotifystreamer.util.NotificationUtils;
import java.io.IOException;
import java.util.List;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class AudioService extends Service
    implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {
  private static final int NOTIFY_ID = 1;
  private static AudioService instance = null;
  private final IBinder audioBind = new AudioBinder();
  private MediaPlayer mediaPlayer;
  private String url;
  private RemoteViews remoteView;
  private Track currentTrack = null;
  private int position;
  private List<Track> tracks;
  private boolean isTablet;
  private Notification not;
  private NotificationManager notManager;
  private final BroadcastReceiver quitReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Config.QUIT_FILTER)) {
        onDestroy();
        //Send message to player fragment to kill itself
        sendBroadcast(new Intent(Config.QUIT_SELF));
      } else if (intent.getAction().equals(Config.NOTI_PRESS_PLAY_FILTER)) {
        if (isPng()) {
          pausePlayer();
        } else {
          go();
        }
      } else if (intent.getAction().equals(Config.NEXT_FILTER)) {
        playNextSong();
      } else if (intent.getAction().equals(Config.PREV_FILTER)) {
        playPrevSong();
      }
    }
  };

  public static boolean isInstanceCreated() {
    return instance != null;
  }

  public static AudioService getAudioService() {
    return instance;
  }

  public Track getCurrentTrack() {
    return this.currentTrack;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Config.NOTI_PRESS_PLAY_FILTER);
    intentFilter.addAction(Config.NEXT_FILTER);
    intentFilter.addAction(Config.PREV_FILTER);
    intentFilter.addAction(Config.QUIT_FILTER);
    registerReceiver(quitReceiver, intentFilter);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onCreate() {
    super.onCreate();
    instance = this;
    isTablet = false;
    notManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    sendBroadcast(new Intent(Config.SERVICE_STARTED));
  }

  @Override public void onCompletion(MediaPlayer mediaPlayer) {
    Intent completeIntent = new Intent();
    completeIntent.setAction(Config.COMPLETE_FILTER);
    sendBroadcast(completeIntent);
    stopForeground(true);
  }

  @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
    this.mediaPlayer.reset();
    Intent errorIntent = new Intent();
    errorIntent.setAction(Config.ERROR_FILTER);
    sendBroadcast(errorIntent);
    return false;
  }

  @Override public void onPrepared(MediaPlayer mediaPlayer) {
    mediaPlayer.start();
    Intent intent = new Intent();
    intent.setAction(Config.PREPARE_FILTER);
    sendBroadcast(intent);
    remoteView = new RemoteViews(getPackageName(), R.layout.noti_music_player);
    not = NotificationUtils.buildNotiForAudiService(remoteView, currentTrack, this);
    startForeground(NOTIFY_ID, not);
  }

  @Override public IBinder onBind(Intent intent) {
    return audioBind;
  }

  @Override public boolean onUnbind(Intent intent) {
    // if music is not playing, destroy it.
    return true;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopSelf();
    stopForeground(true);
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
    }
    instance = null;
    try {
      unregisterReceiver(quitReceiver);
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }

  public void initMusicPlayer() {
    //set player properties
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);
  }

  public void playNextSong() {
    if (position + 1 < tracks.size()) {
      position++;
      playSong(tracks, position);
    }
  }

  public void playPrevSong() {
    if (!(position - 1 <= 0)) {
      position--;
      playSong(tracks, position);
    }
  }

  public void setMode(boolean isTablet) {
    this.isTablet = isTablet;
  }

  public void playSong(List<Track> tracks, int position) {
    this.position = position;
    this.tracks = tracks;
    currentTrack = tracks.get(position);
    url = currentTrack.preview_url;
    if (url == null || url.length() == 0) {
      return;
    }
    //play a song
    if (mediaPlayer != null) {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.stop();
      }
      mediaPlayer.release();
      mediaPlayer = null;
    }
      mediaPlayer = new MediaPlayer();
      initMusicPlayer();

    try {
      mediaPlayer.setDataSource(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent();
    intent.setAction(Config.PLAY_SONG_FILTER);
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
    remoteView.setImageViewResource(R.id.play_pause_btn_noti,
        R.drawable.ic_play_circle_fill_grey600_48dp);
    not.contentView = remoteView;
    notManager.notify(NOTIFY_ID, not);
  }

  public void seek(int posn) {
    mediaPlayer.seekTo(posn);
  }

  public void go() {
    remoteView.setImageViewResource(R.id.play_pause_btn_noti,
        R.drawable.ic_pause_circle_fill_grey600_48dp);
    not.contentView = remoteView;
    notManager.notify(NOTIFY_ID, not);
    mediaPlayer.start();
  }

  public class AudioBinder extends Binder {
    public AudioService getService() {
      return AudioService.this;
    }
  }
}
