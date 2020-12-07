package pl.animagia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import pl.animagia.dialog.Dialogs;
import pl.animagia.html.HtClient;
import pl.animagia.html.VolleyCallback;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.CookieStorage;
import pl.animagia.video.VideoSourcesKt;
import pl.animagia.video.VideoUrl;

import java.util.*;

import static pl.animagia.PlaybackUtils.*;

public class FullscreenPlaybackActivity extends AppCompatActivity {


    private static final int REWINDER_INTERVAL = 1200;
    private static final int RESTARTER_INTERVAL = 4000;
    private PlayerView mainView;
    private SimpleExoPlayer player;
    private int currentEpisode;
    private Anime currentAnime;

    private int previewMilliseconds = Integer.MAX_VALUE;

    private List<Long> chapterTimestamps;

    private String cookie;

    private Handler hideHandler = new Handler();

    private Handler restartHandler = new Handler();

    private Handler rewindHandler = new Handler();

    private final Runnable playerRestarter = new Runnable()
    {
        public void run()
        {
            if (player == null) {
                return;
            }

            if ((player.getPlayWhenReady() &&
                    player.getPlaybackState() == Player.STATE_READY) ||
                    (player.getPlayWhenReady() &&
                            player.getPlaybackState() == Player.STATE_BUFFERING)) {
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
            long sek = player.getCurrentPosition();

            if(sek >= previewMilliseconds) {

                //FIXME should disable rewinder entirely when watchtime not limited
                boolean limitedWatchtime = true;
                if(1 != currentAnime.getEpisodeCount()) {
                    limitedWatchtime = false;
                } else if(userBoughtAccessToFilm(currentAnime, FullscreenPlaybackActivity.this)) {
                    limitedWatchtime = false;
                }

                if(limitedWatchtime) {
                    player.seekTo(previewMilliseconds - 1000);
                    showPurchasePrompt();
                    onPause();
                }
            }
            rewindHandler.postDelayed(rewinder, REWINDER_INTERVAL);
        }
    };


    private long lastTimeSystemUiWasBroughtBack;
    private boolean translationChangesAllowed = false;

    private FragmentManager.OnBackStackChangedListener listenerForOverlaidFragment =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if(0 == getSupportFragmentManager().getBackStackEntryCount()) {
                        layOutActivityAsIfSystemBarsWereGone();
                        recheckPurchaseStatus();
                    } else {
                        layOutActivityLeavingRoomForSystemBars();
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Anime anime = getIntent().getParcelableExtra(Anime.NAME_OF_INTENT_EXTRA);
        cookie = getIntent().getStringExtra(CookieStorage.LOGIN_CREDENTIALS_KEY);

        setContentView(R.layout.activity_fullscreen_playback);
        mainView = findViewById(R.id.exoplayerview_activity_video);

        getSupportFragmentManager().addOnBackStackChangedListener(listenerForOverlaidFragment);

        startPlaybackFlow(anime);
    }


    private void startPlaybackFlow(final Anime anime) {

        String videoPageUrl = anime.getVideoUrl();

        if(TokenStorage.getSkusOfLocallyPurchasedAnime(this).contains(anime.getSku())) {
            String token = TokenStorage.getCombinedToken(this, anime.getSku());
            videoPageUrl = TokenAssembly.URL_BASE + token;
        }

        HtClient.getUsingCookie(videoPageUrl, this, cookie, new VolleyCallback() {
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
        currentEpisode = 1;
        currentAnime = video;
        updateDisplayedTitle();
        chapterTimestamps = new ArrayList<>();

        String[] rawChapterTimestamps = currentAnime.getTimeStamps().split(";");
        if(!rawChapterTimestamps[0].equals("")) {
            chapterTimestamps.add(0L);
            for (String stamp : rawChapterTimestamps) {
                chapterTimestamps.add((long) calculateMsTimestamp(stamp));
            }
            createChapterMarkers();
        }


        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP &&
                        readyToHideSystemUi() && !purchasePromptVisible()) {
                    hideSystemUi();
                }
                return true;
            }
        });

        listenToSystemUiChanges();

        installNewPlayer(VideoSourcesKt.prepareFromAsset(this, videoSourceUrl));

        createSubtitleSelector();

        if (isTheatricalFilm(video.getTitle())) {
            previewMilliseconds = video.getPreviewMillis();
            CustomSeekbar seekbar = findViewById(R.id.exo_progress);
            seekbar.setPreviewMillis(video.getPreviewMillis());
        }

        if (userBoughtAccessToFilm(currentAnime, this)) {
            translationChangesAllowed = true;
        }

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    onPlayerStartedPlaying();
                }
            }


            @Override
            public void onPositionDiscontinuity(int reason) {
                if(Player.DISCONTINUITY_REASON_SEEK == reason &&
                        player.getCurrentPosition() < previewMilliseconds - 1100) {
                    hidePurchasePrompt();
                }
            }
        });


        if(!chapterTimestamps.isEmpty()) {
            prepareChapterButtons();
        }

        player.setPlayWhenReady(true);
    }


    private void prepareChapterButtons() {
        View.OnClickListener ffwdListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentMillis = player.getCurrentPosition();
                for (Long chapterTimestamp : chapterTimestamps) {
                    if (chapterTimestamp > currentMillis) {
                        player.seekTo(chapterTimestamp);
                        break;
                    }
                }
            }
        };
        findViewById(R.id.custom_ffwd).setOnClickListener(ffwdListener);

        View.OnClickListener rewListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentMillis = player.getCurrentPosition();
                ArrayList<Long> list = new ArrayList<>(chapterTimestamps);
                Collections.reverse(list);
                for (Long chapterTimestamp : list) {
                    if (chapterTimestamp < currentMillis) {
                        player.seekTo(chapterTimestamp);
                        break;
                    }
                }
            }
        };
        findViewById(R.id.custom_rew).setOnClickListener(rewListener);
    }


    private void showPurchasePrompt() {
        disableControllerButtons(R.id.exo_play, R.id.exo_pause);
        if(1 == currentAnime.getEpisodeCount()) {
            disableControllerButtons(R.id.custom_ffwd);
        }

        ImageView poster = findViewById(R.id.prompt_poster);

        if(poster.getTag() == null) {
            Glide.with(this)
                    .load(currentAnime.getPosterAsssetUri())
                    .error(Glide.with(this).load("file:///android_asset/clapperboard.jpg"))
                    .into(poster);
            poster.setTag(Boolean.TRUE);
        }

        findViewById(R.id.purchase_prompt).setVisibility(View.VISIBLE);
    }


    public void showSingleProductDialog(View v) {
        //Toast.makeText(this, "single product", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(android.R.id.content, SingleProductFragment.newInstance(currentAnime));
        ft.addToBackStack(null);
        ft.commit();
    }


    private void hidePurchasePrompt() {
        findViewById(R.id.purchase_prompt).setVisibility(View.GONE);
        enableControllerButtons(R.id.exo_play, R.id.exo_pause);
        if(1 == currentAnime.getEpisodeCount()) {
            enableControllerButtons(R.id.custom_ffwd);
        }
    }


    private boolean purchasePromptVisible() {
        return View.VISIBLE == findViewById(R.id.purchase_prompt).getVisibility();
    }


    private void updateDisplayedTitle() {
        TextView title = findViewById(R.id.film_name);
        title.setText(formatTitle());
        title = findViewById(R.id.film_title_in_prompt);
        title.setText(formatTitle());
    }

    private String formatTitle() {
        String title = currentAnime.formatFullTitle();
        return currentAnime.getEpisodeCount() > 1 ? title + " odc. " + currentEpisode : title;
    }

    /**
     * Checks if navbar has been visible for long enough to allow it to be hidden safely
     * (hiding navbar too soon can glitch it).
     */
    private boolean readyToHideSystemUi() {
        return SystemClock.elapsedRealtime() - 600 > lastTimeSystemUiWasBroughtBack;
    }


    private void hideSystemUi() {
        mainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mainView);
        View play = controlView.findViewById(R.id.exo_play);
        play.performClick();

        restartHandler.postDelayed(playerRestarter, RESTARTER_INTERVAL);
    }


    private void onPlayerStartedPlaying() {
        hideHandler.removeCallbacksAndMessages(null);
        hideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainView.hideController();
                hideSystemUi();
            }
        }, 2000);
        rewindHandler.postDelayed(rewinder, REWINDER_INTERVAL);
    }


    private void createSubtitleSelector() {
        View spinnerOfSubtitles = findViewById(R.id.spinner_subtitles);
        String[] subtitle = new String[0]; //FIXME getResources().getStringArray(R.array.subtitles);
        ArrayAdapter<String> adapterSubtitles = new ArrayAdapter<>(
                this, R.layout.spinner_item, subtitle
        );

        adapterSubtitles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View.OnTouchListener listener = createSubtitleSelectionListener();
        spinnerOfSubtitles.setOnTouchListener(listener);
    }


    private View.OnTouchListener createSubtitleSelectionListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!translationChangesAllowed) {
                    Toast.makeText(FullscreenPlaybackActivity.this,
                            R.string.only_subtitles_available,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return true;
                }

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onTranslationChosen(which);
                    }
                };

                Dialogs.showSubtitleSelection(FullscreenPlaybackActivity.this, listener);

                return true;
            }
        };
    }


    private void onTranslationChosen(int which) {

        Map<String,String> params = new HashMap<>();

        SharedPreferences prefs = getSharedPreferences(MainActivity.class.getName(), Context
                .MODE_PRIVATE);
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
        return currentAnime.formatFullTitle().contains("Iroha");
    }


    private String buildQueryString(Map<String, String> params) {
        String query = "?";

        for (Map.Entry<String, String> param : params.entrySet()) {
            query += param.getKey() + "=" + param.getValue() + "&";
        }

        return query.substring(0,query.length()-1);
    }


    private void recheckPurchaseStatus() {
        if(userBoughtAccessToFilm(currentAnime, this)) {
            Toast.makeText(this, R.string.reopen_to_watch_all, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void reinitializePlayer(String query){

        HtClient.getUsingCookie(getVideoPageUrl() + query, getApplicationContext(), cookie,
                new  VolleyCallback() {

            @Override
            public void onSuccess(String result) {
                releaseMediaPlayer();
                String url = VideoUrl.getUrl(result);
                installNewPlayer(VideoSourcesKt
                        .prepareFromAsset(FullscreenPlaybackActivity.this, url));

                player.setPlayWhenReady(true);

                updateDisplayedTitle();

            }

            @Override
            public void onFailure(VolleyError volleyError) {
                restartHandler.postDelayed(playerRestarter,4000);
            }
        });

    }


    private String getVideoPageUrl() {
        if(1 == currentEpisode) {
            return currentAnime.getVideoUrl();
        }
        return currentAnime.getVideoUrl().substring(0, currentAnime.getVideoUrl().length() - 2) +
                currentEpisode;
    }


    private void createChapterMarkers(){
        CustomSeekbar bar = findViewById(R.id.exo_progress);
        for (long timeStamp : chapterTimestamps) {
            bar.addChapterMarker(timeStamp);
        }
    }


    private void listenToSystemUiChanges() {
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (systemUiVisible(visibility)) {
                            mainView.showController();
                            lastTimeSystemUiWasBroughtBack = SystemClock.elapsedRealtime();
                        } else {
                            mainView.hideController();
                        }
                    }
                });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus ) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            layOutActivityAsIfSystemBarsWereGone();
        }
    }


    private void layOutActivityAsIfSystemBarsWereGone() {
        int vis = mainView.getSystemUiVisibility();
        int flagsToAdd = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        mainView.setSystemUiVisibility(vis | flagsToAdd);
    }


    private void layOutActivityLeavingRoomForSystemBars() {
        int vis = mainView.getSystemUiVisibility();
        int flagsToClear = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        mainView.setSystemUiVisibility(vis & ~flagsToClear);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        alignControls(newConfig.orientation);
    }


    private int getNavigationBarThickness() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    private void installNewPlayer(MediaSource mediaSource) {
        SimpleExoPlayer player = createPLayer(this);

        mainView.setPlayer(player);

        player.prepare(mediaSource);

        alignControls(getResources().getConfiguration().orientation);
        mainView.showController();
        setUpInterEpisodeNavigation();

        this.player = player;
    }


    private void alignControls(int screenOrientation) {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mainView);
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) controlView.getLayoutParams();

        int bottom = getNavigationBarThickness();
        int leftRight = screenOrientation == Configuration.ORIENTATION_LANDSCAPE ? 16 + bottom : 0;
        lp.setMargins(leftRight, 0, leftRight, bottom);

        controlView.requestLayout();
    }


    private void setUpInterEpisodeNavigation() {

        if(currentAnime.getEpisodeCount() == 1) {
            hidePreviousAndNextButtons();
            return;
        }

        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mainView);
        View next = controlView.findViewById(R.id.next_episode);
        View previous = controlView.findViewById(R.id.previous_episode);

        final AppCompatActivity ac = this;
        final Anime video = getIntent().getParcelableExtra(Anime.NAME_OF_INTENT_EXTRA);

        next.setOnClickListener(newEpisodeListener(ac, video, 1));
        previous.setOnClickListener(newEpisodeListener(ac, video, -1));
    }


    private void hidePreviousAndNextButtons() {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mainView);
        View next = controlView.findViewById(R.id.next_episode);
        next.setVisibility(View.INVISIBLE);
        View previous = controlView.findViewById(R.id.previous_episode);
        previous.setVisibility(View.INVISIBLE);
    }


    private void disableControllerButtons(Integer... resIds) {
        PlayerControlView controlView = ViewUtilsKt.getPlayerControlView(mainView);
        for (int resId : resIds) {
            View button = controlView.findViewById(resId);
            button.setClickable(false);
            button.setAlpha(0.5f);
            button.setVisibility(View.INVISIBLE);
        }
    }


    private void enableControllerButtons(Integer... resIds) {
        for (int resId : resIds) {
            View v = findViewById(resId);
            v.setClickable(true);
            v.setAlpha(1);
            v.setVisibility(View.VISIBLE);
        }
    }


    private boolean isTheatricalFilm(String title) {
        return !title.contains("Amagi");
    }


    @Override
    public void onPause(){
        super.onPause();
        clearHandlers();
        haltPlayback();
    }


    private void haltPlayback() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }


    private void releaseMediaPlayer() {
        clearHandlers();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }


    private void clearHandlers() {
        rewindHandler.removeCallbacksAndMessages(null);
        restartHandler.removeCallbacksAndMessages(null);
        hideHandler.removeCallbacksAndMessages(null);
    }


    private boolean canShiftByThisManyEpisodes(int episodeShift) {
        return (currentEpisode + episodeShift <= currentAnime.getEpisodeCount() &&
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

                    String query = buildQueryString( loadTranslationPreference
                            (FullscreenPlaybackActivity.this, dubAvailable()));
                    String url =
                            video.getVideoUrl().substring(0, video.getVideoUrl().length() - 2) +
                                    (currentEpisode + episodeShift) + query;

                    HtClient.getUsingCookie(url, getApplicationContext(), cookie, new VolleyCallback() {

                        @Override
                        public void onSuccess(String result) {
                            currentAnime = video;

                            releaseMediaPlayer();
                            String url = VideoUrl.getUrl(result);
                            installNewPlayer(VideoSourcesKt
                                    .prepareFromAsset(activity, url));

                            player.setPlayWhenReady(true);

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