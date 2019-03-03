package pl.animagia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import org.jetbrains.annotations.Nullable;
import pl.animagia.error.Alerts;
import pl.animagia.user.Cookies;
import pl.animagia.video.VideoSourcesKt;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenPlaybackActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();

    private PlayerView mMainView;

    private SimpleExoPlayer mPlayer;
    private int episodes;
    private int currentEpisode;

    private Context context;
    private String cookie;

    private boolean systemUiAndControlsVisible;

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
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Intent intent = getIntent();
        final VideoData video = intent.getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        final String url = intent.getStringExtra(VideoData.NAME_OF_URL);
        final AppCompatActivity ac = this;
        cookie = intent.getStringExtra(Cookies.LOGIN);

        episodes = video.getEpisodes();
        currentEpisode = 1;

        setContentView(R.layout.activity_fullscreen_playback);
        mMainView = findViewById(R.id.exoplayerview_activity_video);


        mMainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(systemUiAndControlsVisible) {
                    hide();
                }
                return true;
            }
        });
        
        listenToSystemUiChanges();

        mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(this, url, video.getTitle()));
        if(!isPrime(video.getTitle())){
            if(cookie.equals(Cookies.COOKIE_NOT_FOUND)) {
                handler.postDelayed(r, 300);
            }
        }

        mPlayer.setPlayWhenReady(true);
    }

    private void listenToSystemUiChanges() {
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            //navbar visible
                            systemUiAndControlsVisible = true;
                            mMainView.showController();
                        } else {
                            //navbar not visible
                            hide();
                        }
                    }
                });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus ) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hide();
        }
    }


    private int getNavigationBarHeight() {
        int navigationBarHeight = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }


    private SimpleExoPlayer createPlayer(MediaSource mediaSource) {
        // 1. Create a default TrackSelector
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

        moveControlsAboveNavigationBar();

        return player;
    }


    private void moveControlsAboveNavigationBar() {

        PlayerControlView controlView = getPlayerControlView();


        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) controlView.getLayoutParams();

        lp.setMargins(0, 0, 0, getNavigationBarHeight());
        controlView.requestLayout();
    }


    private PlayerControlView getPlayerControlView() {
        PlayerControlView controlView = null;

        int childCount = mMainView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (mMainView.getChildAt(i) instanceof PlayerControlView) {
                if (controlView != null) {
                    throw new IllegalStateException("Media player has too many controllers.");
                }
                controlView = (PlayerControlView) mMainView.getChildAt(i);
            }
        }

        if (controlView == null) {
            throw new IllegalStateException("Media player has no controller.");
        }
        return controlView;
    }


    private void hide() {
        systemUiAndControlsVisible = false;
        hideSystemUi();
        mMainView.hideController();
    }

    private void hideSystemUi() {
        mMainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
