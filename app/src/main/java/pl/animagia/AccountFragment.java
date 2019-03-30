package pl.animagia;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import pl.animagia.error.Alerts;
import pl.animagia.file.FileUrl;
import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;


//TODO extract superclass for fragments that require login
public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = isLogged() ?
                R.layout.fragment_files : R.layout.fragment_account_empty; //TODO

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isLogged()) {
            getFiles();
        } else {
            Button loginButton = getView().findViewById(R.id.linkExistingAccountButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activateLoginFragment();
                }
            });
        }
    }

    private void activateLoginFragment() {
        ((MainActivity) getActivity()).activateFragment(new LoginFragment());
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

    private void getFiles(){ //FIXME
        String url = "https://animagia.pl/";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        CookieRequest stringRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                onAccountPageFetched(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
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

    private void onAccountPageFetched(String s) {

        //FIXME
        TextView textView = getView().findViewById(R.id.files);
        String text = FileUrl.getText(s);
        textView.setText(s.substring(0,10));
//        textView.setClickable(true);
//        textView.setMovementMethod(new LinkMovementMethod());


    }
}
