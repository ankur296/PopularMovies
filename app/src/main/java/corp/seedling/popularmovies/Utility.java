package corp.seedling.popularmovies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;

public class Utility {

    public static String strSeparator = "__,__";

    public static String convertArrayToString(String[] array){
        String str = "";
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                str = str + array[i];
                // Do not append comma at the end of last element
                if (i < array.length - 1) {
                    str = str + strSeparator;
                }
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str){
        if (str.isEmpty() || str.equals("")){
            return null;
        }else {
            String[] arr = str.split(strSeparator);
            return arr;
        }
    }

    public static Bitmap convertByteArrayToBitmap(byte[] bytearray){
        ByteArrayInputStream imageStream = new ByteArrayInputStream(bytearray);
        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
        return theImage;
    }


}
