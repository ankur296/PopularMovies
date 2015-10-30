package corp.seedling.popularmovies;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ankur Nigam on 10-08-2015.
 */
public class ReviewView extends TextView {

    Context mContext;

    public ReviewView(Context context) {
        super(context);
        mContext = context;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER | Gravity.LEFT;

        setLayoutParams(params);
        setPadding(convertDpToPixels(20), convertDpToPixels(10),
                0, convertDpToPixels(10));
    }

    public int convertDpToPixels(int dp){
        return (int) ( (dp * mContext.getResources().getDisplayMetrics().density)  + 0.5f) ;
    }
}
