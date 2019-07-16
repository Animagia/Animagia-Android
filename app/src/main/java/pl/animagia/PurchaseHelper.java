package pl.animagia;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
                    skuList.add("knk_past");
                    skuList.add("knk_future");
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
                                            if ("knk_past".equals(sku)) {
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
        private final MainActivity activity;

        public DummyListener(MainActivity ma) {
            this.activity = ma;
        }

        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            //FIXME
        }
    }
}
