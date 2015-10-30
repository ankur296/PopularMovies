package corp.seedling.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mPosterURLs = new String[]{};

    public ImageAdapter(Context c){
        mContext = c;
    }

    public void setPosterURLs(String[] posterURLs){
        mPosterURLs = posterURLs.clone();
    }

    public String[] getPosterURLs(){
        return mPosterURLs;
    }
    public void clearData() {
        // clear the data
        mPosterURLs = null;
    }

    @Override
    public int getCount() {
        return mPosterURLs.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
        }
        else{
            imageView = (ImageView)convertView;
        }
        Glide.with(mContext)
                .load(mPosterURLs[position])
                .fitCenter()
                .error(R.drawable.notavailable)
                .into(imageView)
        ;
        return imageView;
    }
}
