package com.easy.fotocubik;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class s1 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s1);
    }

    @Override
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

    public void onClick(View view) {
        ((MyApplication) this.getApplication()).setSomeVariable("settings");

        EditText userEditText1 = (EditText) findViewById(R.id.editText);
        EditText userEditText2 = (EditText) findViewById(R.id.editText2);
        EditText userEditText3 = (EditText) findViewById(R.id.editText3);

        Intent intent = new Intent(s1.this, FullscreenActivity.class);

        // в ключ username пихаем текст из первого текстового поля
        intent.putExtra("var1", userEditText1.getText().toString());
        intent.putExtra("var2", userEditText2.getText().toString());
        intent.putExtra("var3", userEditText3.getText().toString());

        startActivity(intent);
    }

}
