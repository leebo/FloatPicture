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
        float default_zoom = pictureData.getFloat(Config.DATA_PICTURE_DEFAULT_ZOOM, ImageMethods.getDefaultZoom(mContext, bitmap, true));
        float zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, default_zoom);
        float picture_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
        float picture_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, Config.DATA_DEFAULT_PICTURE_ALPHA);
        int position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
        int position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
        FloatImageView floatImageView = ImageMethods.createPictureView(mContext, bitmap, false, false, zoom, picture_degree);
        floatImageView.setAlpha(picture_alpha);
        floatImageView.updatePackageName(ApplicationMethods.getForegroundAppPackageName(mContext));
        ImageMethods.saveFloatImageViewById(mContext, id, floatImageView);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            WindowsMethods.createWindow(windowManager, floatImageView, false, false, position_x, position_y);
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
                    
            android.util.Log.d("FloatPicture", "Activated floating window for ID: " + id);
        } else { // If 'visible' is false, meaning we are trying to hide the active window
            hideWindowById(context, id);
            pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, false);
            pictureData.commit(null);

            // Update global visibility state to false since no image is active
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, false).apply();
                    
            android.util.Log.d("FloatPicture", "Deactivated floating window for ID: " + id);
        }
        updateToggleButtonIcon(context); // Always update toggle button icon after any visibility change
    }
    
    // Helper method to deactivate all images (for single active image design)
    private static void deactivateAllImages(Context context) {
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
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        if (floatImageView != null && floatImageView.getParent() == null) {
            try {
                PictureData pictureData = new PictureData();
                pictureData.setDataControl(id);
                int positionX = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
                int positionY = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
                WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(mContext, positionX, positionY, false, false);
                getWindowManager(mContext).addView(floatImageView, layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(
                    context,
                    0,
                    0,
                    true,
                    false
            );
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
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
    }

    public static void removeToggleButton(Context context) {
        if (toggleButton != null) {
            getWindowManager(context).removeView(toggleButton);
            toggleButton = null;
        }
    }

}
