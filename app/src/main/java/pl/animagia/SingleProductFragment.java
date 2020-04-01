package pl.animagia;

import android.annotation.SuppressLint;
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
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.bumptech.glide.Glide;
import pl.animagia.token.TokenStorage;
import pl.animagia.user.CookieStorage;

public class SingleProductFragment extends Fragment {


    enum ArgumentKeys {
        ANIME
    }

    static SingleProductFragment newInstance(Anime anime) {

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

        final Anime anime = Anime.valueOf(getArguments().getString(ArgumentKeys.ANIME.name()));

        TextView title = view.findViewById(R.id.product_title);
        title.setText(anime.formatFullTitle());

        Button buyButton = view.findViewById(R.id.buy_film_button);
        buyButton.setText(getResources().getString(R.string.buy_for_some_PLN, anime.getPrice()));
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchase(anime);
            }
        });

        if(alreadyPurchased(anime)) {
            disablePurchaseButton();
        }

        ImageView preview = view.findViewById(R.id.product_preview);
        Glide.with(getContext())
                .load(anime.getThumbnailAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/clapperboard.jpg"))
                .into(preview);

        ImageView poster = view.findViewById(R.id.product_poster_image);
        Glide.with(getContext())
                .load(anime.getPosterAsssetUri())
                .error(Glide.with(getContext()).load("file:///android_asset/clapperboard.jpg"))
                .into(poster);
    }


    private void startPurchase(Anime anime) {
        PurchaseHelper.startPurchase(this, anime);
    }



    void onSuccessfulPurchase() {
        disablePurchaseButton();
    }


    private boolean alreadyPurchased(Anime a) {
        for (Anime anime : TokenStorage.getLocallyPurchasedAnime(getActivity())) {
            if(anime.formatFullTitle().equals(a.formatFullTitle())) {
                return true;
            }
        }

        return CookieStorage.getNamesOfFilesPurchasedByAccount(getActivity()).toString()
                .contains(a.formatFullTitle().split(" ")[0]);
    }


    private void disablePurchaseButton() {
        Button purchaseButton = getView().findViewById(R.id.buy_film_button);
        purchaseButton.setText(R.string.already_bought);
        purchaseButton.setEnabled(false);
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("");
    }

}
