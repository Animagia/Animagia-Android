package pl.animagia;

//import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.video.VideoSourcesKt;
import pl.animagia.video.VideoUrl;

import com.android.volley.VolleyError;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenPlaybackActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();

    private PlayerView mMainView;
    private View mControlsView;

    private SimpleExoPlayer mPlayer;
    private int episodes;
    private int currentEpisode;

    private boolean mVisible;
    private Context context;
    PlayerView playerView;
    final Handler handler = new Handler();
    final Runnable r = new Runnable()
    {
        public void run()
        {
            long sek = mPlayer.getCurrentPosition();
            if(sek >= 420000){
                mPlayer.seekTo(415000);
                Alerts.primeVideoError(context);
                onPause();
            }
            handler.postDelayed(this, 300);
        }
    };

    final Runnable hideUi = new Runnable()
    {
        public void run()
        {
            if(mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY) {
                hideSystemUi();
                mVisible = false;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Intent intent = getIntent();
        final VideoData video = intent.getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        final String url = intent.getStringExtra(VideoData.NAME_OF_URL);
        final AppCompatActivity ac = this;

        episodes = video.getEpisodes();
        currentEpisode = 1;


        if (episodes == 1){
            setContentView(R.layout.activity_fullscreen_playback);
            mMainView = findViewById(R.id.exoplayerview_activity_video);
        } else {
            setContentView(R.layout.episodes_fullscreen_playback);
            mMainView = findViewById(R.id.player);
            TextView next = findViewById(R.id.next);
            TextView previous = findViewById(R.id.previous);
            TextView title = findViewById(R.id.film_name);
            title.setText(video.getTitle() + " odc. " + currentEpisode);
            next.setOnClickListener(newEpisodeListener(ac, video, 1));
            previous.setOnClickListener(newEpisodeListener(ac, video, -1));
        }

        mVisible = true;

        mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(this, url, video.getTitle()));

        if(!isPrime(video.getTitle())){
            handler.postDelayed(r, 300);
        }

        mPlayer.addListener(createPlayPauseListener());
        mMainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    toggle();

                }

                return false;
            }
        });

        mPlayer.setPlayWhenReady(true);
    }

    private Player.EventListener createPlayPauseListener() {
        return new Player.DefaultEventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media is playing
//                    hide();
                    handler.postDelayed(hideUi, 5000);
                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                } else {
                    showSystemUi();
                    if (hideUi != null) {
                        handler.removeCallbacks(hideUi);
                    }

                    // player paused in any state
                }
            }
        };
    }

    private View.OnClickListener newEpisodeListener(final AppCompatActivity activity, final VideoData video, final int newEpisode) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEpisodes(newEpisode)){
                    HTML.getHtml(video.getVideoUrl().substring(0,video.getVideoUrl().length()-2)+(currentEpisode + newEpisode), getApplicationContext(), new VolleyCallback() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public void onSuccess(String result) {
                            releaseMediaPlayer();
                            String url =  VideoUrl.getUrl(result);
                            mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(activity, url, video.getTitle()));
                            if(!isPrime(video.getTitle())){
                                handler.postDelayed(r, 300);
                            }

                            mPlayer.addListener(createPlayPauseListener());

                            mMainView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                                        toggle();
                                    }
                                    return false;
                                }
                            });

                            mPlayer.setPlayWhenReady(true);
                            changeCurrentEpisodes(newEpisode);
                            TextView title = findViewById(R.id.film_name);
                            title.setText(video.getTitle() + " odc. " + currentEpisode);
                        }

                        @Override
                        public void onFailure(VolleyError volleyError) {

                        }
                    });
                }

            }
        };
    }


    private SimpleExoPlayer createPlayer(MediaSource mediaSource) {
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        SimpleExoPlayer player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        mMainView.setPlayer(player);

        player.prepare(mediaSource);

        if(episodes==1){
            PlayerControlView controlView = findViewById(R.id.playback_controls);
            controlView.setPlayer(player);
        }

        return player;
    }

    private void toggle() {
        if (mVisible) {
            hide();

        } else {
            show();
            if (hideUi !=null)
                handler.removeCallbacks(hideUi);
            handler.postDelayed(hideUi, 5000);
        }
    }

    private void hide() {
        hideSystemUi();
        System.out.println("UKRYJ");
        mMainView.showController();
        mVisible = false;
    }

    private void show() {
        System.out.println("POKAŻ");
        showSystemUi();
        mMainView.hideController();
        mVisible = true;
    }

    private void hideSystemUi() {
        mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void showSystemUi() {
        mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private boolean isPrime(String title) {
        boolean prime = true;
        if (title.equals("Chuunibyou demo Koi ga Shitai! Take On Me")){
            prime = false;
        }
        return prime;
    }

    @Override
    public void onPause(){
        super.onPause();
        resumeLivePreview();
    }


    private void resumeLivePreview() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }


    private void releaseMediaPlayer() {
        if (r != null) {
            handler.removeCallbacks(r);
        }
        if (hideUi != null) {
            handler.removeCallbacks(hideUi);
        }
        if (mPlayer!= null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer= null;
        }
    }

    @Override
    public void onBackPressed() {
        releaseMediaPlayer();
        finish();
    }

    private boolean checkEpisodes(int newEpisode){
        boolean isOk = false;

        if (currentEpisode + newEpisode <= episodes && currentEpisode + newEpisode >= 1) {
            isOk = true;
        }

        return isOk;
    }

    private void changeCurrentEpisodes(int change) {
        currentEpisode += change;
    }
}
