package com.yemyatthu.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerActivity extends AppCompatActivity {
  public static final String TRACK_POSITION =
      "com.yemyatthu.spotifystreamer.musicplayeractivity.TRACK_TITLE";
  public static final String FILE_NAME =
      "com.yemyatthu.spotifystreamer.musicplayeractivity.FILE_NAME";
  public static final String IS_TABLET =
      "com.yemyatthu.spotifystreamer.musicplayeractivity.IS_TABLET";
  private MusicPlayerFragment musicPlayerFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    musicPlayerFragment =
        (MusicPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_container);
    if (musicPlayerFragment == null) {
      int trackPosition = getIntent().getIntExtra(TRACK_POSITION, 0);
      String fileName = getIntent().getStringExtra(FILE_NAME);
      musicPlayerFragment = MusicPlayerFragment.getNewInstance(trackPosition, fileName,
          getIntent().getBooleanExtra(IS_TABLET, false));
      getSupportFragmentManager().beginTransaction()
          .add(R.id.player_container, musicPlayerFragment)
          .commit();
    }
  }
}
