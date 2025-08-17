package tool.xfy9326.floatpicture.Methods;


import static tool.xfy9326.floatpicture.Methods.WindowsMethods.getWindowManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.WindowManager;
import android.view.Gravity;

import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;
import tool.xfy9326.floatpicture.View.FloatImageView;
import tool.xfy9326.floatpicture.View.FloatToggleButton;
import tool.xfy9326.floatpicture.R;


public class ManageMethods {

    private static FloatToggleButton toggleButton;

    public static void SelectPicture(Activity mActivity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mActivity.startActivityForResult(intent, Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_GET_PICTURE);
    }

    public static void RunWin(Context mContext) {
        if (PermissionMethods.checkPermission(mContext, PermissionMethods.StoragePermission)) {
            PictureData pictureData = new PictureData();
            LinkedHashMap<String, String> list = pictureData.getListArray();
            WindowManager windowManager = getWindowManager(mContext);
            if (!list.isEmpty()) {
                // Only show the most recent (last) floating image instead of all images
                String lastImageId = null;
                for (LinkedHashMap.Entry<?, ?> entry : list.entrySet()) {
                    lastImageId = entry.getKey().toString();
                }
                if (lastImageId != null) {
                    StartWin(mContext, windowManager, pictureData, lastImageId);
                }
            }
        }
    }

    private static void StartWin(Context mContext, WindowManager windowManager, PictureData pictureData, String id) {
        pictureData.setDataControl(id);
        Bitmap bitmap = ImageMethods.getShowBitmap(mContext, id);
        // For fullscreen floating pictures, position is fixed and zoom is handled automatically
        float picture_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
        FloatImageView floatImageView = ImageMethods.createPictureView(mContext, bitmap, false, false, 1.0f, picture_degree);
        // No alpha settings - always fully opaque
        floatImageView.updatePackageName(ApplicationMethods.getForegroundAppPackageName(mContext));
        ImageMethods.saveFloatImageViewById(mContext, id, floatImageView);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            WindowsMethods.createWindow(windowManager, floatImageView, false, false);
        }
    }

    public static void DeleteWin(Context mContext, String id) {
        PictureData pictureData = new PictureData();
        pictureData.setDataControl(id);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
            if (floatImageView != null && floatImageView.getParent() != null) {
                try {
                    getWindowManager(mContext).removeView(floatImageView);
                    floatImageView.refreshDrawableState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        pictureData.remove();
        ImageMethods.clearAllTemp(mContext, id);
    }

    static void CloseAllWindows(Context mContext) {
        HashMap<String, View> hashMap = ((MainApplication) mContext.getApplicationContext()).getRegister();
        WindowManager windowManager = getWindowManager(mContext);
        PictureData pictureData = new PictureData();
        if (!hashMap.isEmpty()) {
            for (HashMap.Entry<?, ?> entry : hashMap.entrySet()) {
                pictureData.setDataControl(entry.getKey().toString());
                if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
                    FloatImageView floatImageView = (FloatImageView) entry.getValue();
                    if (floatImageView != null && floatImageView.getParent() != null) {
                        try {
                            windowManager.removeView(floatImageView);
                            floatImageView.refreshDrawableState();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void showSingleFloatingImage(Context mContext, String newImageId) {
        // First close all existing floating windows
        CloseAllWindows(mContext);
        
        // Then show only the new image
        if (PermissionMethods.checkPermission(mContext, PermissionMethods.StoragePermission)) {
            PictureData pictureData = new PictureData();
            WindowManager windowManager = getWindowManager(mContext);
            StartWin(mContext, windowManager, pictureData, newImageId);
        }
    }

    public static void updateNotificationCount(Context context) {
        context.sendBroadcast(new Intent().setAction(Config.INTENT_ACTION_NOTIFICATION_UPDATE_COUNT));
    }

    public static void setAllWindowsVisible(Context context, boolean visible) {
        String id;
        PictureData pictureData = new PictureData();
        LinkedHashMap<String, String> linkedHashMap = pictureData.getListArray();
        for (Map.Entry<?, ?> o : linkedHashMap.entrySet()) {
            id = o.getKey().toString();
            setWindowVisible(context, pictureData, id, visible);
        }
        
        // Update MainApplication state
        MainApplication mainApplication = (MainApplication) context.getApplicationContext();
        mainApplication.setWinVisible(visible);
        
        // Update global preferences
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, visible).apply();
        
        updateToggleButtonIcon(context); // Update toggle button icon when visibility changes
    }

    public static void setWindowVisible(Context context, PictureData pictureData, String id, boolean visible) {
        pictureData.setDataControl(id);
        if (visible) {
            // Single active image design: First deactivate all other images
            deactivateAllImages(context);
            
            // Then activate only this image
            showWindowById(context, id);
            pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
            pictureData.commit(null);

            // Update global visibility state
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, true).apply();
            
            // Update MainApplication state
            MainApplication mainApplication = (MainApplication) context.getApplicationContext();
            mainApplication.setWinVisible(true);
                    
            android.util.Log.d("FloatPicture", "Activated floating window for ID: " + id);
        } else { // If 'visible' is false, meaning we are trying to hide the active window
            hideWindowById(context, id);
            pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, false);
            pictureData.commit(null);

            // Update global visibility state to false since no image is active
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, false).apply();
            
            // Update MainApplication state
            MainApplication mainApplication = (MainApplication) context.getApplicationContext();
            mainApplication.setWinVisible(false);
                    
            android.util.Log.d("FloatPicture", "Deactivated floating window for ID: " + id);
        }
        updateToggleButtonIcon(context); // Always update toggle button icon after any visibility change
    }
    
    // Helper method to deactivate all images (for single active image design)
    public static void deactivateAllImages(Context context) {
        PictureData tempPictureData = new PictureData();
        LinkedHashMap<String, String> allPictures = tempPictureData.getListArray();
        for (Map.Entry<String, String> entry : allPictures.entrySet()) {
            String imageId = entry.getKey();
            PictureData imagePictureData = new PictureData();
            imagePictureData.setDataControl(imageId);
            if (imagePictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, false)) {
                hideWindowById(context, imageId);
                imagePictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, false);
                imagePictureData.commit(null);
            }
        }
    }

    private static void hideWindowById(Context mContext, String id) {
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        if (floatImageView != null && floatImageView.getParent() != null) {
            try {
                getWindowManager(mContext).removeView(floatImageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void showWindowById(Context mContext, String id) {
        try {
            // Get picture data first
            PictureData pictureData = new PictureData();
            pictureData.setDataControl(id);
            
            // Get or create FloatImageView for this ID
            FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
            
            if (floatImageView == null || floatImageView.getParent() != null) {
                // Create new FloatImageView if it doesn't exist or is already attached
                android.util.Log.d("FloatPicture", "Creating new FloatImageView for ID: " + id);
                
                // Load bitmap from file
                android.graphics.Bitmap bitmap = ImageMethods.getShowBitmap(mContext, id);
                if (bitmap == null) {
                    android.util.Log.e("FloatPicture", "Failed to load bitmap for floating window ID: " + id);
                    return;
                }
                
                // Get picture settings (zoom is handled automatically for fullscreen)
                float degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
                
                // Create FloatImageView with automatic fullscreen scaling - no alpha settings
                floatImageView = ImageMethods.createPictureView(mContext, bitmap, false, false, 1.0f, degree);
                floatImageView.setPictureId(id);
                
                // Save for reuse
                ImageMethods.saveFloatImageViewById(mContext, id, floatImageView);
            }
            
            // Show the window if not already showing
            if (floatImageView != null && floatImageView.getParent() == null) {
                // Force full opacity before showing
                floatImageView.setAlpha(1.0f);
                
                WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(mContext, false, false);
                getWindowManager(mContext).addView(floatImageView, layoutParams);
                
                // Ensure eye button stays on top after showing mask image
                try {
                    tool.xfy9326.floatpicture.Services.NotificationService.bringEyeButtonToFront();
                } catch (Exception e) {
                    android.util.Log.w("FloatPicture", "Could not bring eye button to front: " + e.getMessage());
                }
                
                android.util.Log.d("FloatPicture", "Successfully showed floating window for ID: " + id + " (forced alpha=1.0)");
            } else {
                android.util.Log.w("FloatPicture", "FloatImageView is null or already has parent for ID: " + id);
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error showing window for ID: " + id, e);
            e.printStackTrace();
        }
    }

    public static void hideAllWindows(Context mContext) {
        PictureData tempPictureData = new PictureData();
        LinkedHashMap<String, String> allPictures = tempPictureData.getListArray();
        if (allPictures != null) {
            for (Map.Entry<String, String> entry : allPictures.entrySet()) {
                String imageId = entry.getKey();
                hideWindowById(mContext, imageId);
            }
        }
    }

    public static void createToggleButton(Context context) {
        if (toggleButton == null) {
            toggleButton = new FloatToggleButton(context);
            toggleButton.setOnClickListener(v -> {
                boolean currentVisibility = PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
                boolean newVisibility = !currentVisibility;
                setAllWindowsVisible(context, newVisibility);
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, newVisibility).apply();
                updateToggleButtonIcon(context);
            });

            WindowManager windowManager = getWindowManager(context);
            WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(context, true, false, true);
            layoutParams.x = (int) context.getResources().getDimension(R.dimen.fab_margin);
            layoutParams.y = (int) context.getResources().getDimension(R.dimen.fab_margin);

            windowManager.addView(toggleButton, layoutParams);
            updateToggleButtonIcon(context);
        }
    }

    public static void updateToggleButtonIcon(Context context) {
        if (toggleButton != null) {
            boolean isVisible = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
            if (isVisible) {
                toggleButton.setIcon(R.drawable.ic_visibility, R.string.toggle_visibility);
            } else {
                toggleButton.setIcon(R.drawable.ic_visibility_off, R.string.toggle_visibility_off);
            }
        }
        
        // Also update eye button icon to stay synchronized
        updateEyeButtonIcon(context);
    }
    
    public static void updateEyeButtonIcon(Context context) {
        // Update eye button icon based on global visibility state
        boolean isVisible = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
        try {
            tool.xfy9326.floatpicture.Services.NotificationService.updateEyeButtonVisibilityIcon(isVisible);
        } catch (Exception e) {
            android.util.Log.w("FloatPicture", "Could not update eye button icon: " + e.getMessage());
        }
        
        // Also update notification bar icon
        updateNotificationIcon(context);
    }
    
    public static void updateNotificationIcon(Context context) {
        // Send broadcast to update notification icon
        try {
            Intent updateIntent = new Intent(Config.INTENT_ACTION_NOTIFICATION_UPDATE_COUNT);
            context.sendBroadcast(updateIntent);
            android.util.Log.d("FloatPicture", "Sent notification icon update broadcast");
        } catch (Exception e) {
            android.util.Log.w("FloatPicture", "Could not update notification icon: " + e.getMessage());
        }
    }

    public static void removeToggleButton(Context context) {
        if (toggleButton != null) {
            getWindowManager(context).removeView(toggleButton);
            toggleButton = null;
        }
    }

}
