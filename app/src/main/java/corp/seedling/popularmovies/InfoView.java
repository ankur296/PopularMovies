package corp.seedling.popularmovies;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ankur Nigam on 10-08-2015.
 */
public class InfoView extends TextView {

    Context mContext;

    public InfoView(Context context) {
        super(context);
        mContext = context;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER ;

        setLayoutParams(params);
    }

}
