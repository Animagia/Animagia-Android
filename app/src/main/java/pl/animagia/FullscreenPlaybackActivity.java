package pl.animagia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import pl.animagia.error.Alerts;
import pl.animagia.video.VideoSourcesKt;

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

import static pl.animagia.CatalogFragment.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenPlaybackActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();

    private View mMainView;
    private View mControlsView;

    private SimpleExoPlayer mPlayer;

    private boolean mVisible;

    private Context context;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_fullscreen_playback);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mMainView = findViewById(R.id.exoplayerview_activity_video);

        hideSystemUi();

        Intent intent = getIntent();
        VideoData video = intent.getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);

        String url = intent.getStringExtra(CatalogFragment.NAME_OF_URL);

        mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(this, url, video.getTitle()));

        if(!isPrime(video.getTitle())){
            handler.postDelayed(r, 300);
        }

        mPlayer.addListener(createPlayPauseListener());

        mMainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toggle();
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
                    hide();
                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                } else {
                    // player paused in any state
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

        PlayerView playerView = findViewById(R.id.exoplayerview_activity_video);

        playerView.setPlayer(player);


        player.prepare(mediaSource);


        PlayerControlView controlView = findViewById(R.id.playback_controls);
        controlView.setPlayer(player);

        return player;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        hideSystemUi();
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
    }

    private void show() {
        showSystemUi();
        mControlsView.setVisibility(View.VISIBLE);
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
        if (title.equals("Chuuni")){
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
            handler.removeCallbacks(r );
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
}
