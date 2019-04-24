package pl.animagia.error;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import pl.animagia.R;

public class Alerts {

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

    public static void primeVideoError(final Context context){
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(R.string.free_streaming_message)
                .setNegativeButton(R.string.return_from_dialog, null)
                .show();
    }

    public static void logInError(final Context context, DialogInterface.OnClickListener login){
        String message = context.getString(R.string.not_logged);
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.login_error)
                .setMessage(message)
                .setNeutralButton(R.string.action_sign_in, login)
                .show();
    }
}
