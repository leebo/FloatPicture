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
    public static void createWindow(WindowManager windowManager, View pictureView, boolean touchable, boolean overLayout, int layoutPositionX, int layoutPositionY) {
        WindowManager.LayoutParams layoutParams = getDefaultLayout(pictureView.getContext(), layoutPositionX, layoutPositionY, touchable, overLayout);
        windowManager.addView(pictureView, layoutParams);
    }

    public static WindowManager.LayoutParams getDefaultLayout(Context context, int layoutPositionX, int layoutPositionY, boolean touchable, boolean overLayout) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        // Use TYPE_SYSTEM_OVERLAY for stronger system coverage (may need special permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }
        // Essential flags for floating overlay that covers status bar and navigation bar
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE  // Make overlay non-touchable by default
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN  // Ensure fullscreen coverage
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  // Allow drawing beyond screen boundaries
                            | WindowManager.LayoutParams.FLAG_FULLSCREEN  // Hide status bar content
                            | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS  // Force draw over system bars
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  // Make status bar translucent
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;  // Make navigation bar translucent
        
        if (overLayout) {
            layoutParams.flags = layoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
        
        // Only make touchable if explicitly requested AND user wants to move the window
        if (touchable) {
            layoutParams.flags = layoutParams.flags & ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        
        // For fullscreen floating pictures, position to cover entire screen including status bar
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;  // Position at absolute top-left
        
        // Force window to extend into status bar area
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        
        // Make the window fullscreen
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            layoutParams.alpha = ((MainApplication) context.getApplicationContext()).getSafeWindowsAlpha();
        }
        
        android.util.Log.d("FloatPicture", "Created fullscreen layout params: " + layoutParams.width + "x" + layoutParams.height);
        return layoutParams;
    }

    public static void updateWindow(WindowManager windowManager, FloatImageView pictureView, boolean touchable, boolean overLayout, int layoutPositionX, int layoutPositionY) {
        if (pictureView != null && pictureView.getParent() != null) {
            try {
                WindowManager.LayoutParams layoutParams = getDefaultLayout(pictureView.getContext(), layoutPositionX, layoutPositionY, touchable, overLayout);
                windowManager.updateViewLayout(pictureView, layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateWindow(WindowManager windowManager, FloatImageView pictureView, Bitmap bitmap, boolean touchable, boolean overLayout, float zoom, float degree, int layoutPositionX, int layoutPositionY) {
        pictureView.refreshDrawableState();
        pictureView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, degree));
        updateWindow(windowManager, pictureView, touchable, overLayout, layoutPositionX, layoutPositionY);
    }
}
