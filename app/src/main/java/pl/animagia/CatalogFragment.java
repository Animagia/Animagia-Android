package pl.animagia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.location.Geolocation;
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
                if(Geolocation.checkLocation(url)) {
                    Intent intent = new Intent(getActivity(), FullscreenPlaybackActivity.class);
                    intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
                    intent.putExtra(VideoData.NAME_OF_URL, url);

                    startActivity(intent);
                } else {
                    setText();
                }
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

    private void setText() {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.catalog_layout);

        TextView view = (TextView) getActivity().findViewById(R.id.geo_text_view);
        if(view == null){

            TextView textView = new TextView(getContext());
            textView.setId(R.id.geo_text_view);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText("Nie jesteś w Polsce");
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            textView.setTextSize(18);

            linearLayout.addView(textView, 0);
        }
    }

}