package pl.animagia;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ContactInfoFragment extends TopLevelFragment {

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Cookies.removeCookie(Cookies.LOGIN, getActivity());

        Button fbButton = getView().findViewById(R.id.fbButton);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fb.com/WydawnictwoAnimagia"));
                getActivity().startActivity(browserIntent);
            }
        });

        Button emailButton = getView().findViewById(R.id.emailButton);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setType("message/rfc822");
                i.setData(Uri.parse("mailto:"));
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"animagia@animagia.pl"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Temat wiadomości mail");
                i.putExtra(Intent.EXTRA_TEXT   , "Treść wiadomości mail");
                try {
                    startActivity(Intent.createChooser(i, "Wybierz aplikację..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Nie ma zainstalowanej aplikacji do wysyłania maila.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final TextView signalNumber = getView().findViewById(R.id.signalNumber);
        ImageView signal = getView().findViewById(R.id.signalCopy);
        signal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Toast.makeText(getActivity(), "Numer skopiowany", Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Skopiowany..", signalNumber.getText());
                    clipboard.setPrimaryClip(clip);
                }catch(NullPointerException e){

                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.drawer_item_contact_info);
    }



}
