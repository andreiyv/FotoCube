package com.easy.fotocubik;

import android.app.Application;

/**
 * Created by voran on 6/30/15.
 */
public class MyApplication extends Application {
    private String someVariable;

    public String getSomeVariable() {
        return someVariable;
    }

    public void setSomeVariable(String someVariable) {
        this.someVariable = someVariable;
    }

}
