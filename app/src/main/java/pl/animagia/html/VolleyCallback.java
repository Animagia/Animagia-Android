package pl.animagia.html;

import android.content.Context;

import com.android.volley.VolleyError;

public interface VolleyCallback {
    void onSuccess(String result);
    void onFailure(VolleyError volleyError);
}
