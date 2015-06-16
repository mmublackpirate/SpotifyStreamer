package com.yemyatthu.spotifystreamer;

import android.os.Bundle;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerActivity extends BaseActivity {
  public static final String TRACK_POSITION ="com.yemyatthu.spotifystreamer.musicplayeractivity.TRACK_TITLE";
  public static final String FILE_NAME = "com.yemyatthu.spotifystreamer.musicplayeractivity.FILE_NAME";
  private MusicPlayerFragment musicPlayerFragment;
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    musicPlayerFragment =
        (MusicPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_container);
    if(musicPlayerFragment == null){
      int trackPosition = getIntent().getIntExtra(TRACK_POSITION,0);
      String fileName = getIntent().getStringExtra(FILE_NAME);
      musicPlayerFragment = MusicPlayerFragment.getNewInstance(trackPosition,fileName);
      getSupportFragmentManager().beginTransaction().add(R.id.player_container,musicPlayerFragment).commit();
    }
  }
}
