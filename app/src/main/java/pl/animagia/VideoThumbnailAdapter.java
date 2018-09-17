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
                {new VideoData("A feature film", Uri.parse("file:///android_asset/oscar_nord.jpg"), "http://dl3.webmfiles.org/big-buck-bunny_trailer.webm"),
                 new VideoData("A TV series", Uri.parse("file:///android_asset/serrah_galos.jpg"), "http://dl3.webmfiles.org/big-buck-bunny_trailer.webm"),
                 new VideoData("Amagi", Uri.parse("file:///android_asset/oscar_nord.jpg"), "https://animagia.pl/amagi-brilliant-park-odc-1/"),
                 new VideoData("Chuuni", Uri.parse("https://static.animagia.pl/film_poster.jpg"), "https://animagia.pl")
                };
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
