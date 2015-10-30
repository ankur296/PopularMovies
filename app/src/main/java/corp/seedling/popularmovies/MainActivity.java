package corp.seedling.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class MainActivity extends AppCompatActivity implements MainFragment.Callback{
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onItemSelected(Bundle bundle) {
        Log.e(TAG, "********* onItemSelected*********");

        if (findViewById(R.id.fragment_detail) == null) {

            Log.e(TAG, "********* onItemSelected: mtwopane FALSE:launch detail act*********");

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("movie_id", bundle.getInt("movie_id"))
                    .putExtra("movie_plot", bundle.getString("movie_plot"))
                    .putExtra("movie_title", bundle.getString("movie_title"))
                    .putExtra("movie_rel_date", bundle.getString("movie_rel_date"))
                    .putExtra("movie_poster_path", bundle.getString("movie_poster_path"))
                    .putExtra("movie_poster", bundle.getByteArray("movie_poster"))
                    .putExtra("movie_rating", bundle.getString("movie_rating"))
                    .putExtra("movie_trailer", bundle.getString("movie_trailer"))
                    .putExtra("movie_review", bundle.getString("movie_review"))
                    .putExtra("is_fav", bundle.getBoolean("is_fav"));
            startActivity(intent);

        } else {
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_detail);

            Log.e(TAG, "********* onItemSelected: mtwopane TRUE:update detail frag*********");
            detailFragment.updateContent(bundle);
        }

    }


    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
