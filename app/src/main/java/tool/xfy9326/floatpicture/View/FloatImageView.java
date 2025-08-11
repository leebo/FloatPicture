package tool.xfy9326.floatpicture.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.widget.AppCompatImageView;

import tool.xfy9326.floatpicture.Methods.WindowsMethods;
import tool.xfy9326.floatpicture.Utils.Config;

public class FloatImageView extends FrameLayout {
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
    private TextView packageNameTextView;

    public FloatImageView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // Create and add ImageView
        imageView = new AppCompatImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ScaleType.MATRIX);
        addView(imageView);

        // Create and add TextView for package name
        packageNameTextView = new TextView(context);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        packageNameTextView.setLayoutParams(textParams);
        packageNameTextView.setTextColor(Color.WHITE);
        packageNameTextView.setBackgroundColor(Color.BLACK);
        packageNameTextView.setPadding(8, 4, 8, 4);
        addView(packageNameTextView);
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

    public void updatePackageName(String packageName) {
        packageNameTextView.setText(packageName);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!moveable) {
            return imageView.onTouchEvent(event);
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
