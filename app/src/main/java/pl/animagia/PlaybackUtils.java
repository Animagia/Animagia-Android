package pl.animagia;

import android.app.Activity;
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

import java.util.HashMap;
import java.util.Map;

public class PlaybackUtils {
    static final String PREFERRED_SUBTITLE_KEY = "preferredSubtitles";
    static final int PREFERRED_SUBTITLE_HONORIFICS = 0;
    static final int PREFERRED_SUBTITLE_NO_HONORIFICS = 1;
    static final String PREFERRED_AUDIO_IS_POLISH_KEY = "preferredAudioIsPolish";


    static Map<String, String> loadTranslationPreference(Context ctx,
                                                         boolean dubAvailable) {
        SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE);

        Map<String, String> params = new HashMap<>();

        if (dubAvailable && prefs.getBoolean(
                PREFERRED_AUDIO_IS_POLISH_KEY, false)) {
            params.put("dub", "yes");
        } else if (PREFERRED_SUBTITLE_NO_HONORIFICS ==
                prefs.getInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_HONORIFICS)) {
            params.put("altsub", "yes");
        } else {
            params.put("altsub", "no");
        }

        return params;
    }


    static boolean userBoughtAccessToFilm(Anime currentAnime, String currentTitle,
                                          Activity context) {
        boolean accessBought = false;

        String currentPremiumStatus = CookieStorage.getAccountStatus(context);

        if (AccountStatus.ACTIVE.getFriendlyName().equals(currentPremiumStatus)) {
            accessBought = true;
        }

        if (AccountStatus.EXPIRING.getFriendlyName().equals(currentPremiumStatus)) {
            accessBought = true;
        }

        for (String sku : TokenStorage.getSkusOfLocallyPurchasedAnime(context)) {
            if(currentAnime.getSku().equals(sku)) {
                accessBought = true;
            }
        }

        String word = currentTitle.split(" ")[0];
        if( CookieStorage.getNamesOfFilesPurchasedByAccount(context).toString().contains( word)) {
            accessBought = true;
        }

        return accessBought;
    }


    static int calculateMsTimeStamp(String unconvertedTimestamp){

        int totalTimeInMs;

        totalTimeInMs = 3600 * 1000 * Integer.parseInt(unconvertedTimestamp.substring(0,2))
                + 1000 * 60 * Integer.parseInt(unconvertedTimestamp.substring(3,5))
                + 1000 * Integer.parseInt(unconvertedTimestamp.substring(6,8))
                +  Integer.parseInt(unconvertedTimestamp.substring(9));

        return totalTimeInMs;
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
}
