package pl.animagia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import com.android.billingclient.api.*;
import com.android.volley.VolleyError;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;

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
                    skuList.add("knk_past");
                    skuList.add("hanairo");
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
                                            if ("android.test.canceled".equals(sku)) {
                                                com.android.billingclient.api.BillingFlowParams
                                                        flowParams =
                                                        BillingFlowParams.newBuilder()
                                                                .setSkuDetails(skuDetails)
                                                                .build();

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


    private static class DummyListener implements PurchasesUpdatedListener {
        private final MainActivity ma;

        public DummyListener(MainActivity ma) {
            this.ma = ma;
        }

        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {

            for (Purchase purchase :  list ) {
                if(Purchase.PurchaseState.PURCHASED == purchase.getPurchaseState()) {
                    TokenStorage.storePurchase(ma, Anime.forSku(purchase.getSku()), purchase);
                    return;
                }
            }

            Toast.makeText(ma, "Coś poszło nie tak.", Toast.LENGTH_SHORT).show();
        }
    }


    private static void getDdlLink(Context ctx, String combinedToken) {

        String url = TokenAssembly.URL_BASE + combinedToken;
        HTML.getHtml(url, ctx, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                String downloadAnchor = FilesFragment.getDownloadAnchors(result).get(0);

                String fileName = FilesFragment.extractFileName(downloadAnchor);
                String downloadUrl = FilesFragment.extractUrl(downloadAnchor);

                return;
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                return; //FIXME
            }
        });
    }

}
