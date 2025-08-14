package tool.xfy9326.floatpicture.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.appcompat.widget.AppCompatImageView;

import tool.xfy9326.floatpicture.Methods.WindowsMethods;
import tool.xfy9326.floatpicture.Utils.Config;

public class FloatImageView extends FrameLayout {
    private static final String PREFS_NAME = "tool_float_picture_prefs";
    private static final String PREF_POS_X = "float_picture_pos_x";
    private static final String PREF_POS_Y = "float_picture_pos_y";

    private String PictureId = "";
    private WindowManager windowManager;
    private boolean moveable = false;
    private boolean overLayout = false;

    private float mTouchStartX = 0;
    private float mTouchStartY = 0;
    private float x = 0;
    private float y = 0;
    private float mNowPositionX = Config.DATA_DEFAULT_PICTURE_POSITION_X;
    private float mNowPositionY = Config.DATA_DEFAULT_PICTURE_POSITION_Y;

    private ImageView imageView;

    public FloatImageView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // Load saved position
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mNowPositionX = prefs.getFloat(PREF_POS_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
        mNowPositionY = prefs.getFloat(PREF_POS_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);

        // Create and add ImageView
        imageView = new AppCompatImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ScaleType.MATRIX);
        addView(imageView);

        // Note: Don't add to WindowManager in constructor - this may be called from background thread
        // The view should be added to WindowManager explicitly when needed
    }

    @SuppressWarnings("unused")
    public String getPictureId() {
        return PictureId;
    }

    public void setPictureId(String id) {
        PictureId = id;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public void setOverLayout(boolean overLayout) {
        this.overLayout = overLayout;
    }

    public void setImageBitmap(android.graphics.Bitmap bm) {
        imageView.setImageBitmap(bm);
    }

    public void setScaleType(android.widget.ImageView.ScaleType scaleType) {
        imageView.setScaleType(scaleType);
    }

    public void updatePackageName(String packageName) {
        // Package name tracking removed to reduce memory usage
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!moveable) {
            // When not moveable, don't consume touch events - let them pass through
            return false;
        }
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
            }
            case MotionEvent.ACTION_MOVE -> {
                getNowPosition();
                updatePosition();
            }
            case MotionEvent.ACTION_UP -> {
                getNowPosition();
                updatePosition();
                mTouchStartX = mTouchStartY = 0;
                // Save position
                SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(PREF_POS_X, mNowPositionX);
                editor.putFloat(PREF_POS_Y, mNowPositionY);
                editor.apply();
            }
        }
        return super.onTouchEvent(event);
    }

    public float getMovedPositionX() {
        return mNowPositionX;
    }

    public float getMovedPositionY() {
        return mNowPositionY;
    }

    private void getNowPosition() {
        mNowPositionX = x - mTouchStartX;
        mNowPositionY = y - mTouchStartY;
    }

    private void updatePosition() {
        windowManager.updateViewLayout(this, WindowsMethods.getDefaultLayout(getContext(), (int) mNowPositionX, (int) mNowPositionY, moveable, overLayout));
    }

}
