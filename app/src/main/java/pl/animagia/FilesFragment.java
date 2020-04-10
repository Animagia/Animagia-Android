package pl.animagia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import pl.animagia.dialog.Dialogs;
import pl.animagia.html.CookieRequest;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.AccountStatus;
import pl.animagia.user.CookieStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilesFragment extends TopLevelFragment {

    private List<String> downloadAnchors;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = isLoggedIn() ? R.layout.file_list : R.layout.fragment_files_empty;

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isLoggedIn()) {
           getFilesFromAccount();
        }

        Set<Anime> locallyPurchasedAnime = TokenStorage.getLocallyPurchasedAnime(getActivity());
        for (Anime a : locallyPurchasedAnime) {
            String token = TokenStorage.getCombinedToken(getActivity(), a);
            String videoPageUrl = TokenAssembly.URL_BASE + token;
            
        }

        if(!isLoggedIn() && locallyPurchasedAnime.isEmpty()) {
            showEmptyCollection();
        }
    }


    private void showEmptyCollection() {
        Button loginButton = getView().findViewById(R.id.getFilesFromAccountButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).activateFragment(new LoginFragment());
            }
        });
        Button shopButton = getView().findViewById(R.id.goToShopButton);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).activateFragment(new ShopFragment());
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_shop);
    }


    private boolean isLoggedIn(){
        boolean logIn = false;

        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
        System.out.println(cookie);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
    }


    private void getFilesFromAccount(){
        String url = "https://animagia.pl/konto";
        if(getActivity() != null){
            RequestQueue queue = Volley.newRequestQueue(getContext());
            CookieRequest stringRequest = new CookieRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    if(getActivity() != null)

                    if(s.contains("<form name=\"loginform\" id=\"loginform\" action=\"https://animagia.pl/wp-login.php\" method=\"post\">")){

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("Zaloguj się ponownie.");
                        builder.setTitle("Sesja wygasła");
                        builder.setPositiveButton("Zaloguj",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        CookieStorage.clearLoginCredentials(getActivity());
                                        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Oglądaj");
                                        ((MainActivity) getActivity()).activateFragment(new LoginFragment());
                                    }
                                });

                        builder.show();

                    }else{
                        onAccountPageFetched(s);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getFilesFromAccount();
                        }
                    };
                    if(getActivity() != null)
                    Dialogs.internetConnectionError(getContext(), onClickTryAgain);
                }
            });
            if(getActivity() != null){
                String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
                stringRequest.setCookies(cookie);
                queue.add(stringRequest);
            }

        }

    }


    private synchronized void onAccountPageFetched(String accountPageHtml) {
        AccountStatus accountStatus = AccountFragment.extractAccountStatus(accountPageHtml);
        CookieStorage.saveAccountStatus(getActivity(), accountStatus);

        List<String> downloadAnchorsFromAccount = getDownloadAnchors(accountPageHtml);
        List<String> fileNames = getDownloadableFileNames(downloadAnchorsFromAccount);

        CookieStorage.saveNamesOfFilesPurchasedByAccount(getActivity(), fileNames);

        this.downloadAnchors.addAll(downloadAnchorsFromAccount);

        updateDisplayedLinks();
    }


    private synchronized void onSingleProductFetched(String pageHtml) {
        this.downloadAnchors.addAll(getDownloadAnchors(pageHtml));
        updateDisplayedLinks();
    }


    private void updateDisplayedLinks() {
        List<String> fileNames = getDownloadableFileNames(downloadAnchors);

        ListView lv = null;
        if(getActivity() != null){
            lv = getView().findViewById(R.id.file_listview);
            lv.setAdapter(new DownloadableFileAdapter(getActivity(), fileNames));
        }

        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> listView,
                                            View itemView,
                                            int position,
                                            long id) {
                        if(downloadAnchors.size() > 0){
                            Intent browserIntent =
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(extractUrl(
                                            downloadAnchors.get(position))));
                            startActivity(browserIntent);
                        }

                    }
                };
        if(getActivity() != null && lv != null){
            lv.setOnItemClickListener(itemClickListener);
        }
    }


    static List<String> getDownloadAnchors(String accountPageHtml) {
        List<String> downloadUrls = new ArrayList<>();
        Matcher m = Pattern.compile("href=\"https:\\/\\/(static|animagia-dl).*?video\\/ddl.*?\">.*?</a")
                .matcher(accountPageHtml);
        while (m.find()) {
            downloadUrls.add(m.group());
        }
        return downloadUrls;
    }


    static String extractUrl(String anchorHtml) {
        int start = "href=\"".length();
        int end = anchorHtml.indexOf("\">");
        return anchorHtml.substring(start, end);
    }


    static String extractFileName(String anchorHtml) {
        int start = anchorHtml.indexOf("\">") + "\">".length();
        int end = anchorHtml.length() - "</a".length();
        return anchorHtml.substring(start, end);
    }


    static List<String> getDownloadableFileNames(List<String> downloadAnchorsAsHtml) {
        List<String> fileNames = new ArrayList<>();

        for (String anchorHtml : downloadAnchorsAsHtml) {
            fileNames.add(extractFileName(anchorHtml));
        }

        return fileNames;
    }

}
