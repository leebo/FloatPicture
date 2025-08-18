package tool.xfy9326.floatpicture.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;
import android.view.View; // Added for HashMap<String, View>

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap; // Added
import java.util.LinkedHashMap; // Added (from original file)
import java.util.Map; // Added
import java.util.Objects;

import tool.xfy9326.floatpicture.Activities.MainActivity;
import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.Methods.ApplicationMethods; // Added
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.EmulatorDetector;
import tool.xfy9326.floatpicture.Utils.EmulatorMaskConfig;
import tool.xfy9326.floatpicture.Utils.RetroEmulatorDatabase;
import tool.xfy9326.floatpicture.View.FloatImageView; // Added
import tool.xfy9326.floatpicture.View.FloatEyeButton; // Added
import tool.xfy9326.floatpicture.View.ManageListAdapter;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "channel_default";
    private RemoteViews remoteViews;
    private NotificationCompat.Builder builder_manage;
    private NotificationButtonBroadcastReceiver notificationButtonBroadcastReceiver;
    private FloatEyeButton floatEyeButton;
    private EmulatorMaskConfig emulatorMaskConfig;
    private String lastForegroundApp = "";

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updatePackageNameRunnable = new Runnable() {
        @Override
        public void run() {
            // 检查前台应用并处理模拟器自动遮罩
            checkForegroundAppAndHandleEmulatorMask();
            handler.postDelayed(this, 3000); // 3秒检查一次
        }
    };

    private static void createNotificationChannel(@NonNull Context context, @NonNull NotificationManagerCompat notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription(context.getString(R.string.notification_channel_des));
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setShowBadge(false);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @SuppressLint({"ForegroundServiceType"})
    @Override
    public void onCreate() {
        super.onCreate();
        currentInstance = this;
        if (notificationButtonBroadcastReceiver == null) {
            notificationButtonBroadcastReceiver = new NotificationButtonBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Config.INTENT_ACTION_NOTIFICATION_BUTTON_CLICK);
            intentFilter.addAction(Config.INTENT_ACTION_NOTIFICATION_UPDATE_COUNT);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(notificationButtonBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(notificationButtonBroadcastReceiver, intentFilter);
            }
        }
        if (builder_manage == null) {
            builder_manage = createNotification();
            startForeground(Config.NOTIFICATION_ID, builder_manage.build());
        }
        
        // 创建并显示浮动眼睛按钮
        if (floatEyeButton == null) {
            floatEyeButton = new FloatEyeButton(this);
            floatEyeButton.show();
        }
        
        // 初始化模拟器遮罩配置
        if (emulatorMaskConfig == null) {
            emulatorMaskConfig = new EmulatorMaskConfig(this);
        }
        
        // 启动前台应用监听
        handler.post(updatePackageNameRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentInstance = null;
        handler.removeCallbacks(updatePackageNameRunnable);
        
        // 隐藏浮动眼睛按钮
        if (floatEyeButton != null) {
            floatEyeButton.hide();
            floatEyeButton = null;
        }
        
        if (notificationButtonBroadcastReceiver != null) {
            unregisterReceiver(notificationButtonBroadcastReceiver);
            notificationButtonBroadcastReceiver = null;
        }
        if (builder_manage != null) {
            stopForeground(true);
            builder_manage = null;
        }
    }

    private NotificationCompat.Builder createNotification() {
        MainApplication mainApplication = (MainApplication) getApplicationContext();
        mainApplication.setWinVisible(true);
        //Create Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel(this, notificationManager);

        builder.setSmallIcon(R.drawable.ic_notification);

        //Content Intent
        Intent intent_main = new Intent(this, MainActivity.class);
        intent_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent_main = PendingIntent.getActivity(this, 0, intent_main, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent_main);

        //Content View
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_manage);
        remoteViews.setImageViewResource(R.id.imageview_notification_application, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.textview_picture_num, getString(R.string.notification_picture_count, String.valueOf(mainApplication.getViewCount())));

        // 恢复通知栏中的控制按钮，与浮动眼睛按钮共存
        remoteViews.setViewVisibility(R.id.imageview_set_picture_view, View.VISIBLE);
        
        // 设置通知栏按钮的点击意图
        Intent intent_button = new Intent(Config.INTENT_ACTION_NOTIFICATION_BUTTON_CLICK);
        PendingIntent pendingIntent_button = PendingIntent.getBroadcast(this, 0, intent_button, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.imageview_set_picture_view, pendingIntent_button);
        
        // 根据当前状态设置按钮图标
        boolean isVisible = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
        remoteViews.setImageViewResource(R.id.imageview_set_picture_view, 
            isVisible ? R.drawable.ic_invisible : R.drawable.ic_visible);

        builder.setContent(remoteViews);
        return builder;
    }

    private class NotificationButtonBroadcastReceiver extends BroadcastReceiver {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (remoteViews != null) {
                MainApplication mainApplication = (MainApplication) getApplicationContext();
                if (Objects.equals(intent.getAction(), Config.INTENT_ACTION_NOTIFICATION_BUTTON_CLICK)) {
                    android.util.Log.d("FloatPicture", "Notification button clicked, current visible: " + mainApplication.getWinVisible());
                    
                    // 使用标准的切换方法，确保状态同步
                    boolean currentVisible = mainApplication.getWinVisible();
                    boolean newVisible = !currentVisible;
                    
                    ManageMethods.setAllWindowsVisible(context, newVisible);
                    
                    // 更新通知栏图标（注意：图标逻辑是反向的）
                    remoteViews.setImageViewResource(R.id.imageview_set_picture_view, 
                        newVisible ? R.drawable.ic_invisible : R.drawable.ic_visible);
                    
                    // 通过ManageMethods的updateEyeButtonIcon方法同步浮动眼睛按钮
                    ManageMethods.updateEyeButtonIcon(context);
                    
                    android.util.Log.d("FloatPicture", "Notification button action completed, new visible: " + newVisible);
                    ManageListAdapter manageListAdapter = ((MainApplication) getApplicationContext()).getManageListAdapter();
                    if (manageListAdapter != null) {
                        manageListAdapter.notifyDataSetChanged();
                    }
                } else if (Objects.equals(intent.getAction(), Config.INTENT_ACTION_NOTIFICATION_UPDATE_COUNT)) {
                    // 更新图片数量
                    remoteViews.setTextViewText(R.id.textview_picture_num, getString(R.string.notification_picture_count, String.valueOf(mainApplication.getViewCount())));
                    
                    // 同时更新可见性图标状态，确保同步
                    boolean isVisible = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
                    remoteViews.setImageViewResource(R.id.imageview_set_picture_view, 
                        isVisible ? R.drawable.ic_invisible : R.drawable.ic_visible);
                    
                    android.util.Log.d("FloatPicture", "Updated notification count and icon, visible: " + isVisible);
                }
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                builder_manage.setContent(remoteViews);
                Objects.requireNonNull(notificationManager).notify(Config.NOTIFICATION_ID, builder_manage.build());
            }
        }
    }
    
    // Static reference to access eye button from other classes
    private static NotificationService currentInstance;
    
    
    public static void bringEyeButtonToFront() {
        if (currentInstance != null && currentInstance.floatEyeButton != null) {
            currentInstance.floatEyeButton.bringToFront();
        }
    }
    
    public static void updateEyeButtonVisibilityIcon(boolean visible) {
        if (currentInstance != null && currentInstance.floatEyeButton != null) {
            currentInstance.floatEyeButton.updateVisibilityIcon(visible);
        }
    }
    
    /**
     * 检查前台应用并处理模拟器自动遮罩
     */
    private void checkForegroundAppAndHandleEmulatorMask() {
        try {
            String currentForegroundApp = ApplicationMethods.getForegroundAppPackageName(this);
            
            if (currentForegroundApp == null || currentForegroundApp.equals(lastForegroundApp)) {
                return; // 没有变化，跳过处理
            }
            
            android.util.Log.d("FloatPicture", "前台应用变化: " + lastForegroundApp + " -> " + currentForegroundApp);
            
            // 处理上一个应用的遮罩隐藏
            handlePreviousAppMaskHide(lastForegroundApp);
            
            // 处理当前应用的遮罩显示
            handleCurrentAppMaskShow(currentForegroundApp);
            
            lastForegroundApp = currentForegroundApp;
            
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "检查前台应用时出错", e);
        }
    }
    
    /**
     * 处理上一个应用的遮罩隐藏
     */
    private void handlePreviousAppMaskHide(String previousApp) {
        if (previousApp == null || previousApp.isEmpty()) {
            return;
        }
        
        // 检查上一个应用是否是模拟器
        RetroEmulatorDatabase.EmulatorInfo emulatorInfo = EmulatorDetector.checkIfInstalledEmulator(this, previousApp);
        if (emulatorInfo != null && emulatorMaskConfig.shouldAutoShowMask(previousApp)) {
            String maskPictureId = emulatorMaskConfig.getMaskPictureId(previousApp);
            if (maskPictureId != null) {
                // 隐藏遮罩
                ManageMethods.setWindowVisible(this, maskPictureId, false);
                android.util.Log.d("FloatPicture", "隐藏模拟器遮罩: " + emulatorInfo.name + " -> " + maskPictureId);
            }
        }
    }
    
    /**
     * 处理当前应用的遮罩显示
     */
    private void handleCurrentAppMaskShow(String currentApp) {
        if (currentApp == null || currentApp.isEmpty()) {
            return;
        }
        
        // 检查当前应用是否是模拟器
        RetroEmulatorDatabase.EmulatorInfo emulatorInfo = EmulatorDetector.checkIfInstalledEmulator(this, currentApp);
        if (emulatorInfo != null && emulatorMaskConfig.shouldAutoShowMask(currentApp)) {
            String maskPictureId = emulatorMaskConfig.getMaskPictureId(currentApp);
            if (maskPictureId != null) {
                // 显示遮罩
                ManageMethods.setWindowVisible(this, maskPictureId, true);
                android.util.Log.d("FloatPicture", "显示模拟器遮罩: " + emulatorInfo.name + " -> " + maskPictureId);
            }
        }
    }
    
    /**
     * 获取模拟器遮罩配置管理器（供外部调用）
     */
    public static EmulatorMaskConfig getEmulatorMaskConfig() {
        if (currentInstance != null) {
            return currentInstance.emulatorMaskConfig;
        }
        return null;
    }
}