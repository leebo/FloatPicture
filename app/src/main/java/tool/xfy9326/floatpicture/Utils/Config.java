package tool.xfy9326.floatpicture.Utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class Config {
    public final static int NOTIFICATION_ID = 4500;

    public final static int REQUEST_CODE_PERMISSION_STORAGE = 1;
    public final static int REQUEST_CODE_PERMISSION_OVERLAY = 2;
    public final static int REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_ADD = 3;
    public final static int REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_GET_PICTURE = 4;
    public final static int REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_CHANGE = 5;

    public final static String INTENT_PICTURE_EDIT_POSITION = "EDIT_POSITION";
    public final static String INTENT_PICTURE_EDIT_ID = "EDIT_ID";
    public final static String INTENT_PICTURE_EDIT_MODE = "EDIT_MODE";

    public final static String INTENT_ACTION_NOTIFICATION_BUTTON_CLICK = "ACTION_NOTIFICATION_BUTTON_CLICK";
    public final static String INTENT_ACTION_NOTIFICATION_UPDATE_COUNT = "ACTION_NOTIFICATION_UPDATE_COUNT";

    public final static String DATA_PICTURE_SHOW_ENABLED = "SHOW_ENABLED";
    public final static String DATA_PICTURE_POSITION_X = "POSITION_X";
    public final static String DATA_PICTURE_POSITION_Y = "POSITION_Y";
    public final static String DATA_PICTURE_ZOOM = "ZOOM";
    public final static String DATA_PICTURE_DEFAULT_ZOOM = "DEFAULT_ZOOM";
    public final static String DATA_PICTURE_ALPHA = "ALPHA";
    public final static String DATA_PICTURE_DEGREE = "DEGREE";
    public final static String DATA_PICTURE_TOUCH_AND_MOVE = "TOUCH_AND_MOVE";
    public final static String DATA_ALLOW_PICTURE_OVER_LAYOUT = "ALLOW_PICTURE_OVER_LAYOUT";

    public final static boolean DATA_DEFAULT_PICTURE_SHOW_ENABLED = true;
    public final static int DATA_DEFAULT_PICTURE_POSITION_X = 0;
    public final static int DATA_DEFAULT_PICTURE_POSITION_Y = 0;
    public final static float DATA_DEFAULT_PICTURE_ALPHA = 0.5f;
    public final static float DATA_DEFAULT_PICTURE_DEGREE = 0f;
    public final static boolean DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE = false;
    public final static boolean DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT = false;

    public final static String PREFERENCE_PICTURE_NAME = "settings_picture_name";
    public final static String PREFERENCE_ALLOW_PICTURE_OVER_LAYOUT = "settings_allow_picture_over_layout";
    public final static String PREFERENCE_PICTURE_RESIZE = "settings_picture_resize";
    public final static String PREFERENCE_PICTURE_ALPHA = "settings_picture_alpha";
    public final static String PREFERENCE_PICTURE_POSITION = "settings_picture_position";
    public final static String PREFERENCE_PICTURE_DEGREE = "settings_picture_degree";
    public final static String PREFERENCE_PICTURE_TOUCH_AND_MOVE = "settings_picture_touchable_and_moveable";

    public final static String PREFERENCE_BOOT_AUTO_RUN = "boot_auto_run";
    public final static String PREFERENCE_SHOW_NOTIFICATION_CONTROL = "show_notification_control";
    public final static String PREFERENCE_NEW_PICTURE_QUALITY = "new_picture_quality";
    public final static String PREFERENCE_TOUCHABLE_POSITION_EDIT = "touchable_position_edit";

    public final static String PREFERENCE_GLOBAL_VISIBILITY_STATE = "global_visibility_state";
    public final static boolean DATA_DEFAULT_GLOBAL_VISIBILITY = true;

    public final static String PREFERENCE_SCREEN_WIDTH = "screen_width";
    public final static String PREFERENCE_SCREEN_HEIGHT = "screen_height";
    public final static String PREFERENCE_SCREEN_DENSITY_DPI = "screen_density_dpi";
    public final static String PREFERENCE_SCREEN_RATIO = "screen_ratio";
    public final static String PREFERENCE_SCREEN_SIZE_INCHES = "screen_size_inches";
    public final static String PREFERENCE_FIRST_LAUNCH = "first_launch";

    public final static String LICENSE_PATH_APPLICATION = "LICENSE";
    // Use app-specific external storage to avoid permission issues on Android 10+
    private static String getAppExternalStorageDir(Context context) {
        if (context != null) {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                return externalFilesDir.getAbsolutePath() + File.separator;
            }
        }
        // Fallback to traditional external storage
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FloatPicture" + File.separator;
    }
    
    // These will be initialized dynamically
    private static String DEFAULT_APPLICATION_DIR = null;
    public static String DEFAULT_PICTURE_TEMP_DIR = null;
    static String DEFAULT_DATA_DIR = null;
    public static String DEFAULT_PICTURE_DIR = null;
    public static String NO_MEDIA_FILE_DIR = null;
    
    public static void initializePaths(Context context) {
        if (DEFAULT_APPLICATION_DIR == null) {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                DEFAULT_APPLICATION_DIR = externalFilesDir.getAbsolutePath() + File.separator;
            } else {
                DEFAULT_APPLICATION_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FloatPicture" + File.separator;
            }
            DEFAULT_PICTURE_TEMP_DIR = DEFAULT_APPLICATION_DIR + "Pictures" + File.separator + ".TEMP" + File.separator;
            DEFAULT_DATA_DIR = DEFAULT_APPLICATION_DIR + "Data" + File.separator;
            DEFAULT_PICTURE_DIR = DEFAULT_APPLICATION_DIR + "Pictures" + File.separator;
            NO_MEDIA_FILE_DIR = DEFAULT_APPLICATION_DIR + ".nomedia";
        }
    }
}
