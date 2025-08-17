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
        
        // 统一使用相同的窗口类型避免层级冲突
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        
        if (isControlButton) {
            // For control buttons (like eye button), use simpler flags to stay on top
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // Control buttons should be touchable by default
        } else {
            // Base flags for floating image overlay
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            
            // Configure touch behavior based on touchable parameter
            if (!touchable) {
                // For non-interactive floating images: allow touches to pass through to apps below
                layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                android.util.Log.d("FloatPicture", "Set floating image as non-touchable (touch passthrough enabled)");
            } else {
                // For interactive floating images: capture touches but don't interfere with system
                android.util.Log.d("FloatPicture", "Set floating image as touchable (interactive mode)");
            }
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
            layoutParams.format = PixelFormat.RGBA_8888;  // 使用RGBA格式而不是TRANSLUCENT
            android.util.Log.d("FloatPicture", "Set control button format to RGBA_8888 (isolated from mask)");
        } else {
            // Fullscreen floating picture layout - COMPLETELY OPAQUE
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.x = 0;
            layoutParams.y = 0;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.format = PixelFormat.OPAQUE;  // Force opaque format - no transparency allowed
            
            // Handle display cutout (notch) for Android P+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            
            android.util.Log.d("FloatPicture", "Set fullscreen picture format to OPAQUE for mask with status bar coverage");
        }
        
        // CRITICAL: Force window to be completely opaque at all levels
        layoutParams.alpha = 1.0f;  // Window level opacity
        
        if (!isControlButton) {
            // For floating images, eliminate ALL sources of transparency
            layoutParams.dimAmount = 0.0f;  // No dimming effect
            layoutParams.screenBrightness = -1.0f;  // Don't change screen brightness
            layoutParams.buttonBrightness = -1.0f;  // Don't change button brightness
            
            // Force solid window appearance
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }
        
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
