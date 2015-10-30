package corp.seedling.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Log.i(TAG, "oncreate enter");

        if (findViewById(R.id.fragment_detail) == null
                &&
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {
            Log.e(TAG, "oncreate detail act: fetch data");
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_detail);
            detailFragment.updateContent(getIntent().getExtras());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop enter");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onStart enter");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy enter");
    }
}