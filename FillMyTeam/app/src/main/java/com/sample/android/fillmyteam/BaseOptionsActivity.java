package com.sample.android.fillmyteam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.sample.android.fillmyteam.util.Constants;

/**
 * Created by dgnc on 8/15/2016.
 */
public class BaseOptionsActivity extends AppCompatActivity {


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
     /* if (id == R.id.share_action) {
            return true;
        }*/
        if(id==R.id.logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_USER_LOGGED_IN, false);
        editor.putString(Constants.EMAIL, "");
        editor.putString(Constants.USER_INFO, "");
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
        intent.putExtra(Constants.LOGOUT, true);
        startActivity(intent);
    }

}
