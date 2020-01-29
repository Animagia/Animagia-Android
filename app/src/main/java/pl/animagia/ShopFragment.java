package pl.animagia;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ShopFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridview = (GridView) view.findViewById(R.id.gridview);

        final ProductAdapter adapter = new ProductAdapter(getActivity());
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                openProduct(v);
            }
        });
    }

    private void openProduct(View v) {


        TextView txt = v.findViewById(R.id.product_title);
        PurchaseHelper.PurchasableAnime p = identifyByTitle(txt.getText().toString());

        SingleProductFragment frag = SingleProductFragment.newInstance(p);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                //.addSharedElement(holder.image, "sharedImage")
                .replace(R.id.frame_for_content, frag)
                .addToBackStack(null)
                .commit();
    }


    public void setText(String message) {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.catalog_layout);

        TextView view = (TextView) getActivity().findViewById(R.id.geo_text_view);
        if(view == null){

            TextView textView = new TextView(getContext());
            textView.setId(R.id.geo_text_view);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(message);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            textView.setTextSize(18);

            linearLayout.addView(textView, 0);
        }
    }


    private static PurchaseHelper.PurchasableAnime identifyByTitle(String title) {
        if(title.contains("Przesz")) {
            return PurchaseHelper.PurchasableAnime.KNK_PAST;
        } else if(title.contains("Przysz")) {
            return PurchaseHelper.PurchasableAnime.KNK_FUTURE;
        } else if(title.contains("Tamako")) {
            return PurchaseHelper.PurchasableAnime.TAMAKO;
        } else if(title.contains("Chu")) {
            return PurchaseHelper.PurchasableAnime.CHU2;
        } else if(title.contains("Hanasaku")) {
            return PurchaseHelper.PurchasableAnime.HANAIRO;
        } else if(title.contains("Amagi")) {
            return PurchaseHelper.PurchasableAnime.AMAGI;
        }
        throw new RuntimeException("Purchasable anime not found.");
    }


}
