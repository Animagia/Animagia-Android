package pl.animagia;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Button signIn = (Button) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText loginText = findViewById(R.id.login);
                EditText passwordText = findViewById(R.id.password);

                String login = loginText.getText().toString();
                String password = passwordText.getText().toString();

                Toast.makeText(getApplicationContext(), login + " " + password, Toast.LENGTH_SHORT).show();
                finish();

            }
        });

    }

}