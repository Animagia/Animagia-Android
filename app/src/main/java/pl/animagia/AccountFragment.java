package pl.animagia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import pl.animagia.dialog.Dialogs;
import pl.animagia.html.CookieRequest;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO extract and share methods that read login cookies
public class AccountFragment extends TopLevelFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = R.layout.fragment_account_empty;

        if(userIsLoggedIn()) {
            layoutResource = R.layout.fragment_account;
        } else if(TokenStorage.getLocallyPurchasedAnime(getActivity()).size() != 0) {
            layoutResource = R.layout.fragment_account_with_creation;
        }

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(userIsLoggedIn()) {
            if(getActivity() != null){
                getAccountInfo();
            }
        } else if(TokenStorage.getLocallyPurchasedAnime(getActivity()).size() != 0) {

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
                    TokenStorage.consumeAllProducts((MainActivity) getActivity());
                }
            };

            getView().findViewById(R.id.bindToNewAccountButton).setOnClickListener(
                    new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAccountCreationDialog();
                }
            });
            getView().findViewById(R.id.bindToExistingAccountButton).setOnClickListener(listener);

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


    private void showAccountCreationDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.account_creation_form);

        final Button btn = dialog.findViewById(R.id.creation_button);

        CheckBox box = dialog.findViewById(R.id.acceptance);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                btn.setEnabled(checked);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = ((TextView) dialog.findViewById(R.id.email)).getText().toString();
                createAccount(email, dialog);
            }
        });

        dialog.show();
    }


    private void createAccount(final String email, final Dialog dialog) {


        RequestQueue queue = Volley.newRequestQueue(getContext());

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) { }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) { }
        };


        String token = "fake";
        String accountCreationUrl = TokenAssembly.URL_BASE + token + "&forNewAccount=" + email;


        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                accountCreationUrl,
                listener, errorListener) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i("response",response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                int firstIndex = rawCookies.indexOf(";");
                String cookie = rawCookies.substring(0, firstIndex);
                if(cookie.startsWith("wordpress_logged_in")) {
                    CookieStorage.setCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, cookie, getActivity());
                    dialog.dismiss();
                    onAccountCreated();
                }
                else {
                    Toast.makeText(getActivity(), "Coś poszło nie tak. Spróbuj ponownie", Toast
                            .LENGTH_SHORT).show();
                }
                Log.i("cookies",rawCookies);
                return super.parseNetworkResponse(response);
            }

        };
        queue.add(stringRequest);

    }


    private void onAccountCreated() {
        getAccountInfo();
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_account);
    }

    private void activateLoginFragment() {
        ((MainActivity) getActivity()).activateFragment(new LoginFragment());
    }

    private boolean userIsLoggedIn(){
        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
        return !cookie.equals(CookieStorage.COOKIE_NOT_FOUND);
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

                        } else{
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
                        Dialogs.internetConnectionError(getContext(), onClickTryAgain);
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
        CookieStorage.saveNamesOfFilesPurchasedByAccount(getActivity(), fileNames);

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
