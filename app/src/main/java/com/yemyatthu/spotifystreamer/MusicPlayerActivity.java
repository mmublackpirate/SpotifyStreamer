package com.yemyatthu.spotifystreamer;

import android.os.Bundle;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class MusicPlayerActivity extends BaseActivity {
  public static final String TRACK_TITLE ="com.yemyatthu.spotifystreamer.musicplayeractivity.TRACK_TITLE";
  public static final String ARTIST_NAME = "com.yemyatthu.spotifystreamer.musicplayeractivity.ARTIST_NAME";
  public static final String COVER_URL = "com.yemyatthu.spotifystreamer.musicplayeractivity.COVER_URL";
  public static final String PREVIEW_LINK = "com.yemyatthu.spotifystreamer.musicplayeractivity.PREVIEW_LINK";
  private MusicPlayerFragment musicPlayerFragment;
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    musicPlayerFragment =
        (MusicPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_container);
    if(musicPlayerFragment == null){
      String artistTitle = getIntent().getStringExtra(ARTIST_NAME);
      String trackTitle = getIntent().getStringExtra(TRACK_TITLE);
      String coverUrl = getIntent().getStringExtra(COVER_URL);
      String previewLink = getIntent().getStringExtra(PREVIEW_LINK);
      musicPlayerFragment = MusicPlayerFragment.getNewInstance(artistTitle,trackTitle,coverUrl,previewLink);
      getSupportFragmentManager().beginTransaction().add(R.id.player_container,musicPlayerFragment).commit();
    }
  }
}
