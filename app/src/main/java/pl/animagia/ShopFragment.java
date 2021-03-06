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

        final ProductAdapter adapter = new ProductAdapter((MainActivity) getActivity());
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                openProduct(adapter.getItem(position));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_shop);
    }


    private void openProduct(Anime vd) {
        SingleProductFragment frag = SingleProductFragment.newInstance(vd);

        MainActivity ma = (MainActivity) getActivity();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                //.addSharedElement(holder.image, "sharedImage")
                .replace(R.id.frame_for_content, frag)
                .addToBackStack(null)
                .commit();
    }


    public void setText(String message) {
        LinearLayout linearLayout = getActivity().findViewById(R.id.catalog_layout);

        TextView view = getActivity().findViewById(R.id.geo_text_view);
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


}
