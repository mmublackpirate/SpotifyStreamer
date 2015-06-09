package com.yemyatthu.spotifystreamer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.yemyatthu.spotifystreamer.adapter.TopTracksAdapter;
import com.yemyatthu.spotifystreamer.util.FileUtils;
import com.yemyatthu.spotifystreamer.util.GeneralUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yemyatthu on 6/9/15.
 */
public class TopTracksFragment extends Fragment {
  private static final String IS_TABLET ="com.yemyatthu.spotifystreamer.TopTracksFragment.IS_TABLET";
  @InjectView(R.id.tracks_recycler_view) RecyclerView tracksRecyclerView;
  @InjectView(R.id.top_track_progress_bar) ProgressBar topTrackProgressBar;
  @InjectView(R.id.empty_view) TextView emptyText;
  @InjectView(R.id.toolbar) Toolbar toolbar;

  public static final String ARTIST_ID =
      "com.yemyatthu.spotifystreamer.TopTracksFragment.ARTIST_ID";
  public static final String ARTIST_NAME =
      "com.yemyatthu.spotifystreamer.TopTracksFragment.ARTIST_NAME";
  private static final String LIST_STATE_FRAGMENT = "TRACK_LIST_STATE_FRAGMENT";
  private static final String SAVED_LIST = "SAVED_LIST";
  private static final String LAYOUT_MANGER_STATE = "LAYOUT_MANAGER_STATE" ;
  private SpotifyApi api;
  private SpotifyService spotify;
  private RecyclerView.LayoutManager layoutManager;
  private TopTracksAdapter topTracksAdapter;
  private List<Track> localTracks;
  private AppCompatActivity activity;
  private String listString;
  private String artistId;
  private String artistName;
  private ActionBar actionBar;
  private boolean isTablet;

  public static TopTracksFragment getNewInstance(String artistId,String artistName,boolean isTablet){
    Bundle args = new Bundle();
    Log.d(artistId,artistName);
    args.putString(ARTIST_ID, artistId);
    args.putString(ARTIST_NAME, artistName);
    args.putBoolean(IS_TABLET, isTablet);
    TopTracksFragment topTracksFragment = new TopTracksFragment();
    topTracksFragment.setArguments(args);
    return topTracksFragment;
  }
  public TopTracksFragment(){

  }
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
    ButterKnife.inject(this, view);
    activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    actionBar = activity.getSupportActionBar();
    String title =getString(R.string.top_tracks_title);
    actionBar.setTitle(title);
    artistId = getArguments().getString(ARTIST_ID);
    isTablet = getArguments().getBoolean(IS_TABLET);
    artistName = getArguments().getString(ARTIST_NAME);

    if(artistId==null||artistId.length()<1){
      tracksRecyclerView.setVisibility(View.GONE);
      emptyText.setVisibility(View.GONE);
      topTrackProgressBar.setVisibility(View.GONE);
      return view;
    }
    actionBar.setSubtitle(artistName);

    //If the device is tablet, remove the up arrow and
    // set Left and Right padding to recycler view to make it a bit looks good
    if(!isTablet) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }else{
      tracksRecyclerView.setPadding(10,0,10,0);
    }

    api = new SpotifyApi();
    spotify = api.getService();
    layoutManager = new LinearLayoutManager(getActivity());
    topTracksAdapter = new TopTracksAdapter();
    tracksRecyclerView.setLayoutManager(layoutManager);
    tracksRecyclerView.setHasFixedSize(true);
    tracksRecyclerView.setAdapter(topTracksAdapter);
    localTracks = FileUtils.convertJsonStringToList(FileUtils.loadJsonString(activity, artistId + ".dat"));
    Map<String, Object> countryParam = new HashMap<>();
    countryParam.put("country", GeneralUtils.getCountry());

    if (savedInstanceState!= null) { // That means configuration changes
      listString = savedInstanceState.getString(SAVED_LIST);
      topTrackProgressBar.setVisibility(View.GONE);
      List<Track> saveList = FileUtils.convertJsonStringToList(listString);
      topTracksAdapter.setTracks(saveList);
      tracksRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_MANGER_STATE));
    }
      else{
        spotify.getArtistTopTrack(artistId, countryParam, new Callback<Tracks>() {
          @Override public void success(final Tracks tracks, Response response) {
            if (tracks.tracks != null && tracks.tracks.size() > 0) {
              activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                  topTrackProgressBar.setVisibility(View.GONE);
                  topTracksAdapter.setTracks(tracks.tracks);
                }
              });
              listString = FileUtils.convertListToJsonString(tracks.tracks);
              FileUtils.saveJsonString(activity, artistId + ".dat", listString);
            } else {
              activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                  if (localTracks != null && localTracks.size() > 0) {
                    topTrackProgressBar.setVisibility(View.GONE);
                    topTracksAdapter.setTracks(localTracks);
                  } else {
                    tracksRecyclerView.setVisibility(View.GONE);
                    topTrackProgressBar.setVisibility(View.GONE);
                    emptyText.setText(getString(R.string.top_tracks_empty));
                  }
                }
              });
            }
          }

          @Override public void failure(RetrofitError error) {
            activity.runOnUiThread(new Runnable() {
              @Override public void run() {
                if (localTracks != null && localTracks.size() > 0) {
                  listString = FileUtils.convertListToJsonString(localTracks);
                  topTrackProgressBar.setVisibility(View.GONE);
                  topTracksAdapter.setTracks(localTracks);
                } else {
                  tracksRecyclerView.setVisibility(View.GONE);
                  topTrackProgressBar.setVisibility(View.GONE);
                  emptyText.setText(getString(R.string.error_try_again));
                }
              }
            });
          }
        });
      }

      return view;
    }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if(listString!=null) {
      outState.putString(SAVED_LIST,listString);
      outState.putParcelable(LAYOUT_MANGER_STATE,((LinearLayoutManager)tracksRecyclerView.getLayoutManager()).onSaveInstanceState());
    }
  }
}
