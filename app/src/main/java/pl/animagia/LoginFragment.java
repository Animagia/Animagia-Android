package pl.animagia;


import android.Manifest;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Button signIn = (Button) getActivity().findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailText = getActivity().findViewById(R.id.email);
                EditText passwordText = getActivity().findViewById(R.id.password);

                final String email = emailText.getText().toString();
                final String password = passwordText.getText().toString();

                Toast.makeText(getContext(), email + " " + password, Toast.LENGTH_SHORT).show();

                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

                View headView = navigationView.getHeaderView(0);
                TextView emailTextView = headView.findViewById(R.id.userEmail);

                RequestQueue queue = Volley.newRequestQueue(getContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, "animagia.pl", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        System.out.println(s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("log", email);
                        params.put("pwd", password);
                        params.put("testcookie", "1");

                        return params;
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        Log.i("response",response.headers.toString());
                        Map<String, String> responseHeaders = response.headers;
                        String rawCookies = responseHeaders.get("Set-Cookie");
                        int firstIndex = rawCookies.indexOf(";");
                        String cookie = rawCookies.substring(0, firstIndex);
                        Cookies.setCookie("Cookie", cookie, getActivity());

                        sendRequest("animagia.pl/account", getContext());

                        Log.i("cookies",rawCookies);
                        return super.parseNetworkResponse(response);
                    }

                };
                queue.add(stringRequest);

                emailTextView.setText(email);
               // activateFragment(new CatalogFragment());
                hideSoftKeyboard();
            }
        });

    }

    private void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }

    private void hideSoftKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void sendRequest(String url, Context con) {
        RequestQueue queue = Volley.newRequestQueue(con);
        CookieRequest stringRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                System.out.println(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
//                callback.onFailure(volleyError);
            }
        });
        String cookie = Cookies.getCookie("Cookie", getActivity());
        stringRequest.setCookies(cookie);
        queue.add(stringRequest);
    }

}