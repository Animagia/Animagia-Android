package pl.animagia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;

import java.util.List;


//TODO extract and share methods that read login cookies
public class AccountFragment extends TopLevelFragment {

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
            if(getActivity() != null){
                getAccountInfo();
            }
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

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_account);
    }

    private void activateLoginFragment() {
        ((MainActivity) getActivity()).activateFragment(new LoginFragment());
    }

    private boolean isLogged(){
        boolean logIn = false;

        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
        System.out.println(cookie);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
    }

    private void getAccountInfo(){
        String url = "https://animagia.pl/konto";
        if(getActivity() != null){
            RequestQueue queue = Volley.newRequestQueue(getContext());
            CookieRequest stringRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    if(getActivity() != null){


                        if(s.contains("<form name=\"loginform\" id=\"loginform\" action=\"https://animagia.pl/wp-login.php\" method=\"post\">")){

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage("Zaloguj się ponownie.");
                            builder.setTitle("Sesja wygasła");
                            builder.setPositiveButton("Zaloguj",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                             CookieStorage
                                                     .clearLoginCredentials(getActivity());
                                            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Oglądaj");
                                            ((MainActivity) getActivity()).activateFragment(new LoginFragment());
                                        }
                                    });

                             builder.show();

                        }else{
                            onAccountPageFetched(s);
                        }
                    }

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
                    if(getActivity() != null){
                        Alerts.internetConnectionError(getContext(), onClickTryAgain);
                    }

                }
            });
            if(getActivity() != null){
                String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
                stringRequest.setCookies(cookie);
                queue.add(stringRequest);
            }
        }

    }

    private void onAccountPageFetched(String accountPageHtml) {
        AccountStatus accountStatus = extractAccountStatus(accountPageHtml);
        CookieStorage.saveAccountStatus(getActivity(), accountStatus);

        List<String> downloadAnchors = FilesFragment.getDownloadAnchors(accountPageHtml);
        List<String> fileNames = FilesFragment.getDownloadableFileNames(downloadAnchors);
        CookieStorage.saveNamesOfPurchasedFiles(getActivity(), fileNames);

        TextView textView = getView().findViewById(R.id.files);
        textView.setText(accountStatus.getFriendlyName());

        TextView textViewEmail = getView().findViewById(R.id.email_text);
        textViewEmail.setText(extractUserEmail(accountPageHtml));
    }


    static AccountStatus extractAccountStatus(String accountPageHtml) {
        if (accountPageHtml.contains("<strong>Wygasające</strong>")) {
            return AccountStatus.EXPIRING;
        } else if (accountPageHtml.contains("<strong>Aktywne.</strong>")) {
            return AccountStatus.ACTIVE;
        } else if (accountPageHtml.contains("<p>Nieaktywne.</p>")) {
            return AccountStatus.INACTIVE;
        }
        return AccountStatus.UNKNOWN;
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
