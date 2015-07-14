package com.yemyatthu.spotifystreamer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
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
import com.yemyatthu.spotifystreamer.util.GeneralUtils;
import com.yemyatthu.spotifystreamer.util.SharePrefUtils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerFragment extends DialogFragment
    implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, Runnable {
  public static final String TRACK_POSITION =
      "com.yemyatthu.spotifystreamer.musicplayerfragment.TRACK_POSITION";
  public static final String FILE_NAME =
      "com.yemyatthu.spotifystreamer.musicplayerfragment.FILE_NAME";
  private static final String IS_TABLET =
      "com.yemyatthu.spotifystreamer.musicplayerfragment.IS_TABLET";
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
  private BaseActivity activity;
  private ActionBar actionBar;
  private AudioService audioService;
  private Intent playIntent;
  private int trackPosition;
  private MediaPreparedReceiver mediaPreparedReceiver;
  private List<Track> tracks;
  private boolean isTablet;
  private Track localTrack;

  public MusicPlayerFragment() {

  }

  public static MusicPlayerFragment getNewInstance(int position, String fileName,
      boolean isTablet) {
    Bundle args = new Bundle();
    args.putInt(TRACK_POSITION, position);
    args.putString(FILE_NAME, fileName);
    args.putBoolean(IS_TABLET, isTablet);
    MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
    musicPlayerFragment.setArguments(args);
    return musicPlayerFragment;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_player, container, false);
    ButterKnife.inject(this, view);
    setHasOptionsMenu(true);
    mediaPreparedReceiver = new MediaPreparedReceiver();
    activity.setSupportActionBar(playerToolbar);
    actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("");
      actionBar.hide();
    }
    playerControlContainer.setVisibility(View.INVISIBLE);
    if (getArguments().getBoolean(IS_TABLET)) {
      isTablet = true;
    } else {
      actionBar.setDisplayHomeAsUpEnabled(true);
      isTablet = false;
    }
    /***
     * This is actually bad for performance. Everytime user changes the rotation,
     * tracks are retreived from the local storage
     * This cause a slight delay in ui update
     * One way to eliminate that would be store track in onSaveInstanceState
     * But that would need track to be Parcelable
     ***/
    String fileName = getArguments().getString(FILE_NAME);
    trackPosition = getArguments().getInt(TRACK_POSITION);
    tracks = FileUtils.convertJsonStringToList(FileUtils.loadJsonString(activity, fileName));
    localTrack = tracks.get(trackPosition);
    playBtn.setOnClickListener(this);
    nextBtn.setOnClickListener(this);
    prevBtn.setOnClickListener(this);
    seekBar.setOnSeekBarChangeListener(this);
    progressContainer.setOnClickListener(
        this); // Set a dummy click listener so that user can't press other view below this
    return view;
  }

  public int getDuration() {
    if (audioService != null) {
      return audioService.getDur();
    } else {
      return 0;
    }
  }

  public int getCurrentPosition() {
    if (audioService != null) {
      return audioService.getPosn();
    }
    return 0;
  }

  public void seekTo(int i) {
    audioService.seek(i);
  }

  public boolean isPlaying() {
    if (audioService != null) {
      return audioService.isPng();
    }
    return false;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (BaseActivity) activity;
  }

  @Override public void onClick(View view) {
    if (view.equals(playBtn)) {
      if (isPlaying()) {
        changePlayBtnDrawable(R.drawable.ic_play_circle_fill_white_48dp);
        audioService.pausePlayer();
      } else if (audioService != null) {
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        // There's no need to afraid that media player will be null since the btn is disable until media player finished preparing
        audioService.go();
      }
    } else if (view.equals(prevBtn)) {
      if (!(trackPosition - 1 <= 0)) {
        trackPosition--;
        audioService.playPrevSong();
      }
    } else if (view.equals(nextBtn)) {
      if (trackPosition + 1 < tracks.size()) {
        trackPosition++;
        audioService.playNextSong();
      }
    }
  }

  @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    if (b) {
      seekTo(i);
    }
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override public void onStopTrackingTouch(SeekBar seekBar) {

  }

  private void changePlayBtnDrawable(final int drawable) {
    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        playBtn.setImageDrawable(getResources().getDrawable(drawable));
      }
    }, 100);
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
    filter.addAction(Config.PREPARE_FILTER);
    filter.addAction(Config.ERROR_FILTER);
    filter.addAction(Config.PLAY_SONG_FILTER);
    filter.addAction(Config.QUIT_SELF);
    filter.addAction(Config.SERVICE_STARTED);
    filter.addAction(Config.NOTI_PRESS_PLAY_FILTER);
    activity.registerReceiver(mediaPreparedReceiver, filter);

    if (playIntent == null) {
      playIntent = new Intent(activity, AudioService.class);
    }
    if (!AudioService.isInstanceCreated()) {
      activity.startService(playIntent);
    } else {
      audioService = AudioService.getAudioService();
      playSongFromUrl();
      doUiThings(localTrack);
      updateSeekBar();
    }
  }

  @Override public void onPause() {
    super.onPause();
    activity.unregisterReceiver(mediaPreparedReceiver);
  }

  private void playSongFromUrl() {
    // start a new media player only if the stream link is not the last one played
    if (audioService == null) { //Not connected to audio service. Nothing to do here.
      return;
    }
    Track currentTrack = audioService.getCurrentTrack();
    audioService.setMode(getArguments().getBoolean(IS_TABLET));
    if (currentTrack == null || !localTrack.preview_url.equals(currentTrack.preview_url)) {
      audioService.playSong(tracks, trackPosition); // start playing a new song
    } else {
      if (isPlaying()) {
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        updateSeekBar();
        progressContainer.setVisibility(View.GONE);
      }
    }
  }

  private void updateSeekBar() {
    seekBar.setProgress(0);
    System.out.println(getDuration());
    seekBar.setMax(getDuration());
    //calculate Duration
    int HOUR = 60 * 60 * 1000;
    int MINUTE = 60 * 1000;
    int SECOND = 1000;
    int durationInMillis = getDuration();
    int durationMint = (durationInMillis % HOUR) / MINUTE;
    int durationSec = (durationInMillis % MINUTE) / SECOND;
    startTime.setText("00:00"); //start time is always zero
    stopTime.setText(String.format("%02d:%02d", durationMint, durationSec));
    new Thread(MusicPlayerFragment.this).start(); // Update seekbar
  }

  private void doUiThings(Track track) {
    trackTitleTv.setText(track.name);
    artistTitleTv.setText(""); // Clear the artist textview first not to mix with next track artists
    for (ArtistSimple artist : track.artists) {
      artistTitleTv.append(artist.name + " ");
    }
    new BitmapAsyncTask(MusicPlayerFragment.this).execute(
        track.album.images.get(0).url); // Get the bitmap and color the toolbar.
    albumTitlePlayerTv.setText(track.album.name);
    trackTitlePlayerTv.setText(track.name);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(TRACK_POSITION, trackPosition);
  }

  private class MediaPreparedReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Config.PREPARE_FILTER)) {
        changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        //We only get duraion when the media player finish preparing
        updateSeekBar();
        progressContainer.setVisibility(View.GONE);
      } else if (intent.getAction().equals(Config.COMPLETE_FILTER)) {
        changePlayBtnDrawable(R.drawable.ic_play_circle_fill_white_48dp);
      } else if (intent.getAction().equals(Config.ERROR_FILTER)) {
        Toast.makeText(activity, R.string.R_string_error_music_play, Toast.LENGTH_LONG).show();
      } else if (intent.getAction().equals(Config.PLAY_SONG_FILTER)) {
        progressContainer.setVisibility(View.VISIBLE);
        doUiThings(
            audioService.getCurrentTrack()); //When you press next or prev from noti, this update the ui
      } else if (intent.getAction().equals(Config.QUIT_SELF)) {
        if (isTablet) {
          getDialog().dismiss();
        } else {
          activity.finish();
        }
      } else if (intent.getAction().equals(Config.NOTI_PRESS_PLAY_FILTER)) {
        if (isPlaying()) { //If music is playing, that means it'll be paused
          changePlayBtnDrawable(R.drawable.ic_play_circle_fill_white_48dp);
        } else {
          changePlayBtnDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
        }
      } else if (intent.getAction().equals(Config.SERVICE_STARTED)) {
        audioService = AudioService.getAudioService();
        playSongFromUrl();
      }
    }
  }

  class BitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {
    android.support.v4.app.DialogFragment fragment;

    public BitmapAsyncTask(android.support.v4.app.DialogFragment fragment) {
      this.fragment = fragment;
    }

    @Override protected Bitmap doInBackground(String... urls) {
      try {
        Bitmap bitmap = Glide.with(fragment).load(urls[0]).asBitmap().into(-1, -1).get();
        //Log.d("image64",GeneralUtils.convertBitmapToBase64(bitmap));
        //SharePrefUtils.getInstance(activity).saveCurrentTrackImage(
        //    GeneralUtils.convertBitmapToBase64(bitmap)); // Save the bitmap for later use
        return bitmap;
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      if (bitmap != null) {
        SharePrefUtils.getInstance(activity)
            .saveCurrentTrackImage(GeneralUtils.convertBitmapToBase64(
                bitmap)); // Save the bitmap for later useimageCoverSmall.setImageBitmap(bitmap);
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
}
