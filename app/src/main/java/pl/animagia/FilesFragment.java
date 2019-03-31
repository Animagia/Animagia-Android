package pl.animagia;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import pl.animagia.error.Alerts;
import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FilesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = isLogged() ? R.layout.file_list : R.layout.fragment_files_empty;

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       //Cookies.removeCookie(Cookies.LOGIN, getActivity());
        if(isLogged()) {
           getFiles();
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

    private void onAccountPageFetched(String accountPageHtml) {
        List<String> downloadUrls = new ArrayList<String>();
        Matcher m = Pattern.compile("href=\"https:\\/\\/(static|animagia-dl).*?video\\/ddl.*?\">.*?</a")
                .matcher(accountPageHtml);
        while (m.find()) {
            downloadUrls.add(m.group());
        }


        ListView lv = getView().findViewById(R.id.file_listview);
        lv.setAdapter(new DownloadableFileAdapter(getActivity(), generateDummyLinks()));

//        Spanned linkForTextView = Html.fromHtml("<a href=\"" + extractUrl(next) +"\">"
//                + extractFileName(next) + "</a>");
//
//        textView.setText(linkForTextView);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private static String extractUrl(String html) {
        int start = "href=\"".length();
        int end = html.indexOf("\">");
        return html.substring(start, end);
    }

    private static String extractFileName(String html) {
        int start = html.indexOf("\">") + "\">".length();
        int end = html.length() - "</a".length();
        return html.substring(start, end);
    }


    private static List<String> generateDummyLinks() {
        List<String> links = new ArrayList<>();
        for(int i=0;i<50;i++){
            links.add("<a href=\"https://animagia.pl\">Animagia</a>");
        }
        return links;
    }

}
