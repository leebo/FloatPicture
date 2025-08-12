package tool.xfy9326.floatpicture.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.R;

public class FloatEyeButton extends ImageView {
    private static final String TAG = "FloatEyeButton";
    private static final String PREFS_NAME = "float_eye_button_prefs";
    private static final String PREF_POS_X = "eye_button_pos_x";
    private static final String PREF_POS_Y = "eye_button_pos_y";
    private static final int LONG_PRESS_TIMEOUT = 500; // 长按时间阈值（毫秒）
    private static final int DOUBLE_CLICK_TIMEOUT = 300; // 双击时间阈值（毫秒）

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private boolean isMoving = false;
    private boolean isLongPressed = false;
    
    private float startX, startY;
    private float startRawX, startRawY;
    private float currentX, currentY;
    
    // 双击检测相关
    private long lastClickTime = 0;
    private boolean waitingForDoubleClick = false;
    
    private Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            isLongPressed = true;
        }
    };
    
    private Runnable singleClickRunnable = new Runnable() {
        @Override
        public void run() {
            // 执行单击操作（在超时后）
            Log.d(TAG, "Executing single click action");
            toggleFloatImages();
            waitingForDoubleClick = false;
        }
    };

    public FloatEyeButton(Context context) {
        super(context);
        Log.d(TAG, "Creating FloatEyeButton");
        init(context);
    }

    private void init(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        // 设置眼睛图标 - 初始状态应该显示可见图标(表示当前图片是可见的)
        setImageResource(R.drawable.ic_visible);
        setScaleType(ScaleType.CENTER_INSIDE);
        
        // 加载保存的位置
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentX = prefs.getFloat(PREF_POS_X, 100); // 默认位置
        currentY = prefs.getFloat(PREF_POS_Y, 100);
        
        setupLayoutParams();
        setupTouchListener();
    }

    private void setupLayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        
        // 设置窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        
        // 设置窗口标志 - 确保可以接收触摸事件
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        
        // 设置位置和大小
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.x = (int) currentX;
        layoutParams.y = (int) currentY;
        layoutParams.width = 120; // 眼睛按钮大小
        layoutParams.height = 120;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        
        // 设置窗口透明度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            layoutParams.alpha = ((MainApplication) getContext().getApplicationContext()).getSafeWindowsAlpha();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN received");
                        startX = event.getX();
                        startY = event.getY();
                        startRawX = event.getRawX();
                        startRawY = event.getRawY();
                        isMoving = false;
                        isLongPressed = false;
                        
                        // 开始长按检测
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - startRawX);
                        float deltaY = Math.abs(event.getRawY() - startRawY);
                        
                        // 如果移动距离超过阈值，取消长按检测和双击等待
                        if (deltaX > 20 || deltaY > 20) {
                            longPressHandler.removeCallbacks(longPressRunnable);
                            waitingForDoubleClick = false;
                            longPressHandler.removeCallbacks(singleClickRunnable);
                        }
                        
                        // 如果已经长按或者移动距离较大，则进入拖动模式
                        if (isLongPressed || (deltaX > 20 || deltaY > 20)) {
                            isMoving = true;
                            
                            // 更新位置
                            currentX = event.getRawX() - startX;
                            currentY = event.getRawY() - startY;
                            
                            layoutParams.x = (int) currentX;
                            layoutParams.y = (int) currentY;
                            
                            try {
                                windowManager.updateViewLayout(FloatEyeButton.this, layoutParams);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 取消长按检测
                        longPressHandler.removeCallbacks(longPressRunnable);
                        
                        if (!isMoving) {
                            // 短点击：切换浮动图片显示/隐藏
                            Log.d(TAG, "ACTION_UP - performing click");
                            performClick();
                        } else {
                            // 拖动结束：保存位置
                            savePosition();
                            // 取消双击等待
                            waitingForDoubleClick = false;
                            longPressHandler.removeCallbacks(singleClickRunnable);
                        }
                        
                        isMoving = false;
                        isLongPressed = false;
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean performClick() {
        super.performClick();
        Log.d(TAG, "performClick called");
        
        long currentTime = System.currentTimeMillis();
        
        if (waitingForDoubleClick && (currentTime - lastClickTime) < DOUBLE_CLICK_TIMEOUT) {
            // 双击检测到
            Log.d(TAG, "Double click detected - opening main activity");
            longPressHandler.removeCallbacks(singleClickRunnable);
            waitingForDoubleClick = false;
            openMainActivity();
        } else {
            // 第一次点击，开始等待双击
            Log.d(TAG, "Single click - waiting for potential double click");
            waitingForDoubleClick = true;
            lastClickTime = currentTime;
            longPressHandler.postDelayed(singleClickRunnable, DOUBLE_CLICK_TIMEOUT);
        }
        
        return true;
    }

    private void openMainActivity() {
        try {
            Intent intent = new Intent(getContext(), tool.xfy9326.floatpicture.Activities.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleFloatImages() {
        Log.d(TAG, "toggleFloatImages called");
        MainApplication mainApplication = (MainApplication) getContext().getApplicationContext();
        boolean winVisible = mainApplication.getWinVisible();
        Log.d(TAG, "Current window visible state: " + winVisible);
        
        if (winVisible) {
            // 隐藏所有浮动图片
            Log.d(TAG, "Hiding all floating windows");
            ManageMethods.setAllWindowsVisible(getContext(), false);
            setImageResource(R.drawable.ic_invisible); // 图片隐藏时显示不可见图标
            mainApplication.setWinVisible(false);
        } else {
            // 显示所有浮动图片
            Log.d(TAG, "Showing all floating windows");
            ManageMethods.setAllWindowsVisible(getContext(), true);
            setImageResource(R.drawable.ic_visible); // 图片可见时显示可见图标
            mainApplication.setWinVisible(true);
        }
    }

    private void savePosition() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(PREF_POS_X, currentX);
        editor.putFloat(PREF_POS_Y, currentY);
        editor.apply();
    }

    public void show() {
        try {
            if (getParent() == null) {
                Log.d(TAG, "Adding FloatEyeButton to window manager at position (" + layoutParams.x + ", " + layoutParams.y + ")");
                windowManager.addView(this, layoutParams);
                Log.d(TAG, "FloatEyeButton successfully added to window manager");
            } else {
                Log.d(TAG, "FloatEyeButton already has a parent, not adding to window manager");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding FloatEyeButton to window manager", e);
            e.printStackTrace();
        }
    }

    public void hide() {
        try {
            if (getParent() != null) {
                windowManager.removeView(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateVisibilityIcon(boolean visible) {
        if (visible) {
            setImageResource(R.drawable.ic_visible); // 图片可见时显示可见图标
        } else {
            setImageResource(R.drawable.ic_invisible); // 图片隐藏时显示不可见图标
        }
    }
}