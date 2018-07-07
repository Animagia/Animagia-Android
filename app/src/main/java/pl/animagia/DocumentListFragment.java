package pl.animagia;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;


public class DocumentListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_document_list, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String[] values = {"good", "bad"};

        ListView listView = (ListView) view;

        ListAdapter adapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                        values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openDocument(position);
            }
        });
    }

    private void openDocument(int position) {
        Intent intent = new Intent(getActivity(), DocumentActivity.class);
        //intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, videoData);
        startActivity(intent);
    }
}
