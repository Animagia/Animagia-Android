package pl.animagia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import com.android.billingclient.api.*;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHelper {


    private PurchaseHelper() {

    }


    private static void startPurchase(final MainActivity ma) {

        BillingClient.Builder builder = BillingClient.newBuilder(ma);
        builder.enablePendingPurchases();
        builder.setListener(new DummyListener(ma));

        ma.billingClient = builder.build();

        ma.billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    List<String> skuList = new ArrayList<>();
                    skuList.add("android.test.canceled");
                    skuList.add("android.test.purchased");
                    skuList.add("android.test.item_unavailable");
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    ma.billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {

                                    if (billingResult.getResponseCode() ==
                                            BillingClient.BillingResponseCode.OK &&
                                            skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();
                                            if ("android.test.purchased".equals(sku)) {
                                                com.android.billingclient.api.BillingFlowParams
                                                        flowParams =
                                                        BillingFlowParams.newBuilder()
                                                                .setSkuDetails(skuDetails)
                                                                .build();
                                                BillingResult flowResult =
                                                        ma.billingClient
                                                                .launchBillingFlow(ma, flowParams);

                                            }
                                        }
                                    }


                                }
                            });
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
        private final MainActivity ma;

        public DummyListener(MainActivity ma) {
            this.ma = ma;
        }

        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            Toast.makeText(ma, "Purchases updated: " + list, Toast.LENGTH_LONG).show();
        }
    }
}
