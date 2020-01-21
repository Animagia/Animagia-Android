package pl.animagia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.widget.TextView;
import com.bumptech.glide.Glide;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class VideoThumbnailAdapter extends ArrayAdapter<VideoData> {

    private static VideoData arr[]; //FIXME should this really be static?

    public VideoThumbnailAdapter(Context context) {
        super(context, R.layout.video_thumbnail, R.id.thumbnail_title, prepareVideos());
    }

    static VideoData[] prepareVideos() {

        int totalTitles = 6;

        if (appIsOutdated()) {
            totalTitles = 0;
        }

        VideoData[] fullArr = new VideoData[]{
                new VideoData("Take On Me",
                        "https://static.animagia.pl/Chuu_poster.jpg",
                        "https://animagia.pl/chuunibyou-demo-koi-ga-shitai-take-on-me/", 1,
                        "https://animagia.pl/wp-content/uploads/2018/07/umbrella_for_store_page.png",
                        "00:00:29.500;00:33:03.115;01:05:03.115",
                        "25,00zł", "Przygoda", "Chuunibyou demo Koi ga Shitai!",
                        "93 min.", "Dorastanie jest zbyt mainstreamowe.", 430000),
                new VideoData("Amagi Brilliant Park",
                        "https://static.animagia.pl/Amagi4.jpg",
                        "https://animagia.pl/amagi-brilliant-park-odc-1/", 13,
                        "https://animagia.pl/wp-content/uploads/2018/05/kv-for-store-page.png",
                        "00:03:03.115;00:08:03.115",
                        "34,90zł", "Przygoda", "",
                        "13 × 24 min.","Najbardziej magiczne miejse na Ziemi.", Integer.MAX_VALUE),
                new VideoData("Home Sweet Home",
                        "https://static.animagia.pl/Hana_poster.jpg",
                        "https://animagia.pl", 1,
                        "https://animagia.pl/wp-content/uploads/2019/02/HanaIro_store_page.png",
                        "00:04:03.115;00:11:03.115",
                        "24,90zł", "Obyczajowy", "Hanasaku Iroha: ",
                        "66 min.", "Chcę lśnić! Ale czy tu mogę zabłysnąć?", 913000),
                new VideoData("Przeszłość",
                        "https://static.animagia.pl/Past_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przeszlosc/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/03/knk_past_store_page.png",
                        "00:03:03.115;00:04:03.115",
                        "19,90zł", "Akcja, dramat", "Kyoukai no Kanata –",
                        "86 min.", "Początek historii Mirai i Akihito.", 772000),
                new VideoData("Przyszłość",
                        "https://static.animagia.pl/Future_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przyszlosc/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/03/future_store_page.png",
                        "00:00:34.019;01:25:31.738;01:28:36.465",
                        "19,90zł", "Akcja, dramat", "Kyoukai no Kanata –",
                        "89 min.", "Mirai i Akihito walczą o lepszą przyszłość, ale ich moce mają swoją cenę.", 768000),
                new VideoData("Tamako Love Story",
                        "https://static.animagia.pl/Tamako_poster.jpg",
                        "https://animagia.pl/tamako-love-story/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/04/Tamako_store_page.png",
                        "00:05:03.115;00:08:03.115",
                        "19,90zł", "Obyczajowy", "",
                        "83 min.", "Czy to miłość? Tak.", 645000),
        };

        arr = Arrays.copyOfRange(fullArr, 0, totalTitles);

        return arr;
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
