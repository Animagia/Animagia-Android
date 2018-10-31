package pl.animagia;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;

import pl.animagia.user.Cookies;

public class LoginPopupFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        String cookie = Cookies.getCookie(Cookies.LOGIN, getActivity());
        System.out.println(cookie);
        if (cookie.equals(Cookies.COOKIE_NOT_FOUND)){
            builder.setMessage("Zaloguj się").setPositiveButton("Zaloguj się", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    activateFragment(new LoginFragment());
                }
            });
        }
        else {
            builder.setMessage("Wyloguj się").setPositiveButton("Wyloguj się", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }
}
