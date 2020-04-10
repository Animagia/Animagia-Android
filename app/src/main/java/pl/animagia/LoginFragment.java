package pl.animagia;


import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import pl.animagia.dialog.Dialogs;
import pl.animagia.user.CookieStorage;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button signIn = (Button) getActivity().findViewById(R.id.sign_in_button);
        final EditText emailText = getActivity().findViewById(R.id.email);
        final EditText passwordText = getActivity().findViewById(R.id.password);
        final ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);
        final TextView errorMessage = getActivity().findViewById(R.id.errorMessage);
        signIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                errorMessage.setText("");
                emailText.setFocusable(false);
                passwordText.setFocusable(false);
                signIn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);


                final String email = emailText.getText().toString();
                final String password = passwordText.getText().toString();

                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

                View headView = navigationView.getHeaderView(0);
                final TextView emailTextView = headView.findViewById(R.id.userEmail);

                RequestQueue queue = Volley.newRequestQueue(getContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://animagia.pl/wp-login.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        progressBar.setVisibility(View.GONE);
                        signIn.setEnabled(true);
                        passwordText.setFocusableInTouchMode(true);
                        passwordText.setFocusable(true);
                        emailText.setFocusableInTouchMode(true);
                        emailText.setFocusable(true);
//
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError instanceof NoConnectionError) {
                            DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            };
                            Dialogs.internetConnectionError(getContext(), onClickTryAgain);
                        }
                        progressBar.setVisibility(View.GONE);
                        signIn.setEnabled(true);
                        passwordText.setFocusableInTouchMode(true);
                        passwordText.setFocusable(true);
                        emailText.setFocusableInTouchMode(true);
                        emailText.setFocusable(true);

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("log", email);
                        params.put("pwd", password);
                        params.put("testcookie", "1");

                        return params;
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        Log.i("response",response.headers.toString());
                        Map<String, String> responseHeaders = response.headers;
                        String rawCookies = responseHeaders.get("Set-Cookie");
                        int firstIndex = rawCookies.indexOf(";");
                        String cookie = rawCookies.substring(0, firstIndex);
                        if(cookie.startsWith("wordpress_logged_in")) {
                            CookieStorage.setCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, cookie, getActivity());
                            activateFragment(new CatalogFragment());
                            emailTextView.setText(email);
                        }
                        else {
                            setText(errorMessage,getString(R.string.wrong_credentials));
                        }
                        Log.i("cookies",rawCookies);
                        return super.parseNetworkResponse(response);
                    }

                };
                queue.add(stringRequest);

                hideSoftKeyboard();
            }
        });

    }

    private void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }

    private void hideSoftKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
    private void setText(final TextView text,final String value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

}
