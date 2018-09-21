package pl.animagia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.video.VideoUrl;

public class CatalogFragment extends Fragment {

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
        HTML.getHtml(videoData.getVideoUrl(), getContext(), new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                String url =  VideoUrl.getUrl(result);

                Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
                intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
                intent.putExtra(VideoData.NAME_OF_URL, url);

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
