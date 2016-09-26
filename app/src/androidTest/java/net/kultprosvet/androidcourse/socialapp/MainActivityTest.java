package net.kultprosvet.androidcourse.socialapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.kultprosvet.androidcourse.socialapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by RomanFomenko on 26.09.2016.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static UiDevice uiDevice;
    private static final String EMAIL = "roma@ya.ru";
    private static final String PASSWORD = "123456";
    private static final long SLEEP_TIME_MILLIS = 2000L;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);
    private static Context context;

    @BeforeClass
    public static void init(){
        context=getInstrumentation().getTargetContext();
        uiDevice = UiDevice.getInstance(getInstrumentation());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(EMAIL, PASSWORD);
            try {
                Thread.sleep(SLEEP_TIME_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void scrollToPositionTest() {
        onView(withId(R.id.main_posts_list)).perform(RecyclerViewActions.scrollToPosition(3));
    }

    @Test
    public void clickAtPositionTest() {
        onView(withId(R.id.main_posts_list)).perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));
//        onView(withId(R.id.post_author)).check(matches(withText(post.getAuthor())));
//        onView(withId(R.id.post_title)).check(matches(withText(post.getTitle())));
//        onView(withId(R.id.post_body)).check(matches(withText(post.getBody())));
        Espresso.pressBack();
    }

    @Test
    public void logOutTest() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(context.getString(R.string.log_out))).perform(click());
    }
}
