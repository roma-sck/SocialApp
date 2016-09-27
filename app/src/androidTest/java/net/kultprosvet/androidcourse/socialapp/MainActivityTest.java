package net.kultprosvet.androidcourse.socialapp;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.kultprosvet.androidcourse.socialapp.models.Post;
import net.kultprosvet.androidcourse.socialapp.ui.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static net.kultprosvet.androidcourse.socialapp.Const.POSTS;

/**
 * Created by RomanFomenko on 26.09.2016.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static final String EMAIL = "roma@ya.ru";
    private static final String PASSWORD = "123456";
    private static final long SLEEP_TIME_MILLIS = 2000L;
    private static final int FIRST_POSITION = 1;
    private static UiDevice sUiDevice;
    private static Context sContext;
    private DatabaseReference mDbRef;
    private int mCounter = 1;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void init(){
        sContext = getInstrumentation().getTargetContext();
        sUiDevice = UiDevice.getInstance(getInstrumentation());
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
        int randomPos = getRandomRecyclerPosition(R.id.main_posts_list);
        onView(withId(R.id.main_posts_list)).perform(RecyclerViewActions.scrollToPosition(randomPos));
    }

    @Test
    public void clickAtPositionTest() {
        takeScreenshot("clickAtPositionTest start");
        int randomPos = getRandomRecyclerPosition(R.id.main_posts_list);
        onView(withId(R.id.main_posts_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(randomPos, click()));
        Espresso.pressBack();
        takeScreenshot("clickAtPositionTest finish");
    }

    private int getRandomRecyclerPosition(int recyclerId) {
        Random rand = new Random();
        //Get the actual drawn RecyclerView
        RecyclerView recyclerView = (RecyclerView) rule.getActivity().findViewById(recyclerId);
        //If the RecyclerView exists, get the item count from the adapter
        int n = (recyclerView == null) ? FIRST_POSITION : recyclerView.getAdapter().getItemCount();
        //Return a random number from 0 (inclusive) to adapter.itemCount() (exclusive)
        return rand.nextInt(n);
    }

    @Test
    public void getPosts() {
        mDbRef = FirebaseDatabase.getInstance().getReference();
        mDbRef.child(POSTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getChildrenCount();
                List<Post> posts = new ArrayList<>();
                for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                    Post post = dataSnapshot.getValue(Post.class);
                    posts.add(post);

                    onView(withId(R.id.main_posts_list))
                            .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));

                    onView(withId(R.id.post_title)).check(matches(withText(post.getTitle())));
                    onView(withId(R.id.post_body)).check(matches(withText(post.getBody())));
                    onView(withId(R.id.post_author)).check(matches(withText(post.getAuthor())));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
        File file = new File(dir.getAbsolutePath() + "/"+String.format("%02d", mCounter)+"_" + name + ".png");
        sUiDevice.takeScreenshot(file);
        mCounter++;
    }

    @Test
    public void logOutTest() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(sContext.getString(R.string.log_out))).perform(click());
    }
}
