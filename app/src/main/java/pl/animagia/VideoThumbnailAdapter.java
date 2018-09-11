package pl.animagia;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class VideoThumbnailAdapter extends ArrayAdapter<VideoData> {

    public VideoThumbnailAdapter(Context context) {
        super(context, R.layout.video_thumbnail, R.id.thumbnail_text, prepareVideos());
    }

    private static VideoData[] prepareVideos() {
        VideoData arr[] =
                {new VideoData("A feature film", Uri.parse("file:///android_asset/oscar_nord.jpg")),
                 new VideoData("A TV series", Uri.parse("file:///android_asset/serrah_galos.jpg")),
                 new VideoData("Amagi", Uri.parse("file:///android_asset/oscar_nord.jpg"))};
        return arr;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);

        Glide.with(getContext()).load(super.getItem(position).getPosterAsssetUri()).into(poster);

        return thumbnail;
    }


}
