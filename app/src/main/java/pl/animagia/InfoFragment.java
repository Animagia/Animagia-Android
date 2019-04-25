package pl.animagia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

public class InfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = R.layout.fragment_info;

        View contents = inflater.inflate(layoutResource, container, false);

        FrameLayout frame =
                (FrameLayout) inflater.inflate(R.layout.fragment_frame, container, false);

        frame.addView(contents);

        return frame;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Cookies.removeCookie(Cookies.LOGIN, getActivity());

            Button rulesButton = getView().findViewById(R.id.infoLink1);
            rulesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    runBrowserIntent("https://animagia.pl/regulamin");
                }
            });

        Button privacyButton = getView().findViewById(R.id.infoLink2);
        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runBrowserIntent("https://animagia.pl/privacy");
            }
        });

        Button creditsButton = getView().findViewById(R.id.infoLink3);
        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runBrowserIntent("https://animagia.pl/credits");
            }
        });
    }

    private void runBrowserIntent(String uri){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(browserIntent);
    }

}