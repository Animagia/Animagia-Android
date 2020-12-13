package pl.animagia;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Map;

class PlaybackUtils {

    static final String PREFERRED_SUBTITLE_KEY = "preferredSubtitles";
    static final int PREFERRED_SUBTITLE_HONORIFICS = 0;
    static final int PREFERRED_SUBTITLE_NO_HONORIFICS = 1;
    static final String PREFERRED_AUDIO_IS_POLISH_KEY = "preferredAudioIsPolish";


    private PlaybackUtils() {
    }


    static Map<String, String> loadTranslationPreference(Context ctx, boolean dubAvailable) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE);

        if (dubAvailable && prefs.getBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false)) {
            return Collections.singletonMap("dub", "yes");
        } else if (PREFERRED_SUBTITLE_NO_HONORIFICS ==
                prefs.getInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_HONORIFICS)) {
            return Collections.singletonMap("altsub", "yes");
        } else {
            return Collections.singletonMap("altsub", "no");
        }
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


    static int calculateMsTimestamp(String unconvertedTimestamp) {
        return 3600 * 1000 * Integer.parseInt(unconvertedTimestamp.substring(0, 2))
                + 1000 * 60 * Integer.parseInt(unconvertedTimestamp.substring(3, 5))
                + 1000 * Integer.parseInt(unconvertedTimestamp.substring(6, 8))
                + Integer.parseInt(unconvertedTimestamp.substring(9));
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

}
