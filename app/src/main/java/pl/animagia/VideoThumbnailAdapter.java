package pl.animagia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.widget.TextView;
import com.bumptech.glide.Glide;


import java.util.*;


class VideoThumbnailAdapter extends ArrayAdapter<Anime> {

    VideoThumbnailAdapter(MainActivity ma) {
        super(ma, R.layout.video_thumbnail, R.id.thumbnail_title,
                new ArrayList<>(ma.getAnimeInCatalog()) );
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);

        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);
        Glide.with(getContext())
                .load(super.getItem(position).getThumbnailAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/clapperboard.jpg"))
                .into(poster);

        String filmSubtitle = super.getItem(position).getSubtitle();
        TextView subtitleView = thumbnail.findViewById(R.id.thumbnail_subtitle);
        if (filmSubtitle.equals("")) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setText(filmSubtitle);
        }

        TextView durationView = thumbnail.findViewById(R.id.thumbnail_duration);
        durationView.setText(super.getItem(position).getDuration());

        TextView descriptionView = thumbnail.findViewById(R.id.thumbnail_description);
        descriptionView.setText(super.getItem(position).getDescription());

        return thumbnail;
    }

}
