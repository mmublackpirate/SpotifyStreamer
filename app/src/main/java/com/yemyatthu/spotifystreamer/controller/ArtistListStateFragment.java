package com.yemyatthu.spotifystreamer.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by yemyatthu on 6/8/15.
 */
public class ArtistListStateFragment extends Fragment {
  private List<Artist> artists;
  private int lastScrollPosition;
  private int checkPosition;

  public ArtistListStateFragment() {
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    artists = new ArrayList<>();
    lastScrollPosition = 0;
    checkPosition = -100;
  }

  public List<Artist> getArtists() {
    return this.artists;
  }

  public void setArtists(List<Artist> artists) {
    this.artists = artists;
  }

  public void clearArtists() {
    this.artists.clear();
  }

  public int getLastScrollPosition() {
    return this.lastScrollPosition;
  }

  public void setLastScrollPosition(int lastScrollPosition) {
    this.lastScrollPosition = lastScrollPosition;
  }

  public int getCheckPosition() {
    return this.checkPosition;
  }

  public void setCheckPosition(int checkPosition) {
    this.checkPosition = checkPosition;
  }
}
