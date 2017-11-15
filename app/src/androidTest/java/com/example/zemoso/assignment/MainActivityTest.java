package com.example.zemoso.assignment;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.example.zemoso.assignment.activities.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.example.zemoso.assignment.activities.utils.AssignmnetApplication.TAG;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by zemoso on 13/11/17.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    final Handler handler = new Handler(Looper.getMainLooper());

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

/*
    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityRule.getActivity().getIdlingResource();
        // To prove that the test fails, omit this call:
        Espresso.registerIdlingResources(mIdlingResource);
    }
*/

    @Test
    public void checkIfMainActivityIsLaunched(){
        onView(withId(R.id.app_name)).check(matches(isDisplayed()));
        onView(withId(R.id.camera)).check(matches(isDisplayed()));
        onView(withId(R.id.gallery)).check(matches(isDisplayed()));
    }

    @Test
    public void checkIfCameraFragmentIsLaunched(){
        onView(withId(R.id.camera)).perform(click());
        onView(withId(R.id.textureView)).check(matches((isDisplayed())));
        onView(withId(R.id.chronometer)).check(matches(not(isDisplayed())));
        onView(withId(R.id.record)).check(matches(isDisplayed()));
    }

    @Test
    public void checkIfCameraIsRecording(){
        onView(withId(R.id.camera)).perform(click());
        onView(withId(R.id.record)).perform(click());
        onView(withId(R.id.chronometer)).check(matches(isDisplayed()));
    }

    @Test
    public void checkIfPreviewFragmentIsLaunchedAfterRecording(){
        onView(withId(R.id.camera)).perform(click());
        onView(withId(R.id.record)).perform(click());
        onView(withId(R.id.record)).perform(click());
        onView(withId(R.id.prev_container)).check(matches((isDisplayed())));
    }

    @Test
    public void checkIfOnLongTapToCommentIsWorking(){
        onView(withId(R.id.camera)).perform(click());
        onView(withId(R.id.record)).perform(click());

       final AsyncTask<String, Void, Integer> task = new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Log.e(TAG, "Thread interrupted while testing", ignored);
                }
                return params.length;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                assertEquals(3, (int) integer);
            }
        };
        task.execute("One","two","three");
        onView(withId(R.id.record)).perform(click());
        onView(withId(R.id.exoplayer)).perform(click());
        onView(withId(R.id.exoplayer)).perform(longClick());

        onView(withId(R.id.comment)).check(matches(isDisplayed()));
        onView(withId(R.id.comment)).perform(typeText("Bhupathi"));

      /*  handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("enter","here");
                onView(withId(R.id.record)).perform(click());
                onView(withId(R.id.exoplayer)).perform(longClick());
                onView(withId(R.id.comment)).perform(typeText("Bhupathi"));
            }
        },1000);*/
       /* onView(withId(R.id.record)).perform(click());
        onView(withId(R.id.exoplayer)).perform(longClick());
        onView(withId(R.id.comment)).perform(typeText("Bhupathi"));*/
    }

}
