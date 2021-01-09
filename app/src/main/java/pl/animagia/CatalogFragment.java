package pl.animagia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import pl.animagia.user.CookieStorage;

import java.util.Set;

public class CatalogFragment extends TopLevelFragment {


    private GridView gridView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridView = view.findViewById(R.id.gridview);

        final VideoThumbnailAdapter adapter =
                new VideoThumbnailAdapter((MainActivity) getActivity());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                launchPlayback(adapter.getItem(position));
            }
        });

        MainActivity ma = (MainActivity) getActivity();
        ma.updateUsernameInHeader();
    }


    @Override
    public void onResume() {
        super.onResume();
        gridView.invalidateViews(); //FIXME redundant rebuild when fragment first created?
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_watch);
    }


    private void launchPlayback(final Anime videoData) {
        final String cookie = CookieStorage.getCookie(getActivity());

        Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
        intent.putExtra(Anime.NAME_OF_INTENT_EXTRA, videoData);
        intent.putExtra(CookieStorage.LOGIN_CREDENTIALS_KEY, cookie);

        startActivity(intent);
    }


    public void populate(Set<Anime> animeInCatalog) {

        throw new UnsupportedOperationException();

    }


}