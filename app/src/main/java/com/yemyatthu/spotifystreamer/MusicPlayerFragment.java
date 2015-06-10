package com.yemyatthu.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.bumptech.glide.Glide;
import com.yemyatthu.spotifystreamer.service.AudioService;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerFragment extends Fragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,Runnable{
  @InjectView(R.id.track_title) TextView trackTitleTv;
  @InjectView(R.id.artist_title) TextView artistTitleTv;
  @InjectView(R.id.image_cover_small) ImageView imageCoverSmall;
  @InjectView(R.id.player_toolbar) Toolbar playerToolbar;
  @InjectView(R.id.play_btn) ImageButton playBtn;
  @InjectView(R.id.image_cover_big) ImageView imageCoverBig;
  @InjectView(R.id.seek_bar) SeekBar seekBar;
  @InjectView(R.id.start_time) TextView startTime;
  @InjectView(R.id.stop_time) TextView stopTime;

  public static final String TRACK_TITLE ="com.yemyatthu.spotifystreamer.musicplayerfragment.TRACK_TITLE";
  public static final String ARTIST_NAME = "com.yemyatthu.spotifystreamer.musicplayerfragment.ARTIST_NAME";
  public static final String COVER_URL = "com.yemyatthu.spotifystreamer.musicplayerfragment.COVER_URL";
  public static final String PREVIEW_LINK = "com.yemyatthu.spotifystreamer.musicplayerfragment.PREVIEW_LINK";
  private BaseActivity activity;
  private ActionBar actionBar;
  private AudioService audioService;
  private boolean musicBound;
  private Intent playIntent;
  private String artistName;
  private String trackTitle;
  private String coverUrl;
  private String previewUrl;
  private MediaPreparedReceiver mediaPreparedReceiver;
  private SharedPreferences sharePreferences;

  public static MusicPlayerFragment getNewInstance(String artistName,String trackTitle,String coverUrl,String previewUrl){
    Bundle args = new Bundle();
    args.putString(ARTIST_NAME,artistName);
    args.putString(TRACK_TITLE,trackTitle);
    args.putString(COVER_URL,coverUrl);
    args.putString(PREVIEW_LINK, previewUrl);
    MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
    musicPlayerFragment.setArguments(args);
    return musicPlayerFragment;
  }

  public MusicPlayerFragment(){

  }

  private ServiceConnection musicConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
      audioService = binder.getService();
      musicBound = true;
      audioService.getUrl(previewUrl); // Give the service url to work
      audioService.playSong(); // start playing a new song
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
      musicBound = false;
    }
  };

  private class MediaPreparedReceiver extends BroadcastReceiver{

    @Override public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(getString(R.string.prepare_filter))){
        //We only get duraion when the media player finish preparing
        seekBar.setProgress(0);
        seekBar.setMax(getDuration());

        //calculate Duration
        int HOUR = 60*60*1000;
        int MINUTE = 60*1000;
        int SECOND = 1000;
        int durationInMillis = getDuration();
        int durationMint = (durationInMillis%HOUR)/MINUTE;
        int durationSec = (durationInMillis%MINUTE)/SECOND;
        startTime.setText("00:00"); //start time is always zero
        stopTime.setText(String.format("%02d:%02d",durationMint,durationSec));
        new Thread(MusicPlayerFragment.this).start();
      }
    }
  }
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_player,container,false);
    ButterKnife.inject(this, view);
    mediaPreparedReceiver = new MediaPreparedReceiver();
    activity = (BaseActivity) getActivity();
    activity.setSupportActionBar(playerToolbar);
    actionBar = activity.getSupportActionBar();
    actionBar.setTitle("");
    actionBar.setDisplayHomeAsUpEnabled(true);
    artistName = getArguments().getString(ARTIST_NAME);
    trackTitle = getArguments().getString(TRACK_TITLE);
    previewUrl = getArguments().getString(PREVIEW_LINK);
    coverUrl = getArguments().getString(COVER_URL);
    trackTitleTv.setText(trackTitle);
    artistTitleTv.setText(artistName);
    Glide.with(this)
        .load(coverUrl)
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.placeholder)
        .into(imageCoverSmall);
    Glide.with(this)
        .load(coverUrl)
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.placeholder)
        .into(imageCoverBig);
    playBtn.setColorFilter(getResources().getColor(R.color.primaryColor));
    playBtn.setOnClickListener(this);
    seekBar.setOnSeekBarChangeListener(this);
    sharePreferences = activity.getDefaultPreferences();
    return view;
  }

  public int getDuration() {
    if(audioService!=null && musicBound) {
      return audioService.getDur();
    }else{
      return 0;
    }

  }

  public int getCurrentPosition() {
    if (audioService != null && musicBound) {
      return audioService.getPosn();
    }
    return 0;
  }

  public void seekTo(int i) {
    audioService.seek(i);
  }

  public boolean isPlaying() {
    if (audioService != null && musicBound) {
      return audioService.isPng();
    }
    return false;
  }


  public int getAudioSessionId() {
    return 0;
  }

  @Override public void onStart() {
    super.onStart();
    if (playIntent == null) {
      playIntent = new Intent(activity, AudioService.class);
      activity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
      activity.startService(playIntent);
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    activity.unbindService(musicConnection);
  }

  @Override public void onClick(View view) {
    if(view.equals(playBtn)){
      if(isPlaying()) {
        changePlayBtnDrawable(R.drawable.ic_play_circle_fill_grey600_48dp);
        audioService.pausePlayer();
      }else if (audioService != null && musicBound) {
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_grey600_48dp);
        audioService.go();
        }
      }
    }

  @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    if(b){
      seekTo(i);
    }
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override public void onStopTrackingTouch(SeekBar seekBar) {

  }

  private void changePlayBtnDrawable(final int drawable){
    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        playBtn.setImageDrawable(
            getResources().getDrawable(drawable));
      }
    },100);

  }

  @Override public void run() {
    int currentPosition = 0;
    long total = getDuration();
    while (currentPosition < total) {
      try {
        currentPosition = getCurrentPosition();
      } catch (Exception e) {
        return;
      }
      seekBar.setProgress(currentPosition);
    }
  }

  @Override public void onResume() {
    super.onResume();
    IntentFilter filter = new IntentFilter(getString(R.string.prepare_filter));
    activity.registerReceiver(mediaPreparedReceiver, filter);
  }

  @Override public void onPause() {
    super.onPause();
    activity.unregisterReceiver(mediaPreparedReceiver);
  }
}
