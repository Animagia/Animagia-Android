package pl.animagia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class ShopDialogHelper {

    private ShopDialogHelper() {

    }

    public static void showDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(R.string.product_in_browser);

        builder.setNegativeButton(R.string.return_from_dialog, null);
        builder.setPositiveButton(R.string.open_something_from_dialog,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://animagia.pl/sklep"));
                        ctx.startActivity(browserIntent);

                    }
                });

        builder.show();
    }

}
