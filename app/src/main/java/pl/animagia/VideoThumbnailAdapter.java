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

    private static VideoData arr[];

    public VideoThumbnailAdapter(Context context) {
        super(context, R.layout.video_thumbnail, R.id.thumbnail_text, prepareVideos());
        for(int i = 0; i < arr.length; i++){
            if (arr[i].getTitle().equals("")){
                getImage(i);
            }
        }
    }

    private static VideoData[] prepareVideos() {


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

        VideoData[] fullArr = new VideoData[]{
                new VideoData("Chuunibyou demo Koi ga Shitai! Take On Me",
                        "https://static.animagia.pl/film_poster.jpg",
                        "https://animagia.pl/chuunibyou-demo-koi-ga-shitai-take-on-me/", 1),
                new VideoData("Amagi Brilliant Park",
                        "https://static.animagia.pl/Amagi4.jpg",
                        "https://animagia.pl/amagi-brilliant-park-odc-1/", 13),
                new VideoData("Hanasaku Iroha: Home Sweet Home",
                        "https://static.animagia.pl/Hana_poster.jpg",
                        "https://animagia.pl/", 1),
                new VideoData("Kyoukai no Kanata: I'll Be Here – przeszłość",
                        "https://static.animagia.pl/KnK_past_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przeszlosc/", 1),
                new VideoData("Kyoukai no Kanata: I'll Be Here – przyszłość",
                        "https://static.animagia.pl/KnK_future_poster.jpg",
                        "https://animagia.pl/kyoukai-no-kanata-ill-be-here-przyszlosc/", 1),
                new VideoData("Tamako Love Story",
                        "https://static.animagia.pl/Tamako_poster.jpg",
                        "https://animagia.pl/tamako-love-story.", 1),
        };

        arr = Arrays.copyOfRange(fullArr, 0, totalTitles);

        return arr;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        ImageView poster = thumbnail.findViewById(R.id.thumbnail_poster);

        if(super.getItem(position).getPosterAsssetUri().equals("")){
            Glide.with(getContext())
                    .load(new ColorDrawable(Color.GRAY))
                    .into(poster);
        } else {
            Glide.with(getContext())
                    .load(super.getItem(position).getPosterAsssetUri())
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


    private void changeImage(int position, String url) {
        arr[position].setUri(url);
        notifyDataSetChanged();
    }

    private void getImage(final int i) {
        HTML.getHtml(arr[i].getVideoUrl(), getContext(),  new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                String uri = getImageUrl(result);
                changeImage(i, uri);

            }

            @Override
            public void onFailure(VolleyError volleyError) {
                changeImage(i, "file:///android_asset/serrah_galos.jpg");
            }
        });
    }

}