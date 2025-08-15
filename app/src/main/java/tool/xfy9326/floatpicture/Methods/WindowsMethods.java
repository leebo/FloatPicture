package tool.xfy9326.floatpicture.Methods;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.View.FloatImageView;


public class WindowsMethods {
    public static WindowManager getWindowManager(Context mContext) {
        return (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    @SuppressWarnings("SameParameterValue")
    public static void createWindow(WindowManager windowManager, View pictureView, boolean touchable, boolean overLayout) {
        WindowManager.LayoutParams layoutParams = getDefaultLayout(pictureView.getContext(), touchable, overLayout);
        windowManager.addView(pictureView, layoutParams);
    }

    public static WindowManager.LayoutParams getDefaultLayout(Context context, boolean touchable, boolean overLayout) {
        return getDefaultLayout(context, touchable, overLayout, false);
    }
    
    public static WindowManager.LayoutParams getDefaultLayout(Context context, boolean touchable, boolean overLayout, boolean isControlButton) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        
        if (isControlButton) {
            // Control buttons need higher priority to stay above mask layers
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;  // Higher priority
            }
        } else {
            // Mask windows use standard overlay type 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;  // Lower priority than control buttons
            }
        }
        
        if (isControlButton) {
            // For control buttons (like eye button), use simpler flags to stay on top
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // Control buttons should be touchable by default
        } else {
            // Standard flags for non-touchable overlay - add FLAG_DIM_BEHIND for opaque effect
            // Important: Do NOT use FLAG_LAYOUT_NO_LIMITS to avoid covering control buttons
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            // Set maximum dimming for complete opacity
            layoutParams.dimAmount = 1.0f;
        }
        
        if (overLayout) {
            layoutParams.flags = layoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
        
        // Only make touchable if explicitly requested AND user wants to move the window
        if (touchable) {
            layoutParams.flags = layoutParams.flags & ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        
        if (isControlButton) {
            // For control buttons, use default positioning and sizing
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            android.util.Log.d("FloatPicture", "Set control button format to TRANSLUCENT");
        } else {
            // Fullscreen floating picture layout - use OPAQUE format for mask pictures
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.x = 0;
            layoutParams.y = 0;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.format = PixelFormat.OPAQUE;  // Force opaque format for mask pictures
            android.util.Log.d("FloatPicture", "Set fullscreen picture format to OPAQUE for mask");
        }
        
        // No alpha/transparency settings - always fully opaque
        
        android.util.Log.d("FloatPicture", "Created layout params: " + layoutParams.width + "x" + layoutParams.height + " gravity=" + layoutParams.gravity + " flags=" + layoutParams.flags);
        return layoutParams;
    }

    public static void updateWindow(WindowManager windowManager, FloatImageView pictureView, boolean touchable, boolean overLayout) {
        if (pictureView != null && pictureView.getParent() != null) {
            try {
                WindowManager.LayoutParams layoutParams = getDefaultLayout(pictureView.getContext(), touchable, overLayout);
                windowManager.updateViewLayout(pictureView, layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateWindow(WindowManager windowManager, FloatImageView pictureView, Bitmap bitmap, boolean touchable, boolean overLayout, float zoom, float degree) {
        pictureView.refreshDrawableState();
        pictureView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, degree));
        updateWindow(windowManager, pictureView, touchable, overLayout);
    }
}
