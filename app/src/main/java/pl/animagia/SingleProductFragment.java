package pl.animagia;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

public class SingleProductFragment extends Fragment {

    enum ArgumentKeys {
        ANIME
    }

    public static SingleProductFragment newInstance(Anime anime) {

        Bundle args = new Bundle();
        args.putString(ArgumentKeys.ANIME.name(), anime.name());

        SingleProductFragment fragment = new SingleProductFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_product, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btn = view.findViewById(R.id.buy_film_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "hello", Toast.LENGTH_SHORT).show();
            }
        });

        Anime vd = Anime.valueOf(getArguments().getString(ArgumentKeys.ANIME.name()));

        TextView title = view.findViewById(R.id.product_title);
        title.setText(vd.formatFullTitle());

        ImageView preview = view.findViewById(R.id.product_preview);
        Glide.with(getContext())
                .load(vd.getThumbnailAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/oscar_nord.jpg"))
                .into(preview);

        ImageView poster = view.findViewById(R.id.product_poster_image);
        Glide.with(getContext())
                .load(vd.getPosterAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/oscar_nord.jpg"))
                .into(poster);


    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("");
    }
}