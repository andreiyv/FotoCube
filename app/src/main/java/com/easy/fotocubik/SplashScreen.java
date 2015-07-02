package com.easy.fotocubik;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by voran on 7/1/15.
 */



    /**
     * Created by vamsikrishna on 12-Feb-15.
     */
    public class SplashScreen extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            setContentView(R.layout.splash);

            Thread timerThread = new Thread(){
                public void run(){
                    try{
                        sleep(500);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }finally{
                        Intent i = new Intent(SplashScreen.this,Passwd.class);
                        startActivity(i);
                    }
                }
            };
            timerThread.start();
        }

        @Override
        protected void onPause() {
            // TODO Auto-generated method stub
            super.onPause();
            finish();
        }
}
