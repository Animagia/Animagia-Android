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

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;


public class VideoThumbnailAdapter extends ArrayAdapter<VideoData> {

    private static VideoData arr[]; //FIXME should this really be static?

    public VideoThumbnailAdapter(Context context) {
        super(context, R.layout.video_thumbnail, R.id.thumbnail_text, prepareVideos());
    }

    static VideoData[] prepareVideos() {


        GregorianCalendar firstMarchRelease = new GregorianCalendar();
        firstMarchRelease.setTimeZone(TimeZone.getTimeZone("UTC"));
        firstMarchRelease.set(2019, GregorianCalendar.MARCH,  12);

        GregorianCalendar secondMarchRelease = new GregorianCalendar();
        secondMarchRelease.setTimeZone(TimeZone.getTimeZone("UTC"));
        secondMarchRelease.set(2019, GregorianCalendar.MARCH,  23);

        GregorianCalendar aprilRelease = new GregorianCalendar();
        aprilRelease.setTimeZone(TimeZone.getTimeZone("UTC"));
        aprilRelease.set(2019, GregorianCalendar.APRIL,  9);

        GregorianCalendar now = new GregorianCalendar();
        now.setTimeZone(TimeZone.getTimeZone("UTC"));

        int totalTitles = 3;
        if(now.after(aprilRelease)) {
            totalTitles += 3;
        } else if(now.after(secondMarchRelease)) {
            totalTitles += 2;
        } else if(now.after(firstMarchRelease)) {
            totalTitles++;
        }

        if(appIsOutdated()) {
            totalTitles = 0;
        }

        VideoData[] fullArr = new VideoData[]{
                new VideoData("Chuunibyou demo Koi ga Shitai! Take On Me",
                        "https://static.animagia.pl/Chuu_poster.jpg",
                        "https://animagia.pl/chuunibyou-demo-koi-ga-shitai-take-on-me/", 1,
                        "https://animagia.pl/wp-content/uploads/2018/07/umbrella_for_store_page.png",
                        "00:33:03.115;01:05:03.115",
                        29.9,"Romance, Drama", "polish subtitles"),
                new VideoData("Amagi Brilliant Park",
                        "https://static.animagia.pl/Amagi4.jpg",
                        "https://animagia.pl/amagi-brilliant-park-odc-1/", 13,
                        "https://animagia.pl/wp-content/uploads/2018/05/kv-for-store-page.png",
                        "00:03:03.115;00:08:03.115",
                        19.9,"Romance, Adventure", ""),
                new VideoData("Hanasaku Iroha: Home Sweet Home",
                        "https://static.animagia.pl/Hana_poster.jpg",
                        "https://animagia.pl", 1,
                        "https://animagia.pl/wp-content/uploads/2019/02/HanaIro_store_page.png",
                        "00:04:03.115;00:11:03.115",
                        14.9,"Drama, Tragedy", "polish subtitles"),
                new VideoData("Kyoukai no Kanata: I'll Be Here – przeszłość",
                        "https://static.animagia.pl/Past_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przeszlosc/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/03/knk_past_store_page.png",
                        "00:03:03.115;00:04:03.115",
                        19.9,"Adventure, Drama", "polish subtitles"),
                new VideoData("Kyoukai no Kanata: I'll Be Here – przyszłość",
                        "https://static.animagia.pl/Future_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przyszlosc/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/03/future_store_page.png",
                        "00:00:34.019;01:25:31.738;01:28:36.465",
                        29.9,"Romance, Drama", "polish subtitles"),
                new VideoData("Tamako Love Story",
                        "https://static.animagia.pl/Tamako_poster.jpg",
                        "https://animagia.pl/tamako-love-story/", 1,
                        "https://animagia.pl/wp-content/uploads/2019/04/Tamako-poster.png",
                        "00:05:03.115;00:08:03.115",
                        49.9,"Romance, Drama", "polish subtitles"),
        };

        arr = Arrays.copyOfRange(fullArr, 0, totalTitles);

        return arr;
    }

    private static boolean appIsOutdated() {
        GregorianCalendar appBecomesOutdated = new GregorianCalendar();
        appBecomesOutdated.setTimeZone(TimeZone.getTimeZone("UTC"));
        appBecomesOutdated.set(2020, GregorianCalendar.JANUARY,  1);

        GregorianCalendar now = new GregorianCalendar();
        now.setTimeZone(TimeZone.getTimeZone("UTC"));

        return now.after(appBecomesOutdated);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);

        if(super.getItem(position).getThumbnailAsssetUri().equals("")){
            Glide.with(getContext())
                    .load(new ColorDrawable(Color.GRAY))
                    .into(poster);
        } else {
            Glide.with(getContext())
                    .load(super.getItem(position).getThumbnailAsssetUri())
                    .error(Glide.with(getContext()).load("file:///android_asset/oscar_nord.jpg"))
                    .into(poster);
        }


        return thumbnail;
    }

    private static String getImageUrl(String html) {
        String line = getImageLine(html);


        String customString = "";
        if (line.equals("")) {
            customString = "file:///android_asset/oscar_nord.jpg";
        } else {
            int firstIndex = line.indexOf("poster=") + "poster=".length()+1;
            String subline = line.substring(firstIndex);
            int last = subline.indexOf("\"") + firstIndex;
            customString =  line.substring(firstIndex, last);
        }

        return customString;
    }

    private static String getImageLine(String html) {
        Boolean read = true;
        String urlLine = "";
        BufferedReader reader = new BufferedReader(new StringReader(html));
        try {
            String line = reader.readLine();
            while(line != null && read){
                if(line.contains("<video ")){
                    while (line != null && read) {
                        if(line.contains("poster")){
                            urlLine = line;
                            read = false;

                        }
                        line = reader.readLine();
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlLine;
    }

}
