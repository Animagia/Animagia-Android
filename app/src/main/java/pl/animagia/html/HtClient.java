
package pl.animagia.html;

import android.content.Context;

import android.text.Html;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.animagia.Anime;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HtClient {


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


    public static void getUsingCookie(String url, Context con, final String cookie, final VolleyCallback callback){
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
    
    
    public static void fetchCatalogJson(Context ctx, final VolleyCallback callback) {

        final String catalogUrl = "https://static.animagia.pl/catalog.json";

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                callback.onSuccess(jsonArray.toString());
            }
        };

        JsonRequest<JSONArray> req = new JsonRequest<JSONArray>(Request.Method.GET, catalogUrl,
                null, listener, null) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    String utf8String = new String(networkResponse.data, "UTF-8");
                    return Response.success(new JSONArray(utf8String), HttpHeaderParser.parseCacheHeaders
                            (networkResponse));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.success(new JSONArray(), HttpHeaderParser.parseCacheHeaders
                            (networkResponse));
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(req);
	}
	
	
	public static Set<Anime> parseCatalog(String catalogJson) {

        Set<Anime> catalog = new HashSet<>();

        try {
            JSONArray animeArr = new JSONArray(catalogJson);

            for(int i=0;i<animeArr.length();i++) {
                JSONObject animeObj = animeArr.getJSONObject(i);

                catalog.add(new Anime(animeObj.getString("title"), animeObj.getString
                        ("thumbnailAsssetUri"), animeObj.getString("videoUrl"), animeObj.getInt
                        ("episodes"),animeObj.getString("posterAssetUri"),animeObj.getString
                        ("timeStamps"), animeObj.getString("price"), "", animeObj.getString
                        ("subtitle"), animeObj.getString("duration"), animeObj.getString
                        ("description"), animeObj.getInt("previewMillis"), "", animeObj.getString
                        ("sku")));
            }

        } catch (JSONException ignore) {
            throw new RuntimeException(ignore.getMessage());
        }

        return catalog;
	}


    public static String readAsUnicode(String response) {
        try {
            return URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    
}
