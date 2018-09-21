package pl.animagia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LoginTest{
    LoginFragment fragment;
    private final String EMAIL =  "email";
    private final String PASSWD = "password";

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, false, false);


    @Before
    public void setup() {
        Intent intent = new Intent();
        rule.launchActivity(intent);
    }


    @Test
    public void getLoginAndPasswordTest() {
        fragment = new LoginFragment();

        rule.getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frame_for_content, fragment).commitAllowingStateLoss();

        onView(withId(R.id.email)).perform(typeText(EMAIL), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(PASSWD),closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());

        NavigationView nav = rule.getActivity().findViewById(R.id.nav_view);
        View headView = nav.getHeaderView(0);
        TextView emailTextView = headView.findViewById(R.id.userEmail);

        Assert.assertEquals(EMAIL, emailTextView.getText().toString());
    }

}
