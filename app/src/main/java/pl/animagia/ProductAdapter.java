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


public class ProductAdapter extends ArrayAdapter<Anime> {

    public ProductAdapter(Context context) {
        super(context, R.layout.product_card, R.id.product_title, VideoThumbnailAdapter.prepareVideos());
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View thumbnail = super.getView(position, convertView, parent);
        ImageView poster = thumbnail.findViewById(R.id.product_poster);

        TextView priceView = thumbnail.findViewById(R.id.product_price);
        TextView genresView = thumbnail.findViewById(R.id.product_genres);
        TextView titleView = thumbnail.findViewById(R.id.product_title);

        if (super.getItem(position).getPosterAsssetUri().equals("")) {
            Glide.with(getContext())
                    .load(new ColorDrawable(Color.WHITE))
                    .into(poster);
        } else {
            Glide.with(getContext())
                    .load(super.getItem(position).getPosterAsssetUri())
                    .error(Glide.with(getContext()).load("file:///android_asset/clapperboard.jpg"))
                    .into(poster);

            priceView.setText(super.getItem(position).getPrice());
            genresView.setText(super.getItem(position).getGenres());

            titleView.setText(super.getItem(position).formatFullTitle());
        }

        return thumbnail;
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