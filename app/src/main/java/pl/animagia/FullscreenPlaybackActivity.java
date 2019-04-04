package pl.animagia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.user.Cookies;
import pl.animagia.video.VideoSourcesKt;
import pl.animagia.video.VideoUrl;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenPlaybackActivity extends AppCompatActivity {


    private PlayerView mMainView;

    private SimpleExoPlayer mPlayer;
    private int episodes;
    private int currentEpisode;
    private String currentTitle;
    private String currentUrl;
    private String timeStampUnconverted;
    private String [] timeStamps;
    private AppCompatActivity control;
    private boolean on_off, firstOnStart = true;

    private Context context;
    private String cookie;

    private boolean systemUiAndControlsVisible;

    private Handler mHideHandler;

    Runnable rExpire = new Runnable()
    {
        public void run()
        {
            if(mPlayer != null){

                if (on_off == true) {
                    if ((mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY) ||
                            (mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_BUFFERING) ) {

                    }else{
                        Toast.makeText(context, "Restart playera", Toast.LENGTH_SHORT).show();
                        reinitializationPlayer();
                        }
                    on_off = false;
                }
            }
        }

    };

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
        control = this;
        Intent intent = getIntent();
        final VideoData video = intent.getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        final String url = intent.getStringExtra(VideoData.NAME_OF_URL);
        cookie = intent.getStringExtra(Cookies.LOGIN);

        episodes = video.getEpisodes();
        currentEpisode = 1;
        currentTitle = video.getTitle();
        currentUrl = video.getVideoUrl();

        setContentView(R.layout.activity_fullscreen_playback);
        mMainView = findViewById(R.id.exoplayerview_activity_video);

        timeStampUnconverted = video.getTimeStamps();
        timeStamps = timeStampUnconverted.split(";");

        OwnTimeBar chapterMarker = findViewById(R.id.exo_progress);
        addTimeStamps(chapterMarker, timeStamps);

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

    @Override
    protected void onResume() {
        super.onResume();

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View play = controlView.findViewById(R.id.exo_play);
        play.performClick();

        mHideHandler.postDelayed(rExpire,4000);
        on_off = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(firstOnStart){
            runTimer();
            firstOnStart = false;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View play = controlView.findViewById(R.id.exo_play);
        play.performClick();

        mHideHandler.postDelayed(rExpire,4000);
        on_off = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHideHandler.removeCallbacks(rExpire);
        rExpire = null;
    }

    private void runTimer(){

         mHideHandler = new Handler();
         mHideHandler.postDelayed(rExpire,4000);

     }


    private void reinitializationPlayer(){

        HTML.getHtml(currentUrl, getApplicationContext(), new VolleyCallback() {

            @Override
            public void onSuccess(String result) {
                releaseMediaPlayer();
                String url = VideoUrl.getUrl(result);
                mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(control, url, currentTitle));
                if (!isPrime(currentTitle)) {
                    if (cookie.equals(Cookies.COOKIE_NOT_FOUND)) {
                        handler.postDelayed(r, 300);
                    }
                }

                mPlayer.setPlayWhenReady(true);

                if (currentEpisode > 1){
                    TextView title = findViewById(R.id.film_name);
                    title.setText(currentTitle + " odc. " + currentEpisode);
                }
                mHideHandler.postDelayed(rExpire,4000);
                on_off = true;

            }

            @Override
            public void onFailure(VolleyError volleyError) {
                mHideHandler.postDelayed(rExpire,4000);
                on_off = true;
            }
        });

    }

    private int calculateMsTimeStamp(String timeStampUnconvert){

        int totalTimeInMs;

        totalTimeInMs = 3600 * 1000 * Integer.parseInt(timeStampUnconvert.substring(0,2))
                + 1000 * 60 * Integer.parseInt(timeStampUnconvert.substring(3,5))
                + 1000 * Integer.parseInt(timeStampUnconvert.substring(6,8))
                +  Integer.parseInt(timeStampUnconvert.substring(9));

        return totalTimeInMs;
    }

    private void addTimeStamps(OwnTimeBar timeBar, String[] timeStamps){
        for(int i = 0; i < timeStamps.length; i++){
            timeBar.addChapterMarker(calculateMsTimeStamp(timeStamps[i]));
        }

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
        setUpInterEpisodeNavigation();

        return player;
    }

    private void moveControlsAboveNavigationBar() {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) controlView.getLayoutParams();

        lp.setMargins(0, 0, 0, getNavigationBarHeight());
        controlView.requestLayout();
    }

    private void setUpInterEpisodeNavigation() {

        if(episodes == 1) {
            hidePreviousAndNextButtons();
            return;
        }

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View next = controlView.findViewById(R.id.next_episode);
        View previous = controlView.findViewById(R.id.previous_episode);

        final AppCompatActivity ac = this;
        final VideoData video = getIntent().getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);

        next.setOnClickListener(newEpisodeListener(ac, video, 1));
        previous.setOnClickListener(newEpisodeListener(ac, video, -1));
    }

    private void hidePreviousAndNextButtons() {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View next = controlView.findViewById(R.id.next_episode);
        next.setVisibility(View.INVISIBLE);
        View previous = controlView.findViewById(R.id.previous_episode);
        previous.setVisibility(View.INVISIBLE);
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
        mHideHandler.removeCallbacks(rExpire);
        rExpire = null;
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


    private View.OnClickListener newEpisodeListener(final AppCompatActivity activity, final VideoData video, final int newEpisode) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEpisodes(newEpisode)) {

                    HTML.getHtml(video.getVideoUrl().substring(0, video.getVideoUrl().length() - 2) + (currentEpisode + newEpisode), getApplicationContext(), new VolleyCallback() {

                        @Override
                        public void onSuccess(String result) {

                            currentTitle = video.getTitle();
                            currentUrl = video.getVideoUrl().substring(0, video.getVideoUrl().length() - 2) + (currentEpisode + newEpisode);

                            releaseMediaPlayer();
                            String url = VideoUrl.getUrl(result);
                            mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(activity, url, video.getTitle()));
                            if (!isPrime(video.getTitle())) {
                                if (cookie.equals(Cookies.COOKIE_NOT_FOUND)) {
                                    handler.postDelayed(r, 300);
                                }
                            }

                            mPlayer.setPlayWhenReady(true);

                            changeCurrentEpisodes(newEpisode);
                            TextView title = findViewById(R.id.film_name);
                            title.setText(video.getTitle() + " odc. " + currentEpisode);
                            mHideHandler.postDelayed(rExpire,4000);
                            on_off = true;
                        }

                        @Override
                        public void onFailure(VolleyError volleyError) {
                            mHideHandler.postDelayed(rExpire,4000);
                            on_off = true;
                        }
                    });


                }

            }
        };
    }

}
