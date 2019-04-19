package pl.animagia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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


public class ProductAdapter extends ArrayAdapter<VideoData> {

    public ProductAdapter(Context context) {
        super(context, R.layout.product_card, R.id.product_text, VideoThumbnailAdapter.prepareVideos());
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        ImageView poster = thumbnail.findViewById(R.id.product_poster);

        TextView priceView = thumbnail.findViewById(R.id.product_price);
        TextView genresView = thumbnail.findViewById(R.id.product_genres);
        TextView subtitleView = thumbnail.findViewById(R.id.subtitle_text);

        if (super.getItem(position).getPosterAsssetUri().equals("")) {
            Glide.with(getContext())
                    .load(new ColorDrawable(Color.WHITE))
                    .into(poster);
        } else {
            Glide.with(getContext())
                    .load(super.getItem(position).getPosterAsssetUri())
                    .error(Glide.with(getContext()).load("file:///android_asset/oscar_nord.jpg"))
                    .into(poster);

            priceView.setText(super.getItem(position).getPrice() + "");
            genresView.setText(super.getItem(position).getGenres() + "");

            if(super.getItem(position).getSubtitles().equals("")){
                subtitleView.setVisibility(View.INVISIBLE);
            }else{
                subtitleView.setText(super.getItem(position).getSubtitles() + "");
                subtitleView.setVisibility(View.VISIBLE);
            }
        }

        return thumbnail;
    }

    private static String getImageUrl(String html) {
        String line = getImageLine(html);


        String customString = "";
        if (line.equals("")) {
            customString = "file:///android_asset/oscar_nord.jpg";
        } else {
            int firstIndex = line.indexOf("poster=") + "poster=".length() + 1;
            String subline = line.substring(firstIndex);
            int last = subline.indexOf("\"") + firstIndex;
            customString = line.substring(firstIndex, last);
        }

        return customString;
    }

    private static String getImageLine(String html) {
        Boolean read = true;
        String urlLine = "";
        BufferedReader reader = new BufferedReader(new StringReader(html));
        try {
            String line = reader.readLine();
            while (line != null && read) {
                if (line.contains("<video ")) {
                    while (line != null && read) {
                        if (line.contains("poster")) {
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