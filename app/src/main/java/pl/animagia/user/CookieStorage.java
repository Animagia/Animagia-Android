package pl.animagia.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import pl.animagia.MainActivity;

import java.util.*;

public class CookieStorage {

    public static final String LOGIN_CREDENTIALS_KEY = "LOGIN";
    public static final String PURCHASED_FILES_KEY = "FILES_PURCHASED_BY_ACCOUNT";
    public static final String COOKIE_NOT_FOUND = "COOKIE_NOT_FOUND";

    public static void setCookie(String cookie, Activity activity){
        SharedPreferences pref = activity.getSharedPreferences(MainActivity.class.getName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LOGIN_CREDENTIALS_KEY, cookie);
        editor.commit();
    }


    public static void saveAccountStatus(Activity activity, AccountStatus status) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putString(AccountStatus.getPrefKey(), status.getFriendlyName());
        editor.apply();
    }


    public static String getAccountStatus(Context ctx) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(MainActivity.class.getName(), Context .MODE_PRIVATE);
        return prefs.getString(AccountStatus.getPrefKey(), AccountStatus.UNKNOWN.getFriendlyName());
    }


    public static void saveNamesOfFilesPurchasedByAccount(Activity activity,
                                                          Collection<String> names) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putStringSet(PURCHASED_FILES_KEY, new HashSet<>(names));
        editor.apply();
    }


    public static Set<String> getNamesOfFilesPurchasedByAccount(Context ctx) {
        return ctx.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE)
                .getStringSet(PURCHASED_FILES_KEY, Collections.<String>emptySet());
    }


    public static String getCookie(Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences(MainActivity.class.getName(),
                Context.MODE_PRIVATE);
        String cookie = pref.getString(LOGIN_CREDENTIALS_KEY, COOKIE_NOT_FOUND);
        return cookie;
    }

    public static void clearLoginCredentials(Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences(MainActivity.class.getName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(LOGIN_CREDENTIALS_KEY);
        editor.apply();
    }
}
