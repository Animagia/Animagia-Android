
package pl.animagia.html;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class HTML{

    public static void getHtml(String url, Context con, final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(con);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                callback.onSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onFailure(volleyError);
            }
        });
        queue.add(stringRequest);
    }

    public static void getHtmlCookie(String url, Context con, final String cookie, final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(con);
        CookieRequest cookieRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                callback.onSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onFailure(volleyError);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", cookie);
                return headers;
        }};
        queue.add(cookieRequest);
    }
}
