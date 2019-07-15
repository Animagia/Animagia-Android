package pl.animagia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.android.billingclient.api.*;

import java.util.List;

public class PurchaseHelper {

    private BillingClient billingClient;


    private PurchaseHelper() {

    }


    private static void startPurchase(MainActivity ma) {

        ma.billingClient = BillingClient.newBuilder(ma).setListener(new DummyListener()).build();

        ma.billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }


    public static void showDialog(final MainActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(R.string.start_iap);

        builder.setNegativeButton(R.string.return_from_dialog, null);
        builder.setPositiveButton(R.string.continue_from_dialog,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startPurchase(activity);

                    }
                });

        builder.show();
    }

    private static class DummyListener implements PurchasesUpdatedListener {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            //FIXME
        }
    }
}
