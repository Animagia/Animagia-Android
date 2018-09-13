
package pl.animagia.html;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HTML{

    public static void getHtml(Context con, final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(con);
        String url = "https://animagia.pl/amagi-brilliant-park-odc-1/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                callback.onSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("NIE MA HTML");
            }
        });
        queue.add(stringRequest);
    }
}