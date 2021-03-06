package pl.animagia;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import com.android.billingclient.api.*;
import com.android.volley.VolleyError;
import pl.animagia.html.HtClient;
import pl.animagia.html.VolleyCallback;
import pl.animagia.token.TokenAssembly;
import pl.animagia.token.TokenStorage;

import java.util.ArrayList;
import java.util.List;

public class PurchaseHelper {


    private PurchaseHelper() {

    }


    static void startPurchase(final SingleProductFragment fragment, final Anime anime) {

        final Activity ctx = fragment.getActivity();

        BillingClient.Builder builder = BillingClient.newBuilder(ctx);
        builder.enablePendingPurchases();
        builder.setListener(new AnimePurchaseListener(fragment));

        final BillingClient billingClient = builder.build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>();
                    skuList.add(anime.getSku().toLowerCase());
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {

                                    if (billingResult.getResponseCode() ==
                                            BillingClient.BillingResponseCode.OK &&
                                            skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            if (anime.getSku().equalsIgnoreCase(sku)) {
                                                com.android.billingclient.api.BillingFlowParams
                                                        flowParams =
                                                        BillingFlowParams.newBuilder()
                                                                .setSkuDetails(skuDetails)
                                                                .build();

                                                billingClient
                                                        .launchBillingFlow(ctx, flowParams);

                                                break;
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


    private static class AnimePurchaseListener implements PurchasesUpdatedListener {
        private final Activity ctx;
        private final SingleProductFragment spf;


        AnimePurchaseListener(SingleProductFragment fragment)
        {
            this.spf = fragment;
            this.ctx = fragment.getActivity();
        }

        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            if(list == null) {
                return;
            }

            for (Purchase purchase : list ) {
                if(Purchase.PurchaseState.PURCHASED == purchase.getPurchaseState()) {
                    TokenStorage.storePurchase(ctx, purchase.getSku(), purchase);

                    spf.onSuccessfulPurchase();

                    return;
                }
            }

            Toast.makeText(ctx, "Coś poszło nie tak.", Toast.LENGTH_SHORT).show();
        }
    }


    private static void getDdlLink(Context ctx, String combinedToken) {

        String url = TokenAssembly.URL_BASE + combinedToken;
        HtClient.getHtml(url, ctx, new VolleyCallback() {
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
