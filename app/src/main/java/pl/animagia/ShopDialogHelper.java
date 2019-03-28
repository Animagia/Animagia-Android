package pl.animagia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class ShopDialogHelper {

    private ShopDialogHelper() {

    }

    public static void showDialog(final Context ctx, final String productShopUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage("Żeby kontynuować zakup, otwórz produkt w przeglądarce internetowej.");

        builder.setNegativeButton("Wróć", null);
        builder.setNegativeButton("Otwórz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(productShopUrl));
                ctx.startActivity(browserIntent);

            }
        });

        builder.show();
    }

    public static void showDialog(final Context ctx) {
        showDialog(ctx, "https://animagia.pl/sklep");
    }

}
