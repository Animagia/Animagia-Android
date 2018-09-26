package pl.animagia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import pl.animagia.error.Alerts;
import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;


public class FilesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       //Cookies.removeCookie(Cookies.LOGIN, getActivity());
        if(isLogged()) {
           getFiles();
        } else {
            DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activateFragment(new LoginFragment());
                }
            };
            Alerts.logInError(getContext(), onClickTryAgain);
        }
    }

    private boolean isLogged(){
        boolean logIn = false;

        String cookie = Cookies.getCookie(Cookies.LOGIN, getActivity());
        System.out.println(cookie);
        if (!cookie.equals(Cookies.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
    }

    private void getFiles(){
        String url = "animagia.pl/";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        CookieRequest stringRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                TextView textView = getView().findViewById(R.id.files);
                String text = s;
                textView.setText(text);
                textView.setMovementMethod(new ScrollingMovementMethod());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError);
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFiles();
                    }
                };
                Alerts.internetConnectionError(getContext(), onClickTryAgain);
            }
        });
        String cookie = Cookies.getCookie(Cookies.LOGIN, getActivity());
        stringRequest.setCookies(cookie);
        queue.add(stringRequest);
    }

    private void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }
}
