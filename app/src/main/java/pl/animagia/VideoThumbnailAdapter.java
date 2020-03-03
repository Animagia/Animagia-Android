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

    VideoThumbnailAdapter(Context context) {
        super(context, R.layout.video_thumbnail, R.id.thumbnail_title, prepareVideos());
    }

    static Anime[] prepareVideos() {
        int totalTitles = 6;

        if (appIsOutdated()) {
            totalTitles = 0;
        }

        Anime[] fullArr = new ArrayList<>(EnumSet.allOf(Anime.class)).toArray(new Anime[0]);

        return Arrays.copyOfRange(fullArr, 0, totalTitles);
    }

    private static boolean appIsOutdated() {
        GregorianCalendar appBecomesOutdated = new GregorianCalendar();
        appBecomesOutdated.setTimeZone(TimeZone.getTimeZone("UTC"));
        appBecomesOutdated.set(2020, GregorianCalendar.JUNE, 1);

        GregorianCalendar now = new GregorianCalendar();
        now.setTimeZone(TimeZone.getTimeZone("UTC"));

        return now.after(appBecomesOutdated);
    }


    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);

        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);
        Glide.with(getContext())
                .load(super.getItem(position).getThumbnailAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/oscar_nord.jpg"))
                .into(poster);

        String filmSubtitle = super.getItem(position).getSubtitle();
        TextView subtitleView = thumbnail.findViewById(R.id.thumbnail_subtitle);
        if (filmSubtitle == "") {
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
