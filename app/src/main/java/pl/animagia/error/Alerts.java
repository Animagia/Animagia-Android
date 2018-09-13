package pl.animagia.error;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

public class Alerts {

    public static void internetConnectionError(final Context context, DialogInterface.OnClickListener onClickTryAgainButton) {
        String message = "Brak połączenia z internetem. Sprawdź swoje połączenie i spróbuj ponownie";
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
                .setTitle("Brak połącznia z internetem")
                .setMessage(message)
                .setPositiveButton("Spróbuj ponownie", onClickTryAgainButton)
                .setNegativeButton("Włącz internet", turnOnInternet)
                .show();
    }
}