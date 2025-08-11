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
                for (LinkedHashMap.Entry<?, ?> entry : list.entrySet()) {
                    StartWin(mContext, windowManager, pictureData, entry.getKey().toString());
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
        boolean touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
        boolean over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
        FloatImageView floatImageView = ImageMethods.createPictureView(mContext, bitmap, touch_and_move, over_layout, zoom, picture_degree);
        floatImageView.setAlpha(picture_alpha);
        ImageMethods.saveFloatImageViewById(mContext, id, floatImageView);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, over_layout, position_x, position_y);
        }
    }

    public static void DeleteWin(Context mContext, String id) {
        PictureData pictureData = new PictureData();
        pictureData.setDataControl(id);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
            if (floatImageView != null) {
                getWindowManager(mContext).removeView(floatImageView);
                floatImageView.refreshDrawableState();
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
                    windowManager.removeView(floatImageView);
                    floatImageView.refreshDrawableState();
                }
            }
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
        boolean data_visible = pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, visible);
        if (visible) {
            if (!data_visible) {
                showWindowById(context, id);
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                pictureData.commit(null);

                // Update global visibility state (Requirement 1)
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, true).apply();
            }
        } else { // If 'visible' is false, meaning we are trying to hide a window
            if (data_visible) {
                hideWindowById(context, id);
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, false);
                pictureData.commit(null);

                // Check if all windows are now hidden to update global visibility state (Requirement 1)
                boolean anyWindowStillVisible = false;
                LinkedHashMap<String, String> allPictures = pictureData.getListArray();
                for (Map.Entry<String, String> entry : allPictures.entrySet()) {
                    PictureData checkPictureData = new PictureData();
                    checkPictureData.setDataControl(entry.getKey());
                    if (checkPictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, false)) {
                        anyWindowStillVisible = true;
                        break;
                    }
                }
                if (!anyWindowStillVisible) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, false).apply();
                }
            }
        }
        updateToggleButtonIcon(context); // Always update toggle button icon after any visibility change
    }

    private static void hideWindowById(Context mContext, String id) {
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        getWindowManager(mContext).removeView(floatImageView);
    }

    private static void showWindowById(Context mContext, String id) {
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        PictureData pictureData = new PictureData();
        pictureData.setDataControl(id);
        int positionX = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
        int positionY = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
        boolean touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
        boolean over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
        WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(mContext, positionX, positionY, touch_and_move, over_layout);
        getWindowManager(mContext).addView(floatImageView, layoutParams);
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
