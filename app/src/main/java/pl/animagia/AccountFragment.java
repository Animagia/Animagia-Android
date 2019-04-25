package pl.animagia;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;


//TODO extract superclass for fragments that require login
public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = isLogged() ?
                R.layout.fragment_account : R.layout.fragment_account_empty; //TODO

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isLogged()) {
            getAccountInfo();
        } else {
            Button loginButton = getView().findViewById(R.id.linkExistingAccountButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activateLoginFragment();
                }
            });

            Button shopButton = getView().findViewById(R.id.goToShopFromAccountButton);
            shopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).activateFragment(new ShopFragment());
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

    private void getAccountInfo(){ //FIXME
        String url = "https://animagia.pl/konto";
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
                        getAccountInfo();
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
        TextView textView = getView().findViewById(R.id.files);
        textView.setText(extractAccountStatus(s));

        TextView textViewEmail = getView().findViewById(R.id.email_text);
        textViewEmail.setText(extractUserEmail(s));
    }

    private static String extractAccountStatus(String accountPageHtml) {
        if(accountPageHtml.contains("<strong>Wygasające</strong>")) {
            return "Wyagasające";
        } else if(accountPageHtml.contains("<strong>Aktywne.</strong>")) {
            return "Aktywne";
        } else if(accountPageHtml.contains("<p>Nieaktywne.</p>")) {
            return "Nieaktywne";
        }
        return "";
    }

    private static String extractUserEmail(String accountPageHtml) {
        String s1 = "Zalogowano jako:";
        String s2 = "<a href=\"https://animagia.pl/wp-login.php?action=logout";

        int start = s1.length() + accountPageHtml.indexOf(s1);
        int end = accountPageHtml.indexOf(s2);

        try {
            String emailWithDot = accountPageHtml.substring(start, end).trim();
            return emailWithDot.substring(0,emailWithDot.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

}
