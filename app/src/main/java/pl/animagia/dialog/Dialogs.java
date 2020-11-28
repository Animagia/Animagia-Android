package pl.animagia.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import pl.animagia.Anime;
import pl.animagia.FullscreenPlaybackActivity;
import pl.animagia.MainActivity;
import pl.animagia.R;

public class Dialogs {


    public static void internetConnectionError(final Context context, DialogInterface.OnClickListener onClickTryAgainButton) {
        String message = context.getString(R.string.no_connection_message);
        DialogInterface.OnClickListener turnOnInternet = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_SETTINGS);
                ((Activity) context).startActivityForResult(i, 0);
            }
        };
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.no_connection_short)
                .setMessage(message)
                .setPositiveButton(R.string.try_again, onClickTryAgainButton)
                .setNegativeButton(R.string.turn_on_internet, turnOnInternet)
                .show();
    }


    public static void primeVideoError(final FullscreenPlaybackActivity fpa, final Anime anime) {
        new AlertDialog.Builder(fpa)
                .setCancelable(true)
                .setMessage(R.string.free_streaming_message)
                .setNegativeButton(R.string.return_from_dialog, null)
                .setPositiveButton("Sklep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent showProductInShop = new Intent(fpa, MainActivity.class);
                        showProductInShop.putExtra(
                                MainActivity.OPTIONAL_NAME_OF_PRODUCT_TO_IMMEDIATELY_SHOW, anime);
                        fpa.startActivity(showProductInShop);
                    }
                })
                .show();
    }


    public static void showAccountCreation() {

    }


    public static void showSubtitleSelection(Context c, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);

        builder.setTitle("Wybierz wersję językową");

        String[] items = {"Napisy „mniej spolszczone”", "Napisy „bardziej spolszczone”"};
        builder.setSingleChoiceItems(items, 0, listener);

        builder.show();
    }
}
