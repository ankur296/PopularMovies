package corp.seedling.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import corp.seedling.popularmovies.data.MoviesContract.MovieEntry;


public class MainFragment extends Fragment implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>{

    private boolean mWaitingForResult = false;
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String URL_SORT_BY_POPULARITY = "popularity.desc";
    private static final String URL_SORT_BY_RATING = "vote_average.desc";
    private GridView mGridView;
    private TextView mEmptyView;
    private ImageAdapter mImageAdapter;
    private boolean mFavoriteMode = false;
    OfflineImageAdapter offlineImageAdapter;
    private int mActivatedPosition = GridView.INVALID_POSITION;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.spinner_action_list,
                R.layout.spinner_dropdown_item
        );

        LayoutInflater inflater = (LayoutInflater) ((AppCompatActivity)getActivity())
                .getSupportActionBar().getThemedContext()
                .getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        final View spinnerView = inflater.inflate(R.layout.layout_spinner, null);
        Spinner spinner = (Spinner) spinnerView.findViewById(R.id.my_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE );

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.RIGHT; // set your layout's gravity to 'right'
        ((AppCompatActivity)getActivity()).getSupportActionBar().setCustomView(spinnerView, layoutParams);

        offlineImageAdapter = new OfflineImageAdapter(getActivity(), null, 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "********* onViewCreated ENTER*********");

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {

            int lastSavedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
            Log.e(TAG, "********* onViewCreated: activate position=*********" + lastSavedPosition);
            setActivatedPosition(lastSavedPosition);
            mGridView.smoothScrollToPosition(lastSavedPosition);

            mImageAdapter.setPosterURLs(savedInstanceState.getStringArray("poster_urls"));
            mImageAdapter.notifyDataSetChanged();
        }else{
            setActivatedPosition(0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "********* onSaveInstanceState ENTER*********");
        super.onSaveInstanceState(outState);

        if (mActivatedPosition != GridView.INVALID_POSITION) {
            Log.e(TAG, "********* onSaveInstanceState ENTER : save pos = *********" + mActivatedPosition);
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
            outState.putStringArray("poster_urls", mImageAdapter.getPosterURLs());
        }
    }

    void setActivatedPosition(int position) {
        Log.e(TAG, "********* setActivatedPosition ENTER : pos = *********" + position);

        if (position == ListView.INVALID_POSITION) {
            mGridView.setItemChecked(mActivatedPosition, false);
        } else {
            mGridView.setItemChecked(position, true);
            mActivatedPosition = position;
        }
        mWaitingForResult = true;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mImageAdapter = new ImageAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main,container, false);

        mGridView = (GridView)rootView.findViewById(R.id.main_grid);
        mEmptyView = (TextView)rootView.findViewById(R.id.empty_tv);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            mGridView.setNumColumns(3);

        mGridView.setEmptyView(mEmptyView);

        mGridView.setAdapter(mImageAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {

                    view.setSelected(true);
                    mActivatedPosition = position;
                    mGridView.setItemChecked(mActivatedPosition, true);

                    if (mFavoriteMode) {
                        launchFavDetailedAct(position);
                    } else {
                        if (!isNetworkAvailable())
                            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
                        else {
                            Bundle bundle = new Bundle();
                            bundle.putInt("movie_id", Parser.getMovieId(position));
                            bundle.putString("movie_plot", Parser.getMoviePlot(position));
                            bundle.putString("movie_title", Parser.getMovieOriginalTitle(position));
                            bundle.putString("movie_rel_date", Parser.getMovieReleaseDate(position));
                            bundle.putString("movie_poster_path", Parser.getMoviePosterPath(position));
                            bundle.putString("movie_rating", Parser.getMovieUserRating(position));
                            bundle.putBoolean("is_fav", mFavoriteMode);
                            ((Callback) getActivity()).onItemSelected(bundle);
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error in parsing");
                    e.printStackTrace();
                }
            }
        });
        return rootView;
    }

    private void launchFavDetailedAct(int index){
        Log.i(TAG, "Fetch movie at position " + index);
        Cursor cursor = (Cursor)offlineImageAdapter.getItem(index);
        Log.i(TAG, "launchFavDetailedAct-CURSOR:" + DatabaseUtils.dumpCursorToString(cursor));

        Bundle bundle = new Bundle();
        bundle.putInt("movie_id", cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID)));
        bundle.putString("movie_plot", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_PLOT)));
        bundle.putString("movie_title", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
        bundle.putString("movie_rel_date", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DATE)));
        bundle.putByteArray("movie_poster", cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));
        bundle.putString("movie_rating", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RATING)));
        bundle.putString("movie_trailer", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TRAILER)));
        bundle.putString("movie_review", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_REVIEW)));
        bundle.putBoolean("is_fav", mFavoriteMode);

        ((Callback) getActivity()).onItemSelected(bundle);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position){
            case 0:
                Log.e(TAG, "onItemSelected : By Popularity");
                mFavoriteMode = false;
                if (isNetworkAvailable()) {
                    mGridView.setAdapter(mImageAdapter);
                    new FetchMovieInfoTask(getActivity(), mImageAdapter).execute(URL_SORT_BY_POPULARITY);
                    if (getActivity().findViewById(R.id.fragment_detail) != null)
                        setActivatedPosition(0);
                }else{
                    mEmptyView.setText(getResources().getString(R.string.no_nwk));
                }
                break;

            case 1:
                Log.e(TAG, "onItemSelected : By Rating");
                mFavoriteMode = false;
                if (isNetworkAvailable()) {
                    mGridView.setAdapter(mImageAdapter);
                    new FetchMovieInfoTask(getActivity() , mImageAdapter).execute(URL_SORT_BY_RATING);

                    if (getActivity().findViewById(R.id.fragment_detail) != null)
                        setActivatedPosition(0);
                }else{
                    mEmptyView.setText(getResources().getString(R.string.no_nwk));
                }
                break;

            case 2:
                Log.e(TAG, "onItemSelected : By Fav");
                Log.e(TAG, "loader..init");
                getActivity().getSupportLoaderManager().initLoader(1, null, this);
                mFavoriteMode = true;
                mGridView.setAdapter(offlineImageAdapter);
//                if (getActivity().findViewById(R.id.fragment_detail) != null)
//                    setActivatedPosition(0);
                break;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "loader..create");
        return new CursorLoader(getActivity(),MovieEntry.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e(TAG,"loader onLoadFinished cursor size = " +cursor.getCount());
        offlineImageAdapter.swapCursor(cursor);

//        if (cursor.moveToFirst() && mWaitingForResult && getActivity().findViewById(R.id.fragment_detail) != null) {
//
//            Bundle bundle = new Bundle();
//            bundle.putInt("movie_id", cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID)));
//            bundle.putString("movie_plot", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_PLOT)));
//            bundle.putString("movie_title", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
//            bundle.putString("movie_rel_date", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DATE)));
//            bundle.putByteArray("movie_poster", cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));
//            bundle.putString("movie_rating", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RATING)));
//            bundle.putString("movie_trailer", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TRAILER)));
//            bundle.putString("movie_review", cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_REVIEW)));
//            bundle.putBoolean("is_fav", mFavoriteMode);
//            ((Callback) getActivity()).onItemSelected(bundle);
//            mWaitingForResult = false;
//            Log.e(TAG,"loader onLoadFinished:since waitingForResult load 1st fav ");
//
//        }

        if (mFavoriteMode && offlineImageAdapter.getCount() == 0) {
            mEmptyView.setText(getResources().getString(R.string.no_fav));
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        offlineImageAdapter.swapCursor(null);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "onNothingSelected");
    }

    public interface Callback {
        void onItemSelected(Bundle bundle);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.e(TAG, "onattach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "ondetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ondestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "ondestroyview");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onstop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onpause");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onactcreated");
        if (getActivity().findViewById(R.id.fragment_detail) != null) {
            Log.e(TAG, "*********set choice mode");
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            mGridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.e(TAG, "onViewStateRestored");
    }

    ///////////////////////// FETCH MOVIE INFO task ///////////////////
    class FetchMovieInfoTask extends AsyncTask<String, Void, String[]> {

        private final String TAG = FetchMovieInfoTask.class.getSimpleName();
        private ImageAdapter mImageAdapter;
        private Context mContext;


        public FetchMovieInfoTask(Context context , ImageAdapter imageAdapter){
            mContext = context;
            mImageAdapter = imageAdapter;
        }

        @Override
        protected String[] doInBackground(String... params) {
            Log.i(TAG, "doinbg");
            HttpURLConnection urlConnection = null;
            String responseJson = null;
            BufferedReader bufferedReader = null;
            final String BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";
            final String KEY = "enter key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, params[0])
                    .appendQueryParameter(KEY_PARAM, KEY)
                    .build();

            try{
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null)
                    return null;

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while( (line = bufferedReader.readLine()) != null)
                    stringBuffer.append(line + "\n");

                if (stringBuffer ==null)
                    return null;

                responseJson = stringBuffer.toString();
                Log.i(TAG, "RESPONSE: " + responseJson);
            }

            catch (MalformedURLException e) {
                Log.e(TAG, "Incorrect URL");
                e.printStackTrace();
                return null;
            }

            catch (IOException e) {
                Log.e(TAG, "Error occurred while opening the connection");
                e.printStackTrace();
                return null;
            }

            finally {
                if (urlConnection !=null)
                    urlConnection.disconnect();

                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                return Parser.getMovieDataFromJson(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String[] strings) {
            Log.i(TAG, "onPostExecute");
            if (strings != null && mImageAdapter != null){
                mImageAdapter.clearData();
                mImageAdapter.setPosterURLs(strings);
                mImageAdapter.notifyDataSetChanged();
                if (isAdded() && mWaitingForResult && getActivity().findViewById(R.id.fragment_detail) != null) {

                    Bundle bundle = new Bundle();
                    try {
                        bundle.putInt("movie_id", Parser.getMovieId(0));

                        bundle.putString("movie_plot", Parser.getMoviePlot(0));
                        bundle.putString("movie_title", Parser.getMovieOriginalTitle(0));
                        bundle.putString("movie_rel_date", Parser.getMovieReleaseDate(0));
                        bundle.putString("movie_poster_path", Parser.getMoviePosterPath(0));
                        bundle.putString("movie_rating", Parser.getMovieUserRating(0));
                        bundle.putBoolean("is_fav", mFavoriteMode);
                        ((Callback) getActivity()).onItemSelected(bundle);
                        mWaitingForResult = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
