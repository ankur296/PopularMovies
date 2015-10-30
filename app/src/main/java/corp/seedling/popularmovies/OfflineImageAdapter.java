package corp.seedling.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import corp.seedling.popularmovies.data.MoviesContract;

public class OfflineImageAdapter extends CursorAdapter {

    private Context mContext;
    int layoutResourceId;
    Bitmap[] data;

    public OfflineImageAdapter(Context c, Cursor cursor, int flags){
        super(c, cursor,0);
        mContext = c;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView iView = new ImageView(context);
        iView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return iView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        byte[] posterBytes = cursor.getBlob(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER));
        Bitmap bitmap;

        if (posterBytes == null)
            bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.notavailable);
        else
            bitmap = Utility.convertByteArrayToBitmap(posterBytes);

        ImageView imageView;

        if (view == null)
            imageView = new ImageView(mContext);
        else
            imageView = (ImageView)view;

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(bitmap);
    }
}
