package pl.animagia;

import android.content.Context;
import android.content.SharedPreferences;

class PreferenceUtils {

    private static final int PREFERRED_SUBTITLE_NO_HONORIFICS = 1;
    private static final String PREFERRED_AUDIO_IS_POLISH_KEY = "preferredAudioIsPolish";
    private static final String PREFERRED_SUBTITLE_KEY = "preferredSubtitles";
    private static final int PREFERRED_SUBTITLE_HONORIFICS = 0;

    private static final String REACHED_EPISODE_PREFIX = "reached-episode-";
    private static final String PROGRESS_PREFIX = "progress-";

    static final long MINIMUM_PROGRESS_TO_SAVE = 5000;


    private PreferenceUtils() {
    }


    static Localization loadTranslationPreference(Context ctx, boolean dubAvailable) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE);

        if (dubAvailable && prefs.getBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false)) {
            return Localization.DUB;
        } else if (PREFERRED_SUBTITLE_NO_HONORIFICS ==
                prefs.getInt(
                        PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_HONORIFICS)) {
            return Localization.NO_HONORIFICS;
        } else {
            return Localization.HONORIFICS;
        }
    }


    static void saveTranslationPreference(Context ctx, Localization preference,
                                          boolean dubAvailable) {
        SharedPreferences.Editor editor =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE)
                        .edit();
        switch (preference) {
            case NO_HONORIFICS:
                editor.putInt(PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_NO_HONORIFICS);
                if (dubAvailable) {
                    editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false);
                }
                break;
            case DUB:
                editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, true);
                break;
            default:
                editor.putInt(
                        PREFERRED_SUBTITLE_KEY, PREFERRED_SUBTITLE_HONORIFICS);
                if (dubAvailable) {
                    editor.putBoolean(PREFERRED_AUDIO_IS_POLISH_KEY, false);
                }
        }
        editor.apply();
    }


    static void saveProgress(Context ctx, Anime anime, int episode, long progress) {

        SharedPreferences.Editor edit =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE).edit();

        String key = PROGRESS_PREFIX + anime.getSku();

        edit.putLong(key, progress);

        edit.apply();
    }


    static int getReachedEpisode(Context ctx, Anime anime) {
        SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.class.getName(),
                Context.MODE_PRIVATE);
        return prefs.getInt(REACHED_EPISODE_PREFIX + anime.getSku(), 1);
    }


    static long getSavedProgress(Context ctx, Anime anime, int episode) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE);
        return prefs.getLong(PROGRESS_PREFIX + anime.getSku(), 0);
    }

}
