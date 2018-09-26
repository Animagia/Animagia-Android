package pl.animagia.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Cookies {

    public static final String LOGIN = "LOGIN";
    public static final String COOKIE_NOT_FOUND = "COOKIE_NOT_FOUND";

    public static void setCookie(String key, String cookie, Activity activity){
        SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, cookie);
        editor.apply();
    }

    public static String getCookie(String key, Activity activity) {
        SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        String cookie = pref.getString(key, COOKIE_NOT_FOUND);
        return cookie;
    }

    public static void removeCookie(String key, Activity activity) {
        SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        System.out.println("USUWANKO");
        editor.apply();
    }
}
