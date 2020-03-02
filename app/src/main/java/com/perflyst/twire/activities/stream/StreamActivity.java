package com.perflyst.twire.activities.stream;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.perflyst.twire.R;
import com.perflyst.twire.activities.ThemeActivity;
import com.perflyst.twire.fragments.ChatFragment;
import com.perflyst.twire.fragments.StreamFragment;
import com.perflyst.twire.service.Settings;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class StreamActivity extends ThemeActivity implements SensorEventListener, StreamFragment.StreamFragmentListener {
    private static final int SENSOR_DELAY = 500 * 1000; // 500ms
    private static final int FROM_RADS_TO_DEGS = -57;
    public StreamFragment mStreamFragment;
    public ChatFragment mChatFragment;
    private String LOG_TAG = getClass().getSimpleName();
    private Sensor mRotationSensor;
    private Settings settings;

    protected abstract int getLayoutResource();

    protected abstract int getVideoContainerResource();

    protected abstract Bundle getStreamArguments();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setEnterTransition(constructTransitions());
                getWindow().setReturnTransition(constructTransitions());
            }

            // If the Fragment is non-null, then it is currently being
            // retained across a configuration change.
            if (mStreamFragment == null) {
                mStreamFragment = StreamFragment.newInstance(getStreamArguments());
                fm.beginTransaction().replace(getVideoContainerResource(), mStreamFragment, getString(R.string.stream_fragment_tag)).commit();
            }

            if (mChatFragment == null) {
                mChatFragment = ChatFragment.getInstance(getStreamArguments());
                fm.beginTransaction().replace(R.id.chat_fragment, mChatFragment).commit();
            }
        }

        try {
            SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        settings = new Settings(this);
        updateOrientation();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing :)
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor == mRotationSensor && getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                if (event.values.length > 4) {
                    float[] truncatedRotationVector = new float[4];
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                    update(truncatedRotationVector);
                } else {
                    update(event.values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void update(float[] vectors) {
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;

        float[] rotationMatrix = new float[9];
        float[] adjustedRotationMatrix = new float[9];
        float[] orientation = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        float roll = orientation[2] * FROM_RADS_TO_DEGS;

        if (roll > -45 && roll < 45) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            Log.d(LOG_TAG, "Requesting undefined");
        }
        Log.d(LOG_TAG, "Roll: " + roll);
    }

    protected void resetStream() {
        FragmentManager fm = getSupportFragmentManager();
        mStreamFragment = StreamFragment.newInstance(getStreamArguments());
        fm.beginTransaction().replace(getVideoContainerResource(), mStreamFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            Log.d(LOG_TAG, "Orientations is reverse portrait");
        }

        Log.d(LOG_TAG, "Current orientation: " + getResources().getConfiguration().orientation);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mChatFragment == null || (mChatFragment.notifyBackPressed())) {
            super.onBackPressed();
        }

        // Eww >(
        if (mStreamFragment != null) {
            if (mStreamFragment.isFullscreen) {
                mStreamFragment.toggleFullscreen();
            } else if (mStreamFragment.chatOnlyViewVisible) {
                this.finish();
                this.overrideTransition();
            } else {
                super.onBackPressed();
                try {
                    mStreamFragment.backPressed();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                this.overrideTransition();
            }
        } else {
            super.onBackPressed();
            this.overrideTransition();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private TransitionSet constructTransitions() {
        int[] slideTargets = {R.id.ChatRecyclerView, R.id.chat_input, R.id.chat_input_divider};

        Transition slideTransition = new Slide(Gravity.BOTTOM);
        Transition fadeTransition = new Fade();

        for (int slideTarget : slideTargets) {
            slideTransition.addTarget(slideTarget);
            fadeTransition.excludeTarget(slideTarget, true);
        }

        TransitionSet set = new TransitionSet();
        set.addTransition(slideTransition);
        set.addTransition(fadeTransition);
        return set;
    }

    private void overrideTransition() {
        this.overridePendingTransition(R.anim.fade_in_semi_anim, R.anim.slide_out_bottom_anim);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mStreamFragment == null) {
            return;
        }
        mStreamFragment.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// Call the super method as we also want the user to go all the way back to last mActivity if the user is in full screen mode
            if (mStreamFragment != null) {
                if (!mStreamFragment.isVideoInterfaceShowing()) {
                    return false;
                }

                if (mStreamFragment.chatOnlyViewVisible) {
                    finish();
                } else {
                    super.onBackPressed();
                    mStreamFragment.backPressed();
                }
                overrideTransition();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof StreamFragment) {
            StreamFragment streamFragment = (StreamFragment) fragment;
            streamFragment.streamFragmentCallback = this;
        }

        if (mChatFragment == null && fragment instanceof ChatFragment)
            mChatFragment = (ChatFragment) fragment;

        if (mStreamFragment == null && fragment instanceof StreamFragment)
            mStreamFragment = (StreamFragment) fragment;
    }

    @Override
    public void onSeek() {
        mChatFragment.clearMessages();
    }

    @Override
    public void refreshLayout() {
        updateOrientation();
    }

    public View getMainContentLayout() {
        return findViewById(R.id.main_content);
    }

    void updateOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        View chat = findViewById(R.id.chat_fragment);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.chat_landscape_fragment).getLayoutParams();
            lp.width = (int) (StreamFragment.getScreenRect(this).height() * (settings.getChatLandscapeWidth() / 100.0));
            Log.d(LOG_TAG, "TARGET WIDTH: " + lp.width);
            chat.setLayoutParams(lp);
        } else {
            chat.setLayoutParams(findViewById(R.id.chat_placement_wrapper).getLayoutParams());
        }

        ViewGroup.LayoutParams layoutParams = findViewById(getVideoContainerResource()).getLayoutParams();
        layoutParams.height = orientation == Configuration.ORIENTATION_LANDSCAPE ? MATCH_PARENT : WRAP_CONTENT;
    }
}
