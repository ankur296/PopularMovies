package corp.seedling.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import corp.seedling.popularmovies.data.MoviesContract;
import corp.seedling.popularmovies.data.MoviesContract.MovieEntry;


public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();
    private ImageView mPosterView;
    private TextView mDateView;
    private TextView mPlotView;
    private TextView mTitleView;
    private TextView mRatingView;
    private Button mFavButton;
    private final static String posterBaseURL = "http://image.tmdb.org/t/p/w154/";
    private final static String posterBaseURLFav = "http://image.tmdb.org/t/p/w185/";
    private String mDate;
    private String mPlot;
    private String mTitle;
    private String mRating;
    private int mMovieId;
    String mPosterURL;
    private boolean isFavorite = false;
    private FetchTrailerInfoTask mFetchTrailerInfoTask ;
    private FetchReviewInfoTask mFetchReviewInfoTask ;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntentRxd = null;
    Bundle rxdBundle;

    public  DetailFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "oncreate enter");
    }

    void updateContent(Bundle bundle){
        Log.e(TAG, "updateContent enter");
        rxdBundle = bundle;

        //saving movie_id
        mMovieId = rxdBundle.getInt("movie_id", 0);

        //saving and setting date
        mDate = rxdBundle.getString("movie_rel_date");

        if (mDate.equals("null")) {
            mDateView.setText("Release Date Unavailable");
        }
        else {
            String[] date = (mDate).split("-");
            mDateView.setText(date[0]);
            mDate = date[0];
        }

        //saving and setting Title
        mTitle = rxdBundle.getString("movie_title");
        mTitleView.setText(mTitle);

        //saving and setting Plot
        mPlot = rxdBundle.getString("movie_plot");

        if (TextUtils.isEmpty(mPlot) || mPlot.equals("null"))//tmdb sends empty plot as string null
            mPlotView.setText("No Plot Summary Available");
        else
            mPlotView.setText(mPlot);

        //saving and setting Rating
        mRating = rxdBundle.getString("movie_rating") ;
        mRatingView.setText(mRating+ "/10");

        if(rxdBundle.getBoolean("is_fav", false)) {//if coming frm fav screen
            //handle poster
            addPosterOffline(rxdBundle.getByteArray("movie_poster"));
        }else {
            addPosterOnline(rxdBundle.getString("movie_poster_path"));

            mFetchTrailerInfoTask = new FetchTrailerInfoTask(rxdBundle.getInt("movie_id", -1));
            mFetchTrailerInfoTask.execute();

            mFetchReviewInfoTask = new FetchReviewInfoTask(rxdBundle.getInt("movie_id", -1));
            mFetchReviewInfoTask.execute();
        }

        if (checkIsFavorite(mMovieId)){
            isFavorite = true;
            mFavButton.setText(getResources().getString(R.string.mark_un_fav));
        }else{
            isFavorite = false;
            mFavButton.setText(getResources().getString(R.string.mark_fav));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.i(TAG, "oncreateview enter");
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        findViewsByIDs(rootView);

        if (savedInstanceState != null)
            updateContent(savedInstanceState);

        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onclick: isFavorite = " + isFavorite);
                if (isFavorite) {
                    //remove from fav
                    removeFromFav(mMovieId);
                    mFavButton.setText(getResources().getString(R.string.mark_fav));
                    isFavorite = false;
                    Log.i(TAG, "onclick: Removed fr FAV");

                } else {
                    if (!isNetworkAvailable())
                        Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
                    else {
                        //add to fav
                        //start an async task which will download the img and save it in DB
                        new DownloadAndSaveImgTask().execute();
                        mFavButton.setEnabled(false);
                        mFavButton.setText(getResources().getString(R.string.mark_un_fav));
                        isFavorite = true;
                        Log.i(TAG, "onclick: Added to FAV");
                    }
                }
            }
        });
        return rootView;
    }

    private boolean checkIsFavorite(int id) {
        Cursor cursor = getActivity().getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.COLUMN_MOVIE_ID},
                MovieEntry.COLUMN_MOVIE_ID + "=" + id,
                null,
                null
        );

        Log.i(TAG, "CURSOR:" + DatabaseUtils.dumpCursorToString(cursor));
        if (cursor.moveToFirst())
            return true;
        else
            return false;
    }

    private void removeFromFav(int id){
        getActivity().getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_MOVIE_ID + "=" + id,
                null
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "onSaveInstanceState Enter");
        super.onSaveInstanceState(outState);
        if (rxdBundle != null) {
            if (rxdBundle.getBoolean("is_fav", false))
                outState.putBoolean("is_fav", true);
            else
                outState.putBoolean("is_fav", false);

            outState.putInt("movie_id", rxdBundle.getInt("movie_id", -1));
            outState.putString("movie_title", rxdBundle.getString("movie_title"));
            outState.putString("movie_plot", rxdBundle.getString("movie_plot"));
            outState.putString("movie_rel_date", rxdBundle.getString("movie_rel_date"));
            outState.putString("movie_rating", rxdBundle.getString("movie_rating"));
            outState.putByteArray("movie_poster", rxdBundle.getByteArray("movie_poster"));
            outState.putString("movie_poster_path", rxdBundle.getString("movie_poster_path"));
        }
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated enter");
        if(getActivity().getIntent().getBooleanExtra("is_fav", false)) {//if coming frm fav screen
            //handle trailers
            addTrailersOffline(getActivity().getIntent());
            addReviewsOffline(getActivity().getIntent());
        }
    }

    private void addPosterOffline( byte[] posterBytes){
        Bitmap bitmap;

        if (posterBytes == null)
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.notavailable);
        else
            bitmap = Utility.convertByteArrayToBitmap(posterBytes);

        mPosterView.setScaleType(ImageView.ScaleType.FIT_XY);
        mPosterView.setAdjustViewBounds(true);
        mPosterView.setImageBitmap(bitmap);
    }

    private void addPosterOnline(String posterURL){
        final String mPosterFullURL;
        mPosterURL = posterURL;
        mPosterFullURL = posterBaseURL + mPosterURL;
        Glide.with(this)
                .load(mPosterFullURL)
                .fitCenter()
                .error(R.drawable.notavailable)
                .into(mPosterView);

    }

    private void addTrailersOffline(Intent intent){
        Log.i(TAG, "addtrailer enter");
        LinearLayout ll_detail = (LinearLayout)getActivity().findViewById(R.id.ll_trailer_main);
        final String[] trailers = Utility.convertStringToArray(intent.getStringExtra("movie_trailer"));

        int layout_child_count = ll_detail.getChildCount();

        //Remove any tetviews that were added dynamically earlier
        for(int i = 0 ; i < layout_child_count ; i++){

            if (ll_detail.getChildAt(i) != null) {

                if (ll_detail.getChildAt(i).getTag() != null) {

                    if (ll_detail.getChildAt(i).getTag().equals("Dynamically Added View")) {
                        ll_detail.removeViewAt(i);
                        i--;
                    }
                }
            }
        }
        //Add textview dynamically based on number of reviews available

        if (trailers == null || trailers.length == 0) {
            setEmptyTrailersFlag(true);
            InfoView trailerView = new InfoView(getActivity());
            trailerView.setText("No Trailers Available");
            trailerView.setTag("Dynamically Added View");
            ll_detail.addView(trailerView);

        } else {

            for (int i = 0; i < trailers.length; i++) {
                TrailerView trailerView = new TrailerView(getActivity());
                trailerView.setText("Trailer" + (i + 1));
                trailerView.setTag("Dynamically Added View");
                ll_detail.addView(trailerView);

                if (i != (trailers.length - 1)) {
                    ll_detail.addView(new SpaceView(getActivity(), "Dynamically Added View"));
                }

                final int finalI = i;
                trailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(trailers[finalI])
                        ));
                    }
                });
            }

            setEmptyTrailersFlag(false);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Trailer URL");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, trailers[0]);
            sharingIntent = Intent.createChooser(sharingIntent, "Share Trailer URL");
            Log.i(TAG, "CALL setShareIntent");
            setShareIntent(sharingIntent);
        }
    }

    private void addReviewsOffline(Intent intent){
        final String[] reviews = Utility.convertStringToArray(intent.getStringExtra("movie_review"));
        LinearLayout ll_detail = (LinearLayout) getActivity().findViewById(R.id.ll_reviews_main);

        int layout_child_count = ll_detail.getChildCount();

        //Remove any tetviews that were added dynamically earlier
        for(int i = 0 ; i < layout_child_count ; i++){

            if (ll_detail.getChildAt(i) != null) {

                if (ll_detail.getChildAt(i).getTag() != null) {

                    if (ll_detail.getChildAt(i).getTag().equals("Dynamically Added View")) {
                        ll_detail.removeViewAt(i);
                        i--;
                    }
                }
            }
        }
        //Add textview dynamically based on number of reviews available

        if (reviews == null || reviews.length == 0) {
            InfoView reviewView = new InfoView(getActivity());
            reviewView.setText("No Reviews Available");
            reviewView.setTag("Dynamically Added View");
            ll_detail.addView(reviewView);

        } else {

            for (int i = 0; i < reviews.length; i++) {
                ReviewView reviewView = new ReviewView(getActivity());
                reviewView.setText("Read Review" + (i + 1));
                reviewView.setTag("Dynamically Added View");
                ll_detail.addView(reviewView);

                if (i != (reviews.length - 1)) {
                    ll_detail.addView(new SpaceView(getActivity() , "Dynamically Added View"));
                }
                final int finalI = i;
                reviewView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(reviews[finalI])
                        ));
                    }
                });
            }
        }
    }

    private void findViewsByIDs(View rootView) {
        mPosterView = (ImageView) rootView.findViewById(R.id.iv_poster);
        mDateView = (TextView) rootView.findViewById(R.id.tv_rel_date);
        mTitleView = (TextView) rootView.findViewById(R.id.tv_movie_name);
        mPlotView = (TextView) rootView.findViewById(R.id.tv_plot);
        mRatingView = (TextView) rootView.findViewById(R.id.tv_rating);
        mFavButton = (Button) rootView.findViewById(R.id.button_fav);
    }


    class DownloadAndSaveImgTask extends AsyncTask<Void, Void, Void> {

        private final String TAG = DownloadAndSaveImgTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {

            byte[] data = null;
            try {

                URL url = new URL(posterBaseURLFav + mPosterURL);
                //open the connection
                URLConnection ucon = url.openConnection();
                //buffer the download
                InputStream is = ucon.getInputStream();
                //store the data as a ByteArray
                data = read(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();
            values.put(MovieEntry.COLUMN_MOVIE_ID, mMovieId);
            values.put(MovieEntry.COLUMN_DATE, mDate);
            values.put(MovieEntry.COLUMN_PLOT, mPlot);
            values.put(MovieEntry.COLUMN_RATING, mRating);
            values.put(MovieEntry.COLUMN_TITLE, mTitle);
            values.put(MovieEntry.COLUMN_REVIEW,
                    Utility.convertArrayToString(mFetchReviewInfoTask.reviewPaths));
            values.put(MovieEntry.COLUMN_TRAILER,
                    Utility.convertArrayToString(mFetchTrailerInfoTask.trailerPaths));
            values.put(MovieEntry.COLUMN_POSTER, data);

            getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, values);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor cursor = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
                    null, null, null, null);
            Log.i(TAG, "Cursor after insertion of FAV: "+ DatabaseUtils.dumpCursorToString(cursor));
            mFavButton.setEnabled(true);
        }

        public byte[] read(InputStream is) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            try {
                // Read buffer, to read a big chunk at a time.
                byte[] buf = new byte[2048];
                int len;
                // Read until -1 is returned, i.e. stream ended.
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
            } catch (IOException e) {
                Log.e("Downloader", "File could not be downloaded", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("Downloader", "Input stream could not be closed", e);
                }
            }
            return baos.toByteArray();
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "ondetach");
        if (mFetchReviewInfoTask!=null && !mFetchReviewInfoTask.getStatus().equals(AsyncTask.Status.FINISHED))
            mFetchReviewInfoTask.cancel(true);

        if (mFetchTrailerInfoTask!=null && !mFetchTrailerInfoTask.getStatus().equals(AsyncTask.Status.FINISHED))
            mFetchTrailerInfoTask.cancel(true);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider!=null)
        mShareActionProvider.setShareIntent(shareIntentRxd);
    }

    // Call to update the share intent
    public void setShareIntent(Intent shareIntent) {
        Log.i(TAG, "setShareIntent enter");
        shareIntentRxd = shareIntent;
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void setEmptyTrailersFlag(boolean trailersEmpty){

        if (trailersEmpty == true && mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(null);
        }
    }
    //////////////TRAILER TASK //////////////////
    class FetchTrailerInfoTask extends AsyncTask<String, Void, String[]> {

        private final String TAG = FetchTrailerInfoTask.class.getSimpleName();
        private int movieId;
        String[] trailerPaths;

        public FetchTrailerInfoTask(int id) {
            movieId = id;
        }

        @Override
        protected String[] doInBackground(String... params) {
            Log.i(TAG, "doInBackground enter");
            HttpURLConnection urlConnection = null;
            String responseJson = null;
            BufferedReader bufferedReader = null;
            final String BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String MOVIE_ID_PARAM = String.valueOf(movieId);
            final String KEY_PARAM = "api_key";
            final String KEY = "enter key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(MOVIE_ID_PARAM)
                    .appendPath("videos")
                    .appendQueryParameter(KEY_PARAM, KEY)
                    .build();

            try {
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null)
                    return null;

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null)
                    stringBuffer.append(line + "\n");

                if (stringBuffer == null)
                    return null;

                responseJson = stringBuffer.toString();
                Log.i(TAG, "RESPONSE: " + responseJson);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Incorrect URL");
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while opening the connection");
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                return Parser.getTrailerDataFromJson(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(final String[] trailers) {
            Log.i(TAG, "onPostExecute enter");
            LinearLayout ll_detail = (LinearLayout) getActivity().findViewById(R.id.ll_trailer_main);
            trailerPaths = null;

            int layout_child_count = ll_detail.getChildCount();

            //Remove any tetviews that were added dynamically earlier
            for(int i = 0 ; i < layout_child_count ; i++){

                if (ll_detail.getChildAt(i) != null) {

                    if (ll_detail.getChildAt(i).getTag() != null) {

                        if (ll_detail.getChildAt(i).getTag().equals("Dynamically Added View")) {
                            ll_detail.removeViewAt(i);
                            i--;
                        }
                    }
                }
            }

            if (trailers != null) {
                trailerPaths = trailers.clone();
                //Add textview dynamically based on number of trailers available
                if (trailers.length == 0) {
                    setEmptyTrailersFlag(true);
                    InfoView trailerView = new InfoView(getActivity());
                    trailerView.setText("No Trailers Available");
                    trailerView.setTag("Dynamically Added View");
                    ll_detail.addView(trailerView);
                    Log.e(TAG, "Added No Trailers Available view ");

                } else {

                    for (int i = 0; i < trailers.length; i++) {
                        TrailerView trailerView = new TrailerView(getActivity());
                        trailerView.setText("Trailer" + (i + 1));
                        trailerView.setTag("Dynamically Added View");
                        ll_detail.addView(trailerView);
                        Log.e(TAG, "Added Trailers view ");

                        if (i != (trailers.length - 1)) {
                            ll_detail.addView(new SpaceView(getActivity(), "Dynamically Added View"));
                            Log.e(TAG, "Added space view ");

                        }

                        final int finalI = i;
                        trailerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(trailers[finalI])
                                ));
                            }
                        });
                    }

                    setEmptyTrailersFlag(false);
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Trailer URL");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, trailers[0]);
                    sharingIntent = Intent.createChooser(sharingIntent, "Share Trailer URL");
                    Log.i(TAG, "setShareIntent CALL");
                    setShareIntent(sharingIntent);
                }
            }
            else{
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /////////////////FETCH REVIEW TASK //////////////////
    class FetchReviewInfoTask extends AsyncTask<Void, Void, String[]> {

        private final String TAG = FetchReviewInfoTask.class.getSimpleName();
        private int movieId;
        public String[] reviewPaths;

        public FetchReviewInfoTask( int id){
            movieId = id;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            Log.i(TAG, "doInBackground enter");
            HttpURLConnection urlConnection = null;
            String responseJson = null;
            BufferedReader bufferedReader = null;
            final String BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String MOVIE_ID_PARAM = String.valueOf(movieId);
            final String KEY_PARAM = "api_key";
            final String KEY = "196527b28198a82e77196ba38b0d32fb";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(MOVIE_ID_PARAM)
                    .appendPath("reviews")
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
                return Parser.getReviewDataFromJson(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(final String[] strings) {
            Log.i(TAG, "onPostExecute enter");
            reviewPaths = null;


            LinearLayout ll_detail = (LinearLayout) getActivity().findViewById(R.id.ll_reviews_main);

            int layout_child_count = ll_detail.getChildCount();

            //Remove any tetviews that were added dynamically earlier
            for(int i = 0 ; i < layout_child_count ; i++){

                if (ll_detail.getChildAt(i) != null) {

                    if (ll_detail.getChildAt(i).getTag() != null) {

                        if (ll_detail.getChildAt(i).getTag().equals("Dynamically Added View")) {
                            ll_detail.removeViewAt(i);
                            i--;
                        }
                    }
                }
            }

            if (strings != null){
                reviewPaths = strings.clone();
                //Add textview dynamically based on number of reviews available
                if (strings.length == 0) {
                    InfoView reviewView = new InfoView(getActivity());
                    reviewView.setText("No Reviews Available");
                    reviewView.setTag("Dynamically Added View");
                    ll_detail.addView(reviewView);

                } else {

                    for (int i = 0; i < strings.length; i++) {
                        ReviewView reviewView = new ReviewView(getActivity());
                        reviewView.setText("Read Review" + (i + 1));
                        reviewView.setTag("Dynamically Added View");
                        ll_detail.addView(reviewView);

                        if (i != (strings.length - 1)) {
                            ll_detail.addView(new SpaceView(getActivity(), "Dynamically Added View"));
                        }
                        final int finalI = i;
                        reviewView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(strings[finalI])
                                ));

                            }
                        });
                    }
                }
            }
            else{
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.e(TAG, "onattach");
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onviewcreated");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.e(TAG, "onViewStateRestored");
    }
}
