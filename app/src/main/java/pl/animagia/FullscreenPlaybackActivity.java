package pl.animagia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.*;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.user.Cookies;
import pl.animagia.video.VideoSourcesKt;
import pl.animagia.video.VideoUrl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class FullscreenPlaybackActivity extends AppCompatActivity {


    public static final int REWINDER_INTERVAL = 500;
    private PlayerView mMainView;
    private ImageButton forwardPlayerButton, rewindPlayerButton ;
    private SimpleExoPlayer mPlayer;
    private int episodes;
    private int currentEpisode;
    private String currentTitle;
    private String currentUrl;
    private Spinner spinnerOfQuality;
    private Spinner spinnerOfSubtitles;

    private int previewMilliseconds = Integer.MAX_VALUE;

    private String timeStampUnconverted;
    private String [] timeStamps;

    private AppCompatActivity control;
    private boolean on_off, firstOnStart = true;

    private Context context; //FIXME remove and use 'this' or 'getApplicationContext'
    private String cookie;

    private Handler mHideHandler = new Handler();

//    Runnable playerRestarter = new Runnable()
//    {
//        public void run()
//        {
//            if(mPlayer != null){
//
//                if (on_off) {
//                    if ((mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY) ||
//                            (mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_BUFFERING)) {
//
//                    } else {
//                        reinitializePlayer("");
//                    }
//                    on_off = false;
//                }
//            }
//        }
//
//    };
//
//    final Handler handler = new Handler();
//
//    final Runnable rewinder = new Runnable()
//    {
//        public void run()
//        {
//            long sek = mPlayer.getCurrentPosition();
//            if(sek >= previewMilliseconds){
//                mPlayer.seekTo(previewMilliseconds - 1000);
//                Alerts.primeVideoError(context);
//                onPause();
//            }
//            handler.postDelayed(this, REWINDER_INTERVAL);
//        }
//    };
//
//    final Runnable hideUi = new Runnable()
//    {
//        public void run()
//        {
//            if(mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() == Player.STATE_READY) {
//                hideSystemUi();
//            }
//        }
//    };
    private long lastTimeSystemUiWasBroughtBack;
    private boolean subtitleChangesAllowed = false;
    private boolean qualityChangesAllowed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        control = this;
        Intent intent = getIntent();
        final VideoData video = intent.getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        cookie = intent.getStringExtra(Cookies.LOGIN);

        setContentView(R.layout.activity_fullscreen_playback);
        mMainView = findViewById(R.id.exoplayerview_activity_video);

        startPlaybackFlow(video);


//        HTML.getHtmlCookie("https://animagia.pl/", this, cookie, new VolleyCallback() {
//            @Override
//            public void onSuccess(String result) {
//                prepareForPlayback(video, url);
//            }
//
//            @Override
//            public void onFailure(VolleyError volleyError) {
//                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        prepareForPlayback(video, url);
//                    }
//                };
//
//                if (volleyError instanceof NoConnectionError) {
//                    Alerts.internetConnectionError(FullscreenPlaybackActivity.this,
//                            onClickTryAgain);
//                }
//            }
//        });


    }


    private void startPlaybackFlow(final VideoData vd) {
        HTML.getHtmlCookie(vd.getVideoUrl(), this, cookie, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                String url1 = VideoUrl.getUrl(result);
                prepareForPlayback(vd, url1);
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startPlaybackFlow(vd);
                    }
                };
                AlertDialog dialog = new AlertDialog.Builder(FullscreenPlaybackActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Błąd połączenia")
                        .setPositiveButton(R.string.try_again, onClickTryAgain)
                        .setNegativeButton("Wróć do katalogu",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }


    private void prepareForPlayback(VideoData video, String url) {
        episodes = video.getEpisodes();
        currentEpisode = 1;
        currentTitle = video.formatFullTitle();
        currentUrl = video.getVideoUrl();

        TextView title = findViewById(R.id.film_name);
        title.setText(formatTitle());

        timeStampUnconverted = video.getTimeStamps();
        timeStamps = timeStampUnconverted.split(";");

        OwnTimeBar chapterMarker = findViewById(R.id.exo_progress);

        if(!timeStamps[0].equals(""))
        addTimeStamps(chapterMarker, timeStamps);

        forwardPlayerButton = findViewById(R.id.exo_ffwd);
        forwardPlayerButton.getDrawable().setAlpha(255);
        rewindPlayerButton = findViewById(R.id.exo_rew);

        mMainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP &&
                        readyToHideSystemUi()) {
                    hideSystemUi();
                }
                return true;
            }
        });

        listenToSystemUiChanges();

        mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(this, url, video.getTitle()));

       // createSpinners();

        if (isPremiumFilm(video.getTitle())) {

            previewMilliseconds = video.getPreviewMillis();

            if (userIsAGuest()) {
               // handler.postDelayed(rewinder, REWINDER_INTERVAL);
            } else {
                qualityChangesAllowed = true;
                subtitleChangesAllowed = true;
            }
        } else {
            qualityChangesAllowed = true;
        }

        mPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    mHideHandler.removeCallbacksAndMessages(null);
                    mHideHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMainView.hideController();
                            hideSystemUi();
                        }
                    }, 2000);
                }
            }
        });

        mPlayer.setPlayWhenReady(true);

        if(!timeStamps[0].equals("")){
            final ArrayList<String> al = new ArrayList<>(Arrays.asList(timeStamps));
            final ListIterator<String> chapterIterator =  al.listIterator();

            View.OnClickListener listener = new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    long time;
                    switch(view.getId()){
                        case R.id.exo_ffwd:

                            if(al.size() > 0){
                                if(calculateMsTimeStamp(al.get(timeStamps.length - 1)) < mPlayer.getCurrentPosition()){
                                    forwardPlayerButton.getDrawable().setAlpha(80);
                                    break;
                                }
                            }

                            while(chapterIterator.hasPrevious()){
                                chapterIterator.previous();
                            }

                            if(chapterIterator.hasNext()){
                                time = calculateMsTimeStamp(chapterIterator.next());
                                while(chapterIterator.hasNext() && time - 1000 < mPlayer.getCurrentPosition()){
                                    time = calculateMsTimeStamp(chapterIterator.next());
                                }

                                mPlayer.seekTo(time);
                                if(!chapterIterator.hasNext())
                                    forwardPlayerButton.getDrawable().setAlpha(80);
                            }
                            break;
                        case R.id.exo_rew:

                            if(al.size() > 0){
                                if(calculateMsTimeStamp(al.get(0)) > mPlayer.getCurrentPosition()){
                                    forwardPlayerButton.getDrawable().setAlpha(255);
                                    break;
                                }
                            }

                            while(chapterIterator.hasNext()){
                                chapterIterator.next();
                            }

                            if(chapterIterator.hasPrevious()){
                                forwardPlayerButton.getDrawable().setAlpha(255);

                                time = calculateMsTimeStamp(chapterIterator.previous());
                                while(chapterIterator.hasPrevious() && time + 1000 > mPlayer.getCurrentPosition()){
                                    time = calculateMsTimeStamp(chapterIterator.previous());
                                }

                                mPlayer.seekTo(time);
                            }else{
                                mPlayer.seekTo(0);
                            }
                            break;
                    }
                }
            };

            forwardPlayerButton.setOnClickListener(listener);
            rewindPlayerButton.setOnClickListener(listener);
        }
    }

    private boolean userIsAGuest() {
        return cookie.equals(Cookies.COOKIE_NOT_FOUND);
    }


    private String formatTitle() {
        return episodes > 1 ? currentTitle + " odc. " + currentEpisode : currentTitle;
    }

    /**
     * Checks if navbar has been visible for long enough to allow it to be hidden safely
     * (hiding navbar too soon can glitch it).
     */
    private boolean readyToHideSystemUi() {
        return SystemClock.elapsedRealtime() - 600 > lastTimeSystemUiWasBroughtBack;
    }

    private void hideSystemUi() {
        mMainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private static boolean systemUiVisible(int systemUiVisibility) {
        return (systemUiVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
//        View play = controlView.findViewById(R.id.exo_play);
//        play.performClick();
//
//        mHideHandler.postDelayed(playerRestarter,4000);
//        on_off = true;
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(firstOnStart){
//            mHideHandler = new Handler();
//            firstOnStart = false;
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mHideHandler.removeCallbacks(playerRestarter);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        mHideHandler.removeCallbacks(playerRestarter);
//        playerRestarter = null;
//    }


//    private void createSpinners() {
//        spinnerOfQuality = findViewById(R.id.spinner_quality);
//        String[] quality = getResources().getStringArray(R.array.quality);
//        ArrayAdapter<String> adapterQuality = new ArrayAdapter<>(
//                this, R.layout.spinner_item, quality
//        );
//
//        adapterQuality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinnerOfQuality.setAdapter(adapterQuality);
//
//
//        spinnerOfSubtitles = findViewById(R.id.spinner_subtitles);
//        String[] subtitle = getResources().getStringArray(R.array.subtitles);
//        ArrayAdapter<String> adapterSubtitles = new ArrayAdapter<>(
//                this, R.layout.spinner_item, subtitle
//        );
//
//        adapterSubtitles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinnerOfSubtitles.setAdapter(adapterSubtitles);
//
//
//        spinnerOfSubtitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            String currentQualitySetting;
//            boolean start = true;
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                currentQualitySetting = String.valueOf(spinnerOfQuality.getSelectedItem());
//                switch (i) {
//                    case 0:
//                        if (!start) {
//                            if (currentQualitySetting.equals("1080p")) {
//                                reinitializePlayer("?altsub=no&sd=no");
//                            } else {
//                                reinitializePlayer("?altsub=no&sd=yes");
//                            }
//                        }
//                        start = false;
//                        break;
//                    case 1:
//                        if (currentQualitySetting.equals("1080p")) {
//                            reinitializePlayer("?altsub=yes&sd=no");
//                        } else {
//                            reinitializePlayer("?altsub=yes&sd=yes");
//                        }
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//        spinnerOfSubtitles.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                if(subtitleChangesAllowed) {
//                    return false;
//                }
//
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//                    builder.setMessage("Nie ma możliwości zmian napisów w preview.");
//                    builder.setTitle("Komunikat.");
//                    builder.setNegativeButton("Wróć", null);
//
//
//                    builder.show();
//                }
//
//
//                return true;
//            }
//        });
//
//
//        spinnerOfQuality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            boolean start = true;
//            String currentSubtitleSetting;
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                currentSubtitleSetting = String.valueOf(spinnerOfSubtitles.getSelectedItem());
//                switch (i) {
//                    case 0:
//                        if (!start) {
//                            if (currentSubtitleSetting.equals("pl")) {
//                                reinitializePlayer("?altsub=yes&sd=no");
//                            } else {
//                                reinitializePlayer("?altsub=no&sd=no");
//                            }
//                        }
//                        start = false;
//                        break;
//                    case 1:
//                        if (currentSubtitleSetting.equals("pl")) {
//                            reinitializePlayer("?altsub=yes&sd=yes");
//                        } else {
//                            reinitializePlayer("?altsub=no&sd=yes");
//                        }
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//        spinnerOfQuality.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                if (qualityChangesAllowed) {
//                    return false;
//                }
//
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//                    builder.setMessage("Nie ma możliwości zmian jakości w preview.");
//                    builder.setTitle("Komunikat.");
//                    builder.setNegativeButton("Wróć", null);
//
//
//                    builder.show();
//                }
//
//                return true;
//            }
//        });
//    }


//    private void reinitializePlayer(String query){
//
//        if(currentTitle.contains("Hanasaku")) {
//            query = query.replace("altsub", "dub");
//        }
//
//        HTML.getHtmlCookie(currentUrl + query, getApplicationContext(), cookie, new VolleyCallback() {
//
//            @Override
//            public void onSuccess(String result) {
//                releaseMediaPlayer();
//                String url = VideoUrl.getUrl(result);
//                mPlayer = createPlayer(VideoSourcesKt.prepareFromAsset(control, url, currentTitle));
//                if (isPremiumFilm(currentTitle)) {
//                    if (userIsAGuest()) {
//                        handler.postDelayed(rewinder, REWINDER_INTERVAL);
//                    }
//                }
//
//                mPlayer.setPlayWhenReady(true);
//
//                    TextView title = findViewById(R.id.film_name);
//                    title.setText(formatTitle());
//
//                mHideHandler.postDelayed(playerRestarter,4000);
//                on_off = true;
//
//            }
//
//            @Override
//            public void onFailure(VolleyError volleyError) {
//                mHideHandler.postDelayed(playerRestarter,4000);
//                on_off = true;
//            }
//        });
//
//    }

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
                        if (systemUiVisible(visibility)) {
                            mMainView.showController();
                            lastTimeSystemUiWasBroughtBack = SystemClock.elapsedRealtime();
                        } else {
                            mMainView.hideController();
                        }
                    }
                });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus ) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
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
//
//        next.setOnClickListener(newEpisodeListener(ac, video, 1));
//        previous.setOnClickListener(newEpisodeListener(ac, video, -1));
    }

    private void hidePreviousAndNextButtons() {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View next = controlView.findViewById(R.id.next_episode);
        next.setVisibility(View.INVISIBLE);
        View previous = controlView.findViewById(R.id.previous_episode);
        previous.setVisibility(View.INVISIBLE);
    }

    private boolean isPremiumFilm(String title) {
        if (title.contains("Amagi")){
            return false;
        }
        return true;
    }

    @Override
    public void onPause(){
        super.onPause();
        haltPlayback();
        mHideHandler.removeCallbacksAndMessages(null);
        //mHideHandler.removeCallbacks(playerRestarter);
    }


    private void haltPlayback() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }


    private void releaseMediaPlayer() {
//        if (rewinder != null) {
//            handler.removeCallbacks(rewinder);
//        }
//        if (hideUi != null) {
//            handler.removeCallbacks(hideUi);
//        }
        if (mPlayer!= null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer= null;
        }
    }

//    @Override
//    public void onBackPressed() {
//        releaseMediaPlayer();
//        mHideHandler.removeCallbacks(playerRestarter);
//        playerRestarter = null;
//        finish();
//    }

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


//    private View.OnClickListener newEpisodeListener(final AppCompatActivity activity, final VideoData video, final int newEpisode) {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (checkEpisodes(newEpisode)) {
//
//                    String url =
//                            video.getVideoUrl().substring(0, video.getVideoUrl().length() - 2) +
//                                    (currentEpisode + newEpisode) + "?altsub=no&sd=no";
//
//                    HTML.getHtmlCookie(url, getApplicationContext(), cookie, new VolleyCallback() {
//
//                        @Override
//                        public void onSuccess(String result) {
//                            currentTitle = video.formatFullTitle();
//                            currentUrl = video.getVideoUrl()
//                                    .substring(0, video.getVideoUrl().length() - 2) +
//                                    (currentEpisode + newEpisode);
//
//                            releaseMediaPlayer();
//                            String url = VideoUrl.getUrl(result);
//                            mPlayer = createPlayer(VideoSourcesKt
//                                    .prepareFromAsset(activity, url, video.getTitle()));
//
//                            mPlayer.setPlayWhenReady(true);
//
//                            changeCurrentEpisodes(newEpisode);
//                            TextView title = findViewById(R.id.film_name);
//                            title.setText(video.getTitle() + " odc. " + currentEpisode);
//                            mHideHandler.postDelayed(playerRestarter, 4000);
//                            on_off = true;
//                        }
//
//                        @Override
//                        public void onFailure(VolleyError volleyError) {
//                            mHideHandler.postDelayed(playerRestarter, 4000);
//                            on_off = true;
//                        }
//                    });
//
//                }
//
//            }
//        };
//    }

}