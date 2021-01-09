package pl.animagia;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PlaybackUtils {

    static final int REWINDER_INTERVAL = 1200;


    private PlaybackUtils() {
    }


    static boolean userBoughtAccessToAnime(Anime anime, Context context) {
        String currentPremiumStatus = CookieStorage.getAccountStatus(context);
        if (AccountStatus.ACTIVE.getFriendlyName().equals(currentPremiumStatus)) {
            return true;
        }
        if (AccountStatus.EXPIRING.getFriendlyName().equals(currentPremiumStatus)) {
            return true;
        }
        for (String sku : TokenStorage.getSkusOfLocallyPurchasedAnime(context)) {
            if (anime.getSku().equals(sku)) {
                return true;
            }
        }
        String word = anime.formatFullTitle().split(" ")[0];
        return CookieStorage.getNamesOfFilesPurchasedByAccount(context).toString().contains(word);
    }


    static SimpleExoPlayer createPLayer(Context ctx) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        return ExoPlayerFactory.newSimpleInstance(ctx, trackSelector);
    }


    static boolean systemUiVisible(int systemUiVisibility) {
        return (systemUiVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }


    static boolean watchtimeIsLimited(Anime a, Context ctx) {
        boolean limitedWatchtime = true;
        if(1 != a.getEpisodeCount()) {
            limitedWatchtime = false;
        } else if(userBoughtAccessToAnime(a, ctx)) {
            limitedWatchtime = false;
        }
        return limitedWatchtime;
    }


    static long prevChapter(long currentMillis, List<Long> chapterTimestamps) {
        ArrayList<Long> list = new ArrayList<>(chapterTimestamps);
        Collections.reverse(list);
        for (Long chapterTimestamp : list) {
            if (chapterTimestamp + 1000 < currentMillis) {
                return chapterTimestamp;
            }
        }
        return 0;
    }


    static long firstChapterAfterLogo(Anime anime) {
        String[] rawChapterTimestamps = anime.getTimeStamps().split(";");
        return rawChapterTimestamps[0].equals("") ?
                0 : calculateMsTimestamp(rawChapterTimestamps[0]);
    }


    static String getVideoUrlOfEpisode(Anime anime, int targetEpisode) {
        return anime.getVideoUrl().substring(0, anime.getVideoUrl().length() - 2) +
                targetEpisode;
    }


    static int getNavigationBarThickness(Context ctx) {
        int resourceId =
                ctx.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    /**
     * Checks if navbar has been visible for long enough to allow it to be hidden safely
     * (hiding navbar too soon can glitch it).
     */
    static boolean readyToHideSystemUi(long lastTimeSystemUiWasBroughtBack) {
        return SystemClock.elapsedRealtime() - 600 > lastTimeSystemUiWasBroughtBack;
    }


    static int calculateMsTimestamp(String unconvertedTimestamp) {
        return 3600 * 1000 * Integer.parseInt(unconvertedTimestamp.substring(0, 2))
                + 1000 * 60 * Integer.parseInt(unconvertedTimestamp.substring(3, 5))
                + 1000 * Integer.parseInt(unconvertedTimestamp.substring(6, 8))
                + Integer.parseInt(unconvertedTimestamp.substring(9));
    }


}
