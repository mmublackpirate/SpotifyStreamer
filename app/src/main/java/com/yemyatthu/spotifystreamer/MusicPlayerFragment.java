package com.yemyatthu.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.bumptech.glide.Glide;
import com.yemyatthu.spotifystreamer.service.AudioService;
import com.yemyatthu.spotifystreamer.util.FileUtils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,Runnable{
  @InjectView(R.id.track_title) TextView trackTitleTv;
  @InjectView(R.id.artist_title) TextView artistTitleTv;
  @InjectView(R.id.image_cover_small) ImageView imageCoverSmall;
  @InjectView(R.id.player_toolbar) Toolbar playerToolbar;
  @InjectView(R.id.play_btn) ImageButton playBtn;
  @InjectView(R.id.next_btn) ImageButton nextBtn;
  @InjectView(R.id.prev_btn) ImageButton prevBtn;
  @InjectView(R.id.image_cover_big) ImageView imageCoverBig;
  @InjectView(R.id.seek_bar) SeekBar seekBar;
  @InjectView(R.id.start_time) TextView startTime;
  @InjectView(R.id.stop_time) TextView stopTime;
  @InjectView(R.id.track_title_player) TextView trackTitlePlayerTv;
  @InjectView(R.id.album_title_player) TextView albumTitlePlayerTv;
  @InjectView(R.id.progress_container) FrameLayout progressContainer;
  @InjectView(R.id.player_control_container) RelativeLayout playerControlContainer;

  public static final String TRACK_POSITION ="com.yemyatthu.spotifystreamer.musicplayerfragment.TRACK_POSITION";
  public static final String FILE_NAME = "com.yemyatthu.spotifystreamer.musicplayerfragment.FILE_NAME";
  private static final String IS_TABLET ="com.yemyatthu.spotifystreamer.musicplayerfragment.IS_TABLET";
  private BaseActivity activity;
  private ActionBar actionBar;
  private AudioService audioService;
  private boolean musicBound;
  private Intent playIntent;
  private String fileName;
  private int trackPosition;
  private MediaPreparedReceiver mediaPreparedReceiver;
  private SharedPreferences sharePreferences;
  private List<Track> tracks;
  private String previewUrl;
  private boolean isTablet;
  public static MusicPlayerFragment getNewInstance(int position,String fileName,boolean isTablet) {
    Bundle args = new Bundle();
    args.putInt(TRACK_POSITION, position);
    args.putString(FILE_NAME, fileName);
    args.putBoolean(IS_TABLET,isTablet);
    MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
    musicPlayerFragment.setArguments(args);
    return musicPlayerFragment;
  }

  public MusicPlayerFragment(){

  }

  private ServiceConnection musicConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
      if(audioService==null) {
        audioService = binder.getService();
        musicBound = true;
        playSongFromUrl();
      }
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
      musicBound = false;
    }
  };

  private class MediaPreparedReceiver extends BroadcastReceiver{

    @Override public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(getString(R.string.prepare_filter))){
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        //We only get duraion when the media player finish preparing
        updateSeekBar();
        progressContainer.setVisibility(View.GONE);
      } else if(intent.getAction().equals(getString(R.string.complete_filter))){
        changePlayBtnDrawable(R.drawable.ic_play_circle_fill_white_48dp);
      } else if(intent.getAction().equals(getString(R.string.error_filter))){
        Toast.makeText(activity,"Can't play the requested music",Toast.LENGTH_LONG).show();
      } else if (intent.getAction().equals(getString(R.string.play_song))){
        progressContainer.setVisibility(View.VISIBLE);
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
    actionBar.hide();
    playerControlContainer.setVisibility(View.INVISIBLE);
    if(!getArguments().getBoolean(IS_TABLET)){
    actionBar.setDisplayHomeAsUpEnabled(true);
    }
    trackPosition = getArguments().getInt(TRACK_POSITION);
    fileName = getArguments().getString(FILE_NAME);
    tracks = FileUtils.convertJsonStringToList(FileUtils.loadJsonString(activity, fileName));
    if(trackPosition<tracks.size()){
      doUiThings(tracks.get(trackPosition));
    }
    playBtn.setOnClickListener(this);
    nextBtn.setOnClickListener(this);
    prevBtn.setOnClickListener(this);
    seekBar.setOnSeekBarChangeListener(this);
    progressContainer.setOnClickListener(this); // Set a dummy click listener so that user can't press other view below this
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
      if(!AudioService.isInstanceCreated()){
        activity.startService(playIntent);
      }else{
        audioService = AudioService.getAudioService();
        musicBound = true;
        playSongFromUrl();
      }
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    activity.unbindService(musicConnection);
  }

  @Override public void onClick(View view) {
    if(view.equals(playBtn)){
      if(isPlaying()) {
        changePlayBtnDrawable(R.drawable.ic_play_circle_fill_white_48dp);
        audioService.pausePlayer();
      }else if (audioService != null && musicBound) {
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        audioService.go();
        }
      }
    else if(view.equals(prevBtn)){
     if(trackPosition<=0){
       trackPosition --;
       doUiThings(tracks.get(trackPosition));
       playSongFromUrl();
     }
    }
    else if (view.equals(nextBtn)){
      if(trackPosition<tracks.size()){
        trackPosition++;
        doUiThings(tracks.get(trackPosition));
        playSongFromUrl();
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
    int currentPosition = getCurrentPosition();
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
    IntentFilter filter = new IntentFilter();
    filter.addAction(getString(R.string.prepare_filter));
    filter.addAction(getString(R.string.complete_filter));
    filter.addAction(getString(R.string.error_filter));
    filter.addAction(getString(R.string.play_song));
    activity.registerReceiver(mediaPreparedReceiver, filter);
  }

  @Override public void onPause() {
    super.onPause();
    activity.unregisterReceiver(mediaPreparedReceiver);
  }

  private void playSongFromUrl(){
    // start a new media player only if the stream link is not the last one played
    if(!previewUrl.equals(audioService.getUrl())) {
      Log.d("playing song", "true");
      audioService.playSong(previewUrl); // start playing a new song
    }else{
      if(isPlaying()){
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        Log.d("current", getCurrentPosition() + "");
        Log.d("duration", getDuration() + "");
        updateSeekBar();
        progressContainer.setVisibility(View.GONE);
      }
    }
  }

  private void updateSeekBar(){
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
    new Thread(MusicPlayerFragment.this).start(); // Update seekbar
  }

  class BitmapAsyncTask extends AsyncTask<String,Void,Bitmap>{
    android.support.v4.app.DialogFragment fragment;
    public BitmapAsyncTask(android.support.v4.app.DialogFragment fragment){
      this.fragment = fragment;
    }
    @Override protected Bitmap doInBackground(String ... urls) {
      try {
        return Glide.with(fragment)
            .load(urls[0])
            .asBitmap()
            .into(-1, -1)
            .get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      if(bitmap!=null){
        imageCoverSmall.setImageBitmap(bitmap);
        imageCoverBig.setImageBitmap(bitmap);
        int bgColor = Palette.generate(bitmap).getDarkMutedColor(R.color.primaryColor);
        playerToolbar.setBackgroundColor(bgColor);
        playerControlContainer.setBackgroundColor(bgColor);
      }
      actionBar.show();
      playerControlContainer.setVisibility(View.VISIBLE);
    }
  }

  private void doUiThings(Track track){
    previewUrl = track.preview_url;
    trackTitleTv.setText(track.name);
    artistTitleTv.setText(""); // Clear the artist textview first not to mix with next track artists
    for (ArtistSimple artist : track.artists) {
      artistTitleTv.append(artist.name);
    }
    new BitmapAsyncTask(MusicPlayerFragment.this).execute(track.album.images.get(0).url);
    albumTitlePlayerTv.setText(track.album.name);
    trackTitlePlayerTv.setText(track.name);
  }
}
