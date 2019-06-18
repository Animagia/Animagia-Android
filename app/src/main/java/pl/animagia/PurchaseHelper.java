package pl.animagia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class PurchaseHelper {


    private PurchaseHelper() {

    }

    private static JSONObject getBaseRequest() {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }


    private static JSONObject getTokenizationSpecification() {
        JSONObject tokenizationSpecification = new JSONObject();
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
        tokenizationSpecification.put(
                "parameters",
                new JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleMerchantId"));

        return tokenizationSpecification;
    }


    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray()
                .put("MASTERCARD")
                .put("VISA");
    }


    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }


    private static JSONObject getBaseCardPaymentMethod() {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");
        cardPaymentMethod.put(
                "parameters",
                new JSONObject()
                        .put("allowedAuthMethods", getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", getAllowedCardNetworks()));

        return cardPaymentMethod;
    }


    private static JSONObject getCardPaymentMethod() {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification());

        return cardPaymentMethod;
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
