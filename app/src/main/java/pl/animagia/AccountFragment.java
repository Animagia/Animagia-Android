package pl.animagia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import pl.animagia.dialog.Dialogs;
import pl.animagia.html.CookieRequest;
import pl.animagia.html.HtClient;
import pl.animagia.html.VolleyCallback;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;


//TODO extract and share methods that read login cookies
public class AccountFragment extends TopLevelFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = R.layout.fragment_account_empty;

        if(userIsLoggedIn()) {
            layoutResource = R.layout.fragment_account;
        } else if(TokenStorage.hasLocallyPurchasedAnime(getActivity())) {
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
        } else if(TokenStorage.hasLocallyPurchasedAnime(getActivity())) {

            getView().findViewById(R.id.bindToNewAccountButton).setOnClickListener(
                    new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAccountCreationDialog();
                }
            });

            View.OnClickListener existingAccountListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TokenStorage.consumeAllProducts((MainActivity) getActivity());
                    activateLoginFragment();
                }
            };
            getView().findViewById(R.id.bindToExistingAccountButton)
                    .setOnClickListener(existingAccountListener);

            TextView localPurchases = getView().findViewById(R.id.productsPurchasedLocallyHint);
            localPurchases.setText(getListOfTitles());

        } else {
            Button loginButton = getView().findViewById(R.id.linkExistingAccountButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TokenStorage.consumeAllProducts((MainActivity) getActivity());
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

        TextView acceptanceLabel = dialog.findViewById(R.id.acceptance_label);
        acceptanceLabel.setMovementMethod(LinkMovementMethod.getInstance());

        final Button btn = dialog.findViewById(R.id.creation_button);

        final CheckBox box = dialog.findViewById(R.id.acceptance);
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
                btn.setEnabled(false);
                box.setEnabled(false);
                dialog.setCancelable(false);
                createAccount(email, dialog);
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void createAccount(final String email, final Dialog accountCreationDialog) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) { }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                promptUserToTryAgain(accountCreationDialog);
            }
        };

        String token = TokenStorage.getBulkImportToken(getActivity());
        String accountCreationUrl = TokenAssembly.URL_BASE + token + "&forNewUser=" + email;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                accountCreationUrl,
                listener, errorListener) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");

                if(rawCookies == null) {
                    promptUserToTryAgain(accountCreationDialog);
                    return super.parseNetworkResponse(response);
                }

                int firstIndex = rawCookies.indexOf(";");
                String cookie = rawCookies.substring(0, firstIndex);

                String responseBody;
                try {
                    responseBody = new String(response.data, HttpHeaderParser.parseCharset(response
                            .headers));
                } catch (UnsupportedEncodingException e) {
                    responseBody = new String(response.data);
                }

                if(cookie.startsWith("wordpress_logged_in") && websiteReportedSuccess(responseBody))
                {
                    SharedPreferences pref = getActivity().getSharedPreferences(
                            MainActivity.class.getName(), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(CookieStorage.LOGIN_CREDENTIALS_KEY, cookie);
                    editor.commit();
                    accountCreationDialog.dismiss();
                    TokenStorage.consumeAllProducts((MainActivity) getActivity());
                    onAccountCreated(email);
                }
                else {
                    promptUserToTryAgain(accountCreationDialog);
                }

                return super.parseNetworkResponse(response);
            }

        };
        queue.add(stringRequest);

    }


    private void promptUserToTryAgain(Dialog accountCreationDialog) {
        accountCreationDialog.dismiss();
        Toasts.promptUserToTryAgain(getActivity());
    }


    private boolean websiteReportedSuccess(String responseBody) {
        return !responseBody.contains("użyciu")
                && !responseBody.contains("Wymagane") &&
                !responseBody.contains("poszło nie tak");
    }


    private void onAccountCreated(String email) {
        Toast.makeText(getActivity(), "Zalogowano jako: " + email, Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).activateFragment(new FilesFragment());
    }


    private void bindToCurrentAccount() {
        getView().findViewById(R.id.bindToCurrentAccountButton).setEnabled(false);

        String token = TokenStorage.getBulkImportToken(getActivity());
        String email = ((TextView) getView().findViewById(R.id.email_text)).getText().toString()
                .trim();
        String url = TokenAssembly.URL_BASE + token + "&forExistingUser=" + email;

        String cookie = CookieStorage.getCookie(getActivity());

        HtClient.getUsingCookie(url, getActivity(), cookie, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if(websiteReportedSuccess(result)) {
                    TokenStorage.consumeAllProducts((MainActivity) getActivity());
                    ((MainActivity) getActivity()).activateFragment(new FilesFragment());
                } else {
                    promptUserToRetryImportToExisting();
                }
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                promptUserToRetryImportToExisting();
            }
        });
    }


    private void promptUserToRetryImportToExisting() {
        getView().findViewById(R.id.bindToCurrentAccountButton).setEnabled(true);
        Toasts.promptUserToTryAgain(getActivity());
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
        String cookie = CookieStorage.getCookie(getActivity());
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
                String cookie = CookieStorage.getCookie(getActivity());
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

        getView().findViewById(R.id.account_status_label).setVisibility(View.VISIBLE);

        TextView textView = getView().findViewById(R.id.account_status);
        textView.setText(accountStatus.getFriendlyName());

        TextView textViewEmail = getView().findViewById(R.id.email_text);
        String email = extractUserEmail(accountPageHtml);
        textViewEmail.setText(email);

        if(TokenStorage.hasLocallyPurchasedAnime(getActivity())) {
            textView = getView().findViewById(R.id.productsPurchasedLocallyHint);
            textView.setText(getListOfTitles());
            textView.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.productsCanBeBoundHint).setVisibility(View.VISIBLE);

            Button btn = getView().findViewById(R.id.bindToCurrentAccountButton);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bindToCurrentAccount();
                }
            });
            btn.setVisibility(View.VISIBLE);
        }
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


    private String getListOfTitles() {

        Set<Anime> anime = ((MainActivity) getActivity()).getAnimeInCatalog();

        String titles = "";

        for (String sku : TokenStorage.getSkusOfLocallyPurchasedAnime(getActivity())) {
            for (Anime a : anime) {
                if(a.getSku().equals(sku)) {
                    titles += "\n" + a.formatFullTitle();
                }
            }
        }
        return getResources().getString(R.string.have_locally_purchased_anime, titles);

    }

}
