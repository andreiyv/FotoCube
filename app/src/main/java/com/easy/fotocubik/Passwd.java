package com.easy.fotocubik;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class Passwd extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passwd);
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_s1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void PasswdButton_onClick(View view) {
        ((MyApplication) this.getApplication()).setSomeVariable("settings");

        EditText userEditText1 = (EditText) findViewById(R.id.PasswdText);


        Intent intent = new Intent(Passwd.this, FullscreenActivity.class);

        // в ключ username пихаем текст из первого текстового поля
        intent.putExtra("var1", userEditText1.getText().toString());

        if (isValidPassword(userEditText1.getText().toString())) {
            startActivity(intent);
        } else {
            userEditText1.setError(userEditText1.getText().toString());
        }
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        Log.d ("Password: ", "" + pass);

        if (pass.equals("12345")) {
            return true;
        }
        return false;
    }

}
