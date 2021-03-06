package pl.animagia;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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

        TextView errorMessage = rule.getActivity().findViewById(R.id.errorMessage);

        Assert.assertEquals(fragment.getString(R.string.wrong_credentials), errorMessage.getText().toString());
    }

}
