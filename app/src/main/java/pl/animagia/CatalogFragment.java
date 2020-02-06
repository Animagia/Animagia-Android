package pl.animagia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.user.Cookies;
import pl.animagia.video.VideoUrl;

public class CatalogFragment extends TopLevelFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);

        final VideoThumbnailAdapter adapter = new VideoThumbnailAdapter(getActivity());
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                launchPlayback(adapter.getItem(position));
            }
        });

        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        final View headView = navigationView.getHeaderView(0);

        TextView textView = headView.findViewById(R.id.userEmail);
        ImageView imageView = headView.findViewById(R.id.login);
        Button button = headView.findViewById(R.id.account_view_icon_button);


        if (isLogged()) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setText(getUsername());
        }
        else {
            textView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
            textView.setText(R.string.guest);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_watch);
    }


    private void launchPlayback(final VideoData videoData) {
        final String cookie = Cookies.getCookie(Cookies.LOGIN, getActivity());
        if (videoData.getVideoUrl().endsWith(".mkv") || videoData.getVideoUrl().endsWith(".webm")) {
            Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
            intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
            intent.putExtra(VideoData.NAME_OF_URL, videoData.getVideoUrl());
            intent.putExtra(Cookies.LOGIN, cookie);

            startActivity(intent);
        } else {

            HTML.getHtmlCookie(videoData.getVideoUrl(), getContext(), cookie, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    String url = VideoUrl.getUrl(result);
                    Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
                    intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
                    intent.putExtra(VideoData.NAME_OF_URL, url);
                    intent.putExtra(Cookies.LOGIN, cookie);

                    startActivity(intent);

                }

                @Override
                public void onFailure(VolleyError volleyError) {
                    DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            launchPlayback(videoData);
                        }
                    };

                    if (volleyError instanceof NoConnectionError) {
                        Alerts.internetConnectionError(getContext(), onClickTryAgain);
                    }
                }
            });
        }
    }

    public void setText(String message) {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.catalog_layout);

        TextView view = (TextView) getActivity().findViewById(R.id.geo_text_view);
        if (view == null) {

            TextView textView = new TextView(getContext());
            textView.setId(R.id.geo_text_view);
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(message);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            textView.setTextSize(18);

            linearLayout.addView(textView, 0);
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

    private String getUsername() {
        String cookie = Cookies.getCookie(Cookies.LOGIN, getActivity());
        if (!cookie.equals(Cookies.COOKIE_NOT_FOUND)){
            int first_index = cookie.indexOf('=');
            int last_index = cookie.indexOf('%');
            return cookie.substring(first_index + 1, last_index);
        }
        return "";
    }

}