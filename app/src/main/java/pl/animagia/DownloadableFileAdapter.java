package pl.animagia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DownloadableFileAdapter extends ArrayAdapter<String> {

    public DownloadableFileAdapter(Context context, List<String> links) {
        super(context, R.layout.downloadable_file, R.id.link_to_file, links);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        TextView link = v.findViewById(R.id.link_to_file);

        link.setText(getItem(position));

        return v;
    }
}