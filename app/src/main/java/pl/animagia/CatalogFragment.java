package pl.animagia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
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

import pl.animagia.user.CookieStorage;

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


    private void launchPlayback(final Anime videoData) {
        final String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());

        Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
        intent.putExtra(Anime.NAME_OF_INTENT_EXTRA, videoData.name());
        intent.putExtra(CookieStorage.LOGIN_CREDENTIALS_KEY, cookie);

        startActivity(intent);
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
        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
        System.out.println(cookie);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
    }

    private String getUsername() {
        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, getActivity());
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            int first_index = cookie.indexOf('=');
            int last_index = cookie.indexOf('%');
            return cookie.substring(first_index + 1, last_index);
        }
        return "";
    }

}