package pl.animagia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import static pl.animagia.ViewUtilsKt.getPlayerControlView;

public class FullscreenPlaybackActivity extends AppCompatActivity {

    private static final int REWINDER_INTERVAL = 1200;
    private static final int RESTARTER_INTERVAL = 4000;

    private PlayerView mainView;
    private SimpleExoPlayer player;

    private long timestampToStartAt = -1;

    private Anime anime;
    private int currentEpisode;

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
                startPlaybackFlow(PreferenceUtils.loadTranslationPreference(
                        FullscreenPlaybackActivity.this, anime.hasDub()), currentEpisode, 0);
            }
        }
    };


    private final Runnable rewinder = new Runnable()
    {
        public void run()
        {
            if(player.getCurrentPosition() >= anime.getPreviewMillis()) {
                player.seekTo(anime.getPreviewMillis() - 1000);
                haltPlayback();
                showPurchasePrompt();
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

        anime = getIntent().getParcelableExtra(Anime.NAME_OF_INTENT_EXTRA);
        cookie = getIntent().getStringExtra(CookieStorage.LOGIN_CREDENTIALS_KEY);

        setContentView(R.layout.activity_fullscreen_playback);
        mainView = findViewById(R.id.exoplayerview_activity_video);

        getSupportFragmentManager().addOnBackStackChangedListener(listenerForOverlaidFragment);


        long startAt = firstChapterAfterLogo(anime);
        startPlaybackFlow(
                PreferenceUtils.loadTranslationPreference(this, anime.hasDub()), 1, startAt);
    }


    private void startPlaybackFlow(
            final Localization loc, final int episode, final long startAt) {

        timestampToStartAt = startAt;

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
                        startPlaybackFlow(loc, episode, startAt);
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
        anime = video;
        updateDisplayedTitle();
        chapterTimestamps = new ArrayList<>();

        String[] rawChapterTimestamps = anime.getTimeStamps().split(";");
        if(!rawChapterTimestamps[0].equals("")) {
            chapterTimestamps.add(0L);
            for (String stamp : rawChapterTimestamps) {
                chapterTimestamps.add((long) calculateMsTimestamp(stamp));
            }
            drawChapterMarkers();
        }


        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP &&
                        readyToHideSystemUi(lastTimeSystemUiWasBroughtBack) && !purchasePromptVisible()) {
                    hideSystemUi();
                }
                return true;
            }
        });

        listenToSystemUiChanges();

        installNewPlayer(VideoSourcesKt.prepareFromAsset(this, videoSourceUrl));

        createSubtitleSelector();

        this.<CustomSeekbar>findViewById(R.id.exo_progress).
                setPreviewMillis(video.getPreviewMillis());

        if (userBoughtAccessToAnime(anime, this)) {
            translationChangesAllowed = true;
        }

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    onPlayerStartedPlaying();
                }

                if(Player.STATE_READY == playbackState) {
                    findViewById(R.id.exo_buffering).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.exo_buffering).setVisibility(View.VISIBLE);
                }

                updatePlayPauseButtons(playWhenReady);
            }


            @Override
            public void onPositionDiscontinuity(int reason) {
                if(Player.DISCONTINUITY_REASON_SEEK == reason &&
                        player.getCurrentPosition() < anime.getPreviewMillis() - 1100) {
                    hidePurchasePrompt();
                }
            }
        });


        if(!chapterTimestamps.isEmpty()) {
            prepareChapterButtons();
        }

        preparePlayPauseButtons();

        player.setPlayWhenReady(true);
    }


    private void preparePlayPauseButtons() {
        View.OnClickListener playPauseListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.setPlayWhenReady(!player.getPlayWhenReady());
            }
        };
        findViewById(R.id.custom_play_pause).setOnClickListener(playPauseListener);
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
                player.seekTo(
                        prevChapter(player.getCurrentPosition(), chapterTimestamps));
            }
        };
        findViewById(R.id.custom_rew).setOnClickListener(rewListener);
    }


    private void showPurchasePrompt() {
        disableControllerButtons(R.id.custom_play_pause, R.id.custom_ffwd);

        ViewGroup purchasePrompt = findViewById(R.id.purchase_prompt);
        ImageView poster = purchasePrompt.findViewById(R.id.prompt_poster);

        if(purchasePrompt.getTag() == null) {
            Glide.with(this)
                    .load(anime.getPosterAsssetUri())
                    .error(Glide.with(this).load("file:///android_asset/clapperboard.jpg"))
                    .into(poster);
            purchasePrompt.setTag(Boolean.TRUE);
        }

        purchasePrompt.setVisibility(View.VISIBLE);
    }


    public void showSingleProductDialog(View v) {
        //Toast.makeText(this, "single product", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(android.R.id.content, SingleProductFragment.newInstance(anime));
        ft.addToBackStack(null);
        ft.commit();
    }


    private void hidePurchasePrompt() {
        findViewById(R.id.purchase_prompt).setVisibility(View.GONE);
        enableControllerButtons(R.id.custom_play_pause, R.id.custom_ffwd);
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
        String title = anime.formatFullTitle();
        return anime.getEpisodeCount() > 1 ? title + " odc. " + currentEpisode : title;
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

        restartHandler.postDelayed(playerRestarter, RESTARTER_INTERVAL);
    }


    private void onPlayerStartedPlaying() {

        if(timestampToStartAt > 0) {
            player.seekTo(timestampToStartAt);
            timestampToStartAt = -1;
        }

        hideHandler.removeCallbacksAndMessages(null);
        hideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainView.hideController();
                hideSystemUi();
            }
        }, 2000);

        if(watchtimeIsLimited(anime, this)) {
            rewindHandler.postDelayed(rewinder, REWINDER_INTERVAL);
        }
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
        Localization loc;
        switch (which) {
            case 1:
                loc = Localization.NO_HONORIFICS;
                break;
            case 2:
                loc = Localization.DUB;
                break;
            default:
                loc = Localization.HONORIFICS;
        }

        Context ctx = this;
        boolean dubAvailable = anime.hasDub();
        PreferenceUtils.saveTranslationPreference(ctx, loc, dubAvailable);

        startPlaybackFlow(loc, currentEpisode, player.getContentPosition());
    }


    private void recheckPurchaseStatus() {
        if(userBoughtAccessToAnime(anime, this)) {
            Toast.makeText(this, R.string.reopen_to_watch_all, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void drawChapterMarkers(){
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


    private void installNewPlayer(MediaSource mediaSource) {
        SimpleExoPlayer player = createPLayer(this);

        releaseMediaPlayer();

        mainView.setPlayer(player);

        player.prepare(mediaSource);

        alignControls(getResources().getConfiguration().orientation);
        mainView.showController();
        setUpInterEpisodeNavigation();
        recolorBufferingIndicator();

        this.player = player;
    }


    private void recolorBufferingIndicator() {
        ProgressBar pb = findViewById(R.id.exo_buffering);
        pb.getIndeterminateDrawable().setColorFilter(Color.HSVToColor(new float[]{0f, 0f, 0.8f}),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }


    private void alignControls(int screenOrientation) {
        PlayerControlView controlView = getPlayerControlView(mainView);
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) controlView.getLayoutParams();

        int bottom = getNavigationBarThickness(this);
        int leftRight = screenOrientation == Configuration.ORIENTATION_LANDSCAPE ? 16 + bottom : 0;
        lp.setMargins(leftRight, 0, leftRight, bottom);

        controlView.requestLayout();
    }


    private void setUpInterEpisodeNavigation() {
        if(anime.getEpisodeCount() == 1) {
            hidePreviousAndNextButtons();
            return;
        }

        PlayerControlView controlView = getPlayerControlView(mainView);
        View next = controlView.findViewById(R.id.next_episode);
        View previous = controlView.findViewById(R.id.previous_episode);

        final AppCompatActivity ac = this;
        final Anime video = getIntent().getParcelableExtra(Anime.NAME_OF_INTENT_EXTRA);

        next.setOnClickListener(newEpisodeListener(ac, video, 1));
        previous.setOnClickListener(newEpisodeListener(ac, video, -1));
    }


    private void hidePreviousAndNextButtons() {
        PlayerControlView controlView = getPlayerControlView(mainView);
        View next = controlView.findViewById(R.id.next_episode);
        next.setVisibility(View.INVISIBLE);
        View previous = controlView.findViewById(R.id.previous_episode);
        previous.setVisibility(View.INVISIBLE);
    }


    private void disableControllerButtons(Integer... resIds) {
        PlayerControlView controlView = getPlayerControlView(mainView);
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


    private void updatePlayPauseButtons(boolean playerSetToPlayWhenReady) {
        ImageButton btn = findViewById(R.id.custom_play_pause);
        if(playerSetToPlayWhenReady) {
            btn.setImageResource(R.drawable.exo_controls_pause);
        } else {
            btn.setImageResource(R.drawable.exo_controls_play);
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        haltPlayback();
    }


    private void haltPlayback() {
        clearHandlers();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }


    private void releaseMediaPlayer() {
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
        return (currentEpisode + episodeShift <= anime.getEpisodeCount() &&
                currentEpisode + episodeShift >= 1);
    }


    private View.OnClickListener newEpisodeListener(final AppCompatActivity activity,
                                                    final Anime video, final int episodeShift) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canShiftByThisManyEpisodes(episodeShift)) {

                    Localization loc = PreferenceUtils.loadTranslationPreference(
                            FullscreenPlaybackActivity.this, anime.hasDub());
                    int targetEpisode = currentEpisode + episodeShift;
                    startPlaybackFlow(loc, targetEpisode, 0);
                }

            }
        };
    }


}