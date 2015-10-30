package corp.seedling.popularmovies;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ankur Nigam on 05-08-2015.
 */
public class Parser {

    private final static String posterBaseURL = "http://image.tmdb.org/t/p/w185/";
    final static String VIDEO_BASE_URL = "http://www.youtube.com/watch?v=";
    //json field names
    final static String MOVIE_ID = "id";
    final static String MOVIE_POSTER_PATH = "poster_path";
    final static String MOVIE_REVIEW = "url";
    final static String MOVIE_TRAILERS = "key";
    final static String MOVIE_ORIGINAL_TITLE = "original_title";
    final static String MOVIE_USER_RATING = "vote_average";
    final static String MOVIE_PLOT = "overview";
    final static String MOVIE_RELEASE_DATE = "release_date";
    final static String MOVIE_RESULTS_ARRAY = "results";
    private static final String TAG = Parser.class.getSimpleName();
    private static String jsonRspStrMain;
    private static String jsonRspStrTrailer;

    public static String[] getMovieDataFromJson(String jsonStr) throws JSONException {
        jsonRspStrMain = jsonStr;
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        String[] posterPaths = new String[respJsonArray.length()];

        for(int i = 0 ; i < respJsonArray.length() ; i++){
            JSONObject movieJsonObject = respJsonArray.getJSONObject(i);
            posterPaths[i] = posterBaseURL + movieJsonObject.getString(MOVIE_POSTER_PATH);
        }

        return posterPaths;
    }

    public static String[] getTrailerDataFromJson(String jsonStr) throws JSONException {

        jsonRspStrTrailer = jsonStr;
        JSONObject respJsonObject = new JSONObject(jsonRspStrTrailer);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        String[] data = new String[respJsonArray.length()];

        for(int i = 0 ; i < respJsonArray.length() ; i++){
            JSONObject movieJsonObject = respJsonArray.getJSONObject(i);
                data[i] = VIDEO_BASE_URL + movieJsonObject.getString(MOVIE_TRAILERS);
        }

        return data;
    }

    public static String[] getReviewDataFromJson(String jsonStr) throws JSONException {

        jsonRspStrTrailer = jsonStr;
        JSONObject respJsonObject = new JSONObject(jsonRspStrTrailer);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        String[] data = new String[respJsonArray.length()];

        for(int i = 0 ; i < respJsonArray.length() ; i++){
            JSONObject movieJsonObject = respJsonArray.getJSONObject(i);
                data[i] = movieJsonObject.getString(MOVIE_REVIEW);
        }

        return data;
    }

    public static int getMovieId(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getInt(MOVIE_ID);
    }

    public static String getMovieOriginalTitle(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getString(MOVIE_ORIGINAL_TITLE);
    }

    public static String getMoviePlot(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getString(MOVIE_PLOT);
    }

    public static String getMoviePosterPath(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getString(MOVIE_POSTER_PATH);
    }

    public static String getMovieUserRating(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getString(MOVIE_USER_RATING);
    }

    public static String getMovieReleaseDate(int index) throws JSONException {
        //TODO: null check
        JSONObject respJsonObject = new JSONObject(jsonRspStrMain);
        JSONArray respJsonArray = respJsonObject.getJSONArray(MOVIE_RESULTS_ARRAY);
        JSONObject movieJsonObject = respJsonArray.getJSONObject(index);
        return movieJsonObject.getString(MOVIE_RELEASE_DATE);
    }


}
