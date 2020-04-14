package pl.animagia;

import android.content.Context;
import android.widget.Toast;

public class Toasts {
    public static void promptUserToTryAgain(Context ctx) {
        Toast.makeText(ctx, R.string.something_went_wrong_try_again, Toast
                .LENGTH_SHORT).show();
    }
}
