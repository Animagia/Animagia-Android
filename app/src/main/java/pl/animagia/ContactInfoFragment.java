package pl.animagia;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import pl.animagia.error.Alerts;
import pl.animagia.html.CookieRequest;
import pl.animagia.user.Cookies;


public class ContactInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = R.layout.fragment_contact_info;

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame =
                (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }

}
