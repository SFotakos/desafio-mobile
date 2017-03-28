package stonepagamentos.sfotakos.desafiomobile;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by sfotakos on 22/03/2017.
 */

public class Util {

    /**
     * Remove anything that is not a letter or a number.
     *
     * @param auxString from which characters will be removed from
     * @return String containing letters and numbers
     */
    public static String removeSpecialCharacters(String auxString) {
        return auxString
                .replaceAll("[\\W]", "");
    }

    /**
     * Remove all characters that are not a number.
     *
     * @param auxString which will be turned into number only
     * @return String containing only numbers
     */
    public static String unmaskToNumberOnly(String auxString) {
        return auxString != null ? auxString
                .replaceAll("[\\D]", "") : null;
    }

    public static String formatValueToDisplay(String value) {
        try {
            value = unmaskToNumberOnly(value);
            if (value.length() < 2) {
                value = "0" + value;
            }
            String decimal = value.substring(value.length() - 2, value.length());
            value = value.substring(0, value.length() - 2) + "." + decimal;
            BigDecimal valueBd = new BigDecimal(value);
            return NumberFormat.getCurrencyInstance().format(valueBd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clickEffectDefault(final View view, final Runnable runnable) {

        final int duration = 150 / 2;
        view.animate().
                scaleX(0.80f).
                scaleY(0.80f).
                setDuration(duration).
                withEndAction(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        view.animate().
                                scaleX(1.0f).
                                scaleY(1.0f).
                                setDuration(duration).
                                withEndAction(runnable).start();
                    }
                })).start();
    }

    public static Animation fromAtoB(float fromX, float fromY,
                                     float toX, float toY, long duration,
                                     Animation.AnimationListener animationListener) {

        Animation fromAtoB = new TranslateAnimation(
                Animation.ABSOLUTE, //from xType
                fromX,
                Animation.ABSOLUTE, //to xType
                toX,
                Animation.ABSOLUTE, //from yType
                fromY,
                Animation.ABSOLUTE, //to yType
                toY
        );

        fromAtoB.setDuration(duration);
        fromAtoB.setInterpolator(new AccelerateDecelerateInterpolator());

        if (animationListener != null)
            fromAtoB.setAnimationListener(animationListener);

        return fromAtoB;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into
     *                pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static BitmapDrawable writeOnDrawable(Context context, int drawableId, String text) {

        float _4dp = Util.convertDpToPixel(4, context);
        Resources resources = context.getResources();
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, resources.getDisplayMetrics());
        Bitmap bitmap = BitmapFactory.decodeResource(resources, drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);

        Paint textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(ContextCompat.getColor(context, R.color.white));
        textPaint.setTextSize(fontSize);
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.transparentBlack));

        float textPosX = bitmap.getWidth() - textPaint.measureText(text) - _4dp;

        canvas.drawCircle(textPosX + textPaint.measureText(text)/2f, fontSize/2f, fontSize, backgroundPaint);
        canvas.drawText(text, textPosX, fontSize, textPaint);

        return new BitmapDrawable(resources, bitmap);
    }

    /**
     * Adds text watcher to a specified EditText, passing mask to define how the text should
     * be formatted. For example: ##.###.###-##
     *
     * @param mask        how you want to format the text
     * @param auxEditText field which will be formatted
     * @return TextWatcher with proper link to field and desired behavior
     */
    //TODO: Fix the deletion issue this has.
    public static TextWatcher maskTextWatcher(final String mask, final EditText auxEditText) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String str = removeSpecialCharacters(s.toString());
                String mascara = "";
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                if (count > 0) {
                    int i = 0;
                    for (char m : mask.toCharArray()) {
//					if (m != '#' && str.length() > old.length()) {
                        if (m != '#') {
                            mascara += m;
                            continue;
                        }
                        try {
                            mascara += str.charAt(i);
                        } catch (Exception e) {
                            break;
                        }
                        i++;
                    }
                    isUpdating = true;
                    auxEditText.setText(mascara);
                    auxEditText.setSelection(mascara.length());
                } else {
                    isUpdating = true;
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * Hide soft keyboard.
     *
     * @param view that has focus.
     */
    public static void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =
              (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
