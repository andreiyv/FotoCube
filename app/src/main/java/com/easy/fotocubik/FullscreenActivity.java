package com.easy.fotocubik;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Update from Windows

public class FullscreenActivity extends Activity {
 //   public class FullscreenActivity extends    ActionBarActivity {

 //   @Override
 //   protected void onCreate(Bundle savedInstanceState) {
 //       super.onCreate(savedInstanceState);
 //       setContentView(R.layout.activity_fullscreen);
 //   }
 final Context context = this;
 private EditText result;
    private EditText emailEditText;
    private EditText passEditText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    /************
     *  MENU FUNCTIONS
     **********/
	/*
	 * Creates the menu and populates it via xml
	 */

    /*
 * On selection of a menu item
 */

    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 6) {
            return true;
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.view_settings:
                Intent intent = new Intent(FullscreenActivity.this, s1.class);
                mRenderer.s = 1;
                startActivity(intent);





                return true;
            case R.id.settings1:
                mRenderer.slowdown = 500.0f;
                Log.d("SLOWDOWN", "mode=" + mRenderer.slowdown);
            //    requestRender();
                return true;
            case R.id.settings2: 			// Gouraud Shading
                mRenderer.slowdown = 50.0f;
                Log.d("SLOWDOWN", "mode=" + mRenderer.slowdown);
             //   requestRender();
                return true;
            case R.id.settings3: 			// Phong Shading
                mRenderer.slowdown = 1.0f;
                Log.d("SLOWDOWN", "mode=" + mRenderer.slowdown);
            //    requestRender();
                return true;

            default:
                Log.d("default", "");
                return false;
        }
    }
    /**
     * Hold a reference to our GLSurfaceView
     */
    private LessonSixGLSurfaceView mGLSurfaceView;
    private LessonSixRenderer mRenderer;

    private static final int MIN_DIALOG = 1;
    private static final int MAG_DIALOG = 2;

    private int mMinSetting = -1;
    private int mMagSetting = -1;

    private static final String MIN_SETTING = "min_setting";
    private static final String MAG_SETTING = "mag_setting";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String val1="";
        String str1, str2, str3;
        str1="not initialized";
        str2="not initialized";
        str3="not initialized";

        result = (EditText) findViewById(R.id.editTextResult);

        mGLSurfaceView = new LessonSixGLSurfaceView(this);
        setContentView(mGLSurfaceView);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView.setEGLContextClientVersion(2);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);



            // Set the renderer to our demo renderer, defined below.
            mRenderer = new LessonSixRenderer(this);
            mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);
           //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

            Log.d("SLOWDOWN", "mRenderer.s=" + mRenderer.s);
// get
            String s = ((MyApplication) this.getApplication()).getSomeVariable();

            if (s == "settings") {

                str1 = getIntent().getExtras().getString("var1");
                str2 = getIntent().getExtras().getString("var2");
                str3 = getIntent().getExtras().getString("var3");


                mRenderer.slowdown = Float.parseFloat(str1);

                Log.d("SLOWDOWN", "str1=" + str1);
                Log.d("SLOWDOWN", "mRenderer.slowdown=" + mRenderer.slowdown);

            }



        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

    /*    findViewById(R.id.button_set_min_filter).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(MIN_DIALOG);
            }
        });

        findViewById(R.id.button_set_mag_filter).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(MAG_DIALOG);
            }
        });
*/
        // Restore previous settings
        if (savedInstanceState != null) {
            mMinSetting = savedInstanceState.getInt(MIN_SETTING, -1);
            mMagSetting = savedInstanceState.getInt(MAG_SETTING, -1);

            if (mMinSetting != -1) {
                setMinSetting(mMinSetting);
            }
            if (mMagSetting != -1) {
                setMagSetting(mMagSetting);
            }
        }
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity
        // onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity
        // onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(MIN_SETTING, mMinSetting);
        outState.putInt(MAG_SETTING, mMagSetting);
    }

    private void setMinSetting(final int item) {
        mMinSetting = item;

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                final int filter;

                if (item == 0) {
                    filter = GLES20.GL_NEAREST;
                } else if (item == 1) {
                    filter = GLES20.GL_LINEAR;
                } else if (item == 2) {
                    filter = GLES20.GL_NEAREST_MIPMAP_NEAREST;
                } else if (item == 3) {
                    filter = GLES20.GL_NEAREST_MIPMAP_LINEAR;
                } else if (item == 4) {
                    filter = GLES20.GL_LINEAR_MIPMAP_NEAREST;
                } else // if (item == 5)
                {
                    filter = GLES20.GL_LINEAR_MIPMAP_LINEAR;
                }

                mRenderer.setMinFilter(filter);
            }
        });
    }

    private void setMagSetting(final int item) {
        mMagSetting = item;

        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                final int filter;

                if (item == 0) {
                    filter = GLES20.GL_NEAREST;
                } else // if (item == 1)
                {
                    filter = GLES20.GL_LINEAR;
                }

                mRenderer.setMagFilter(filter);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case MIN_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getText(R.string.lesson_six_set_min_filter_message));
                builder.setItems(getResources().getStringArray(R.array.lesson_six_min_filter_types), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        setMinSetting(item);
                    }
                });

                dialog = builder.create();
            }
            break;
            case MAG_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getText(R.string.lesson_six_set_mag_filter_message));
                builder.setItems(getResources().getStringArray(R.array.lesson_six_mag_filter_types), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        setMagSetting(item);
                    }
                });

                dialog = builder.create();
            }
            break;
            default:
                dialog = null;
        }

        return dialog;
    }



}

