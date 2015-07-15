package com.yemyatthu.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yemyatthu on 6/8/1x5.
 */
public class TopTracksActivity extends AppCompatActivity {

  public static final String ARTIST_ID =
      "com.yemyatthu.spotifystreamer.TopTracksActivity.ARTIST_ID";
  public static final String ARTIST_NAME =
      "com.yemyatthu.spotifystreamer.TopTracksActivity.ARTIST_NAME";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_top_tracks);
    String artistName = getIntent().getStringExtra(ARTIST_NAME);
    String artistId = getIntent().getStringExtra(ARTIST_ID);
    TopTracksFragment topTrackFragment =
        (TopTracksFragment) getSupportFragmentManager().findFragmentById(R.id.top_tracks_container);
    if (topTrackFragment == null) {
      topTrackFragment = TopTracksFragment.getNewInstance(artistId, artistName, false);
      getSupportFragmentManager().beginTransaction()
          .add(R.id.top_tracks_container, topTrackFragment)
          .commit();
    }
  }
}
