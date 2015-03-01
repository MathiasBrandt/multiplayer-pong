package com.mathiasbrandt.android.multiplayerpong;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by brandt on 01/03/15.
 */
public class Common {
    /**
     * Converts a value from device independent pixels (dp) to pixels.
     * @param context context.
     * @param dpValue the value to convert.
     * @return the given value converted to pixels.
     */
    public static float toPixels(Context context, float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}
