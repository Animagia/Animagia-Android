package pl.animagia.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import pl.animagia.Anime;
import pl.animagia.FullscreenPlaybackActivity;
import pl.animagia.R;
import pl.animagia.SingleProductFragment;

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


    private static Point getDisplayDimensions(Context context)
    {
        WindowManager wm = ( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        return new Point( screenWidth, screenHeight );
    }


    public static void showSubtitleSelection(Context c, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);

        builder.setTitle("Wybierz wersję językową");

        String[] items = {"Napisy „mniej spolszczone”", "Napisy „bardziej spolszczone”"};
        builder.setSingleChoiceItems(items, 0, listener);

        builder.show();
    }
}
