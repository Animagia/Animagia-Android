package pl.animagia;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SubtitleTrackAdapter extends ArrayAdapter<String> {

    public SubtitleTrackAdapter(Context context, String[] objects) {
        super(context, 0, objects);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);

        TextView t = findTextViewIn(v);

        return v;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);

        return v;
    }


    private TextView findTextViewIn(View v) {
        return null; //FIXME
    }

}
