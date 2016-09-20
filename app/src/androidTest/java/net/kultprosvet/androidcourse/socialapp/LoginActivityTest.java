package net.kultprosvet.androidcourse.socialapp;

import android.content.Context;
import android.os.Environment;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.Matchers.not;
/**
 * Created by Stanislav Volnjanskij on 25.08.16.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static UiDevice uiDevice;
    @Rule
    public ActivityTestRule<LoginActivity> rule=new ActivityTestRule<LoginActivity>(LoginActivity.class);
    private static Context context;
    private Object currentActivity;
    private int counter=1;
    @BeforeClass
    public static void init(){
        context=getInstrumentation().getTargetContext();
        uiDevice = UiDevice.getInstance(getInstrumentation());
    }

    @Before
    public void setUp(){
        onView(withId(R.id.email)).perform(replaceText(""));
        onView(withId(R.id.password)).perform(replaceText(""));
    }

    @Test
    public void doubleClickTest(){
        onView(withId(R.id.email)).perform(replaceText("stas@kultprosvet.net"));
        onView(withId(R.id.password)).perform(replaceText("123456"));
        onView(withId(R.id.btn_login)).perform(click());
        //button should be disabled
        onView(withId(R.id.btn_login)).check(matches(not(ViewMatchers.isEnabled())));
    }
    @Test
    public void emailFieldVerifyCheck(){
        onView(withId(R.id.btn_login)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(context.getString(R.string.error_toast_enter_email))));
    }
    @Test
    public void emailFormatCheck(){
        onView(withId(R.id.email)).perform(replaceText("xxxx"));
        onView(withId(R.id.btn_login)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(context.getString(R.string.wrong_email_format))));
    }
    @Test
    public void loginTest(){
        takeScreenshot("start");
        onView(withId(R.id.email)).perform(replaceText("roma@ya.ru"));
        onView(withId(R.id.password)).perform(replaceText("123456"));
        takeScreenshot("field filled");
        onView(withId(R.id.btn_login)).perform(click());
        takeScreenshot("login");
    }
    @After
    public void tearDown(){
        getActivityInstance();
        if (currentActivity instanceof  MainActivity){
            onView(withId(R.id.sign_out)).perform(click());
        }
    }

    public void getActivityInstance(){
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()){
                     currentActivity = resumedActivities.iterator().next();
                }
            }
        });
    }

    public void takeScreenshot(String name) {
        File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!pic.exists()) {
            pic.mkdir();
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Screenshots");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir.getAbsolutePath() + "/"+String.format("%02d", counter)+"_" + name + ".png");
        uiDevice.takeScreenshot(file);
        counter++;
    }
}
