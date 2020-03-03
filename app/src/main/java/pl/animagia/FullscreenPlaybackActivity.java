package pl.animagia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.VolleyError;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.*;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;
import pl.animagia.video.VideoSourcesKt;
import pl.animagia.video.VideoUrl;

import java.util.*;

public class FullscreenPlaybackActivity extends AppCompatActivity {

    private static final String PREFERRED_SUBTITLE_KEY = "preferredSubtitles";
    private static final int PREFERRED_SUBTITLE_HONORIFICS = 0;
    private static final int PREFERRED_SUBTITLE_NO_HONORIFICS = 1;
    private static final String PREFERRED_AUDIO_IS_POLISH_KEY = "preferredAudioIsPolish";


    private static final int REWINDER_INTERVAL = 1200;
    private static final int RESTARTER_INTERVAL = 4000;
    private PlayerView mMainView;
    private ImageButton forwardPlayerButton, rewindPlayerButton;
    private SimpleExoPlayer mPlayer;
    private int episodeCount;
    private int currentEpisode;
    private String currentTitle;
    private String currentVideoPageUrl;

    private int previewMilliseconds = Integer.MAX_VALUE;

    private String timeStampUnconverted;
    private String [] timeStamps;

    private String cookie;

    private Handler hideHandler = new Handler();

    private Handler restartHandler = new Handler();

    private Handler rewindHandler = new Handler();

    private final Runnable playerRestarter = new Runnable()
    {
        public void run()
        {
            if (mPlayer == null) {
                return;
            }

            if ((mPlayer.getPlayWhenReady() &&
                    mPlayer.getPlaybackState() == Player.STATE_READY) ||
                    (mPlayer.getPlayWhenReady() &&
                            mPlayer.getPlaybackState() == Player.STATE_BUFFERING)) {
                restartHandler.removeCallbacks(playerRestarter);
            } else {
                Toast.makeText(FullscreenPlaybackActivity.this, "restart playera",
                        Toast.LENGTH_SHORT).show();
                reinitializePlayer("");
            }
        }

    };

    private final Runnable rewinder = new Runnable()
    {
        public void run()
        {
            long sek = mPlayer.getCurrentPosition();
            if(sek >= previewMilliseconds){
                mPlayer.seekTo(previewMilliseconds - 1000);
                Alerts.primeVideoError(FullscreenPlaybackActivity.this);
                onPause();
            }
            rewindHandler.postDelayed(rewinder, REWINDER_INTERVAL);
        }
    };

    private long lastTimeSystemUiWasBroughtBack;
    private boolean subtitleChangesAllowed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Anime anime = Anime.valueOf(getIntent().getStringExtra(Anime.NAME_OF_INTENT_EXTRA));
        cookie = getIntent().getStringExtra(CookieStorage.LOGIN_CREDENTIALS_KEY);

        setContentView(R.layout.activity_fullscreen_playback);
        mMainView = findViewById(R.id.exoplayerview_activity_video);

        startPlaybackFlow(anime);
    }


    private void startPlaybackFlow(final Anime anime) {

        String videoPageUrl = anime.getVideoUrl();

        if(TokenStorage.getLocallyPurchasedAnime(this).contains(anime)) {
            String token = TokenStorage.getCombinedToken(this, anime);
            videoPageUrl = TokenAssembly.URL_BASE + token;
        }

        HTML.getHtmlCookie(videoPageUrl, this, cookie, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                String videoSourceUrl = VideoUrl.getUrl(result);
                prepareForPlayback(anime, videoSourceUrl);
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startPlaybackFlow(anime);
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


    private void prepareForPlayback(Anime video, String videoSourceUrl) {
        episodeCount = video.getEpisodeCount();
        currentEpisode = 1;
        currentTitle = video.formatFullTitle();
        currentVideoPageUrl = video.getVideoUrl();

        updateDisplayedTitle();

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

        mPlayer = createPlayer(
                VideoSourcesKt.prepareFromAsset(this, videoSourceUrl, video.getTitle()));

        createSpinners();

        if (isTheatricalFilm(video.getTitle())) {

            previewMilliseconds = video.getPreviewMillis();

            if (userBoughtAccessToFilm()) {
               subtitleChangesAllowed = true;
            } else {
                // handler.postDelayed(rewinder, REWINDER_INTERVAL);
            }
        }

        mPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    onPlayerStartedPlaying();
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

    private void updateDisplayedTitle() {
        TextView title = findViewById(R.id.film_name);
        title.setText(formatTitle());
    }

    private boolean userBoughtAccessToFilm() {
        String currentPremiumStatus = CookieStorage.getAccountStatus(this);
        if (AccountStatus.ACTIVE.getFriendlyName().equals(currentPremiumStatus) ||
                AccountStatus.EXPIRING.getFriendlyName().equals(currentPremiumStatus)) {
            return true;
        }

        for (Anime anime : TokenStorage.getLocallyPurchasedAnime(this)) {
            if(anime.formatFullTitle().equals(currentTitle)) {
                return true;
            }
        }

        return CookieStorage.getNamesOfFilesPurchasedByAccount(this).toString()
                .contains(currentTitle.split(" ")[0]);
    }


    private boolean userIsAGuest() {
        return cookie.equals(CookieStorage.COOKIE_NOT_FOUND);
    }


    private String formatTitle() {
        return episodeCount > 1 ? currentTitle + " odc. " + currentEpisode : currentTitle;
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

    @Override
    protected void onResume() {
        super.onResume();

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View play = controlView.findViewById(R.id.exo_play);
        play.performClick();

        restartHandler.postDelayed(playerRestarter, RESTARTER_INTERVAL);
    }


    private void onPlayerStartedPlaying() {
        hideHandler.removeCallbacksAndMessages(null);
        hideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMainView.hideController();
                hideSystemUi();
            }
        }, 2000);
        rewindHandler.postDelayed(rewinder, REWINDER_INTERVAL);
    }


    private void createSpinners() {
        View spinnerOfSubtitles = findViewById(R.id.spinner_subtitles);
        String[] subtitle = getResources().getStringArray(R.array.subtitles);
        ArrayAdapter<String> adapterSubtitles = new ArrayAdapter<>(
                this, R.layout.spinner_item, subtitle
        );

        adapterSubtitles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerOfSubtitles.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return true;
                }

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(FullscreenPlaybackActivity.this);

                builder.setTitle("Wybierz wersję językową");
                String[] items = {"Napisy „mniej spolszczone”, Napisy „bardziej spolszczone”"};
                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onTranslationChosen(which);
                    }
                });

                builder.show();

                return true;
            }
        });
    }

    private void onTranslationChosen(int which) {

        Map<String,String> params = new HashMap<>();

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        switch(which) {
            case 1:
                params.put("altsub", "yes");
                editor.putInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_NO_HONORIFICS);
                if(dubAvailable()) {
                    editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false);
                }
                break;
            case 2:
                params.put("dub", "yes");
                editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, true);
                break;
            default:
                params.put("altsub", "no");
                editor.putInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_NO_HONORIFICS);
                if(dubAvailable()) {
                    editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false);
                }
        }

        editor.apply();

        reinitializePlayer(buildQueryString(params));
    }


    private boolean dubAvailable() {
        return currentTitle.contains("Iroha");
    }


    private String buildQueryString(Map<String, String> params) {
        String query = "?";

        for (Map.Entry<String, String> param : params.entrySet()) {
            query += param.getKey() + "=" + param.getValue() + "&";
        }

        return query.substring(0,query.length()-1);
    }


    private Map<String, String> loadTranslationPreference() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        Map<String, String> params = new HashMap<>();

        if (dubAvailable() && prefs.getBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false)) {
            params.put("dub", "yes");
        } else if (PREFERRED_SUBTITLE_NO_HONORIFICS ==
                prefs.getInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_HONORIFICS)) {
            params.put("altsub", "yes");
        } else {
            params.put("altsub", "no");
        }

        return params;
    }


    private void reinitializePlayer(String query){

        HTML.getHtmlCookie(currentVideoPageUrl + query, getApplicationContext(), cookie, new VolleyCallback() {

            @Override
            public void onSuccess(String result) {
                releaseMediaPlayer();
                String url = VideoUrl.getUrl(result);
                mPlayer = createPlayer(VideoSourcesKt
                        .prepareFromAsset(FullscreenPlaybackActivity.this, url, currentTitle));

                mPlayer.setPlayWhenReady(true);

                updateDisplayedTitle();

            }

            @Override
            public void onFailure(VolleyError volleyError) {
                restartHandler.postDelayed(playerRestarter,4000);
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

        if(episodeCount == 1) {
            hidePreviousAndNextButtons();
            return;
        }

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mMainView);
        View next = controlView.findViewById(R.id.next_episode);
        View previous = controlView.findViewById(R.id.previous_episode);

        final AppCompatActivity ac = this;
        final Anime video = Anime.valueOf(getIntent().getStringExtra(Anime.NAME_OF_INTENT_EXTRA));
//
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

    private boolean isTheatricalFilm(String title) {
        if (title.contains("Amagi")){
            return false;
        }
        return true;
    }


    @Override
    public void onPause(){
        super.onPause();
        clearHandlers();
        haltPlayback();
    }


    private void haltPlayback() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }


    private void releaseMediaPlayer() {
        clearHandlers();
        if (mPlayer!= null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer= null;
        }
    }


    private void clearHandlers() {
        rewindHandler.removeCallbacksAndMessages(null);
        restartHandler.removeCallbacksAndMessages(null);
        hideHandler.removeCallbacksAndMessages(null);
    }


    private boolean canShiftByThisManyEpisodes(int episodeShift) {
        return (currentEpisode + episodeShift <= episodeCount &&
                currentEpisode + episodeShift >= 1);
    }

    private void changeCurrentEpisode(int change) {
        currentEpisode += change;
    }


    private View.OnClickListener newEpisodeListener(final AppCompatActivity activity,
                                                    final Anime video, final int episodeShift) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canShiftByThisManyEpisodes(episodeShift)) {

                    String query = buildQueryString(loadTranslationPreference());
                    String url =
                            video.getVideoUrl().substring(0, video.getVideoUrl().length() - 2) +
                                    (currentEpisode + episodeShift) + query;

                    HTML.getHtmlCookie(url, getApplicationContext(), cookie, new VolleyCallback() {

                        @Override
                        public void onSuccess(String result) {
                            currentTitle = video.formatFullTitle();
                            currentVideoPageUrl = video.getVideoUrl()
                                    .substring(0, video.getVideoUrl().length() - 2) +
                                    (currentEpisode + episodeShift);

                            releaseMediaPlayer();
                            String url = VideoUrl.getUrl(result);
                            mPlayer = createPlayer(VideoSourcesKt
                                    .prepareFromAsset(activity, url, video.getTitle()));

                            mPlayer.setPlayWhenReady(true);

                            changeCurrentEpisode(episodeShift);
                            updateDisplayedTitle();
                            hideHandler.postDelayed(playerRestarter, 4000);
                        }

                        @Override
                        public void onFailure(VolleyError volleyError) {
                            hideHandler.postDelayed(playerRestarter, 4000);
                        }
                    });

                }

            }
        };
    }

}