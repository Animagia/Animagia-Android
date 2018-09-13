package pl.animagia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.video.VideoUrl;

public class CatalogFragment extends Fragment {

    public static final String NAME_OF_URL = "video url";

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
    }


    private void launchPlayback(final VideoData videoData) {
        HTML.getHtml(getContext(), new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                String url =  VideoUrl.getUrl(result);

                Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
                intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
                intent.putExtra(CatalogFragment.NAME_OF_URL, url);

                startActivity(intent);
            }
        });
    }

}
