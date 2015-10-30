package corp.seedling.popularmovies;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

/**
 * Created by Ankur Nigam on 10-08-2015.
 */
public class SpaceView extends TextView {

    Context mContext;

    public SpaceView(Context context, Object object) {
        super(context);
        mContext = context;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT , convertDpToPixels(1));
        params.setMargins(convertDpToPixels(10), 0, convertDpToPixels(10), 0);
        setLayoutParams(params);
        setBackgroundResource(android.R.color.darker_gray);
        setTag(object);
    }

    public int convertDpToPixels(int dp){
        return (int) ( (dp * mContext.getResources().getDisplayMetrics().density)  + 0.5f) ;
    }
}
