package pl.animagia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import java.util.*;

import static pl.animagia.PlaybackUtils.firstChapterAfterLogo;


class VideoThumbnailAdapter extends ArrayAdapter<Anime> {

    VideoThumbnailAdapter(MainActivity ma) {
        super(ma, R.layout.video_thumbnail, R.id.thumbnail_title,
                new ArrayList<>(ma.getAnimeInCatalog()) );
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        Anime anime = super.getItem(position);

        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);
        Glide.with(getContext())
                .load(anime.getThumbnailAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/clapperboard.jpg"))
                .into(poster);

        String filmSubtitle = anime.getSubtitle();
        TextView subtitleView = thumbnail.findViewById(R.id.thumbnail_subtitle);
        if (filmSubtitle.equals("")) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setText(filmSubtitle);
        }

        TextView durationView = thumbnail.findViewById(R.id.thumbnail_duration);
        durationView.setText(anime.getDuration());

        TextView descriptionView = thumbnail.findViewById(R.id.thumbnail_description);
        descriptionView.setText(anime.getDescription());


        prepareProgressBar(thumbnail, anime);


        return thumbnail;
    }


    private void prepareProgressBar(View thumbnail, Anime anime) {
        View watchedBar = thumbnail.findViewById(R.id.thumbnail_progress_watched);
        View unwatchedBar = thumbnail.findViewById(R.id.thumbnail_progress_unwatched);
        float watchedWeight;
        float unwatchedWeight;
        int vis;

        if(anime.getEpisodeCount() == 1) {
            long length = anime.getLengthMillis();
            long progress = PreferenceUtils.getSavedProgress(getContext(), anime, 1);
            progress = Math.min(progress, length);
            vis = progress > firstChapterAfterLogo(anime) +
                    PreferenceUtils.MINIMUM_PROGRESS_TO_SAVE ? View.VISIBLE : View.GONE;
            watchedWeight = progress / 1000;
            unwatchedWeight = (length - progress) / 1000;

        } else {
            int episodeReached = PreferenceUtils.getReachedEpisode(getContext(), anime);
            vis = episodeReached > 1 ? View.VISIBLE : View.GONE;
            watchedWeight = episodeReached;
            unwatchedWeight = anime.getEpisodeCount() + 1 - episodeReached;
        }

        watchedBar.setVisibility(vis);
        unwatchedBar.setVisibility(vis);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) watchedBar.getLayoutParams();
        lp.weight = watchedWeight;
        watchedBar.setLayoutParams(lp);
        lp = (LinearLayout.LayoutParams) unwatchedBar.getLayoutParams();
        lp.weight = unwatchedWeight;
        unwatchedBar.setLayoutParams(lp);
    }


}
