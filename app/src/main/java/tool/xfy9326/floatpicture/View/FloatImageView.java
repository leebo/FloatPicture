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
    private String PictureId = "";
    private WindowManager windowManager;
    private boolean overLayout = false;

    private ImageView imageView;

    public FloatImageView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // Force complete opacity at every level
        setBackgroundColor(0xFF000000);  // Solid black background
        setAlpha(1.0f);  // Force frame alpha to maximum
        
        // Create and add ImageView with forced opacity
        imageView = new AppCompatImageView(context);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ScaleType.FIT_XY);  // Scale to fill exactly
        imageView.setBackgroundColor(0xFF000000);  // Solid black for ImageView too
        imageView.setAlpha(1.0f);  // Force ImageView alpha to maximum
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

    // Moveable functionality removed since pictures are now fullscreen

    public void setOverLayout(boolean overLayout) {
        this.overLayout = overLayout;
    }

    public void setImageBitmap(android.graphics.Bitmap bm) {
        if (bm != null) {
            android.util.Log.d("FloatPicture", "Setting bitmap to FloatImageView: " + bm.getWidth() + "x" + bm.getHeight() + " bytes=" + bm.getByteCount());
            imageView.setImageBitmap(bm);
            
            // Force full opacity for floating images
            this.setAlpha(1.0f);
            imageView.setAlpha(1.0f);
            android.util.Log.d("FloatPicture", "Forced FloatImageView and ImageView alpha to 1.0 (fully opaque)");
        } else {
            android.util.Log.e("FloatPicture", "Attempted to set null bitmap to FloatImageView");
        }
    }

    public void setScaleType(android.widget.ImageView.ScaleType scaleType) {
        imageView.setScaleType(scaleType);
    }

    public void updatePackageName(String packageName) {
        // Package name tracking removed to reduce memory usage
    }

    // Touch events removed - fullscreen pictures don't need positioning
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // For fullscreen pictures, don't consume touch events
        return false;
    }

    // Position methods removed - fullscreen pictures are fixed position

}
