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
import tool.xfy9326.floatpicture.Utils.Config;

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
    private int initialX, initialY;
    
    // 双击检测相关
    private long lastClickTime = 0;
    private boolean waitingForDoubleClick = false;
    
    private Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            isLongPressed = true;
            // 长按时设置灰色滤镜表示可拖动状态
            setDragModeVisual(true);
            Log.d(TAG, "Long press detected - entering drag mode with gray tint");
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
        
        // 设置眼睛图标 - 根据当前全局可见性状态设置
        boolean isVisible = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
        setImageResource(isVisible ? R.drawable.ic_visible : R.drawable.ic_invisible);
        setScaleType(ScaleType.CENTER_INSIDE);
        
        // 加载保存的位置
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        float savedX = prefs.getFloat(PREF_POS_X, 100); // 默认位置
        float savedY = prefs.getFloat(PREF_POS_Y, 100);
        
        // 先设置初始位置（无约束检查），再设置布局参数
        currentX = savedX;
        currentY = savedY;
        
        setupLayoutParams();
        
        // 布局参数设置后再应用边界约束
        currentX = constrainX((int) currentX);
        currentY = constrainY((int) currentY);
        
        // 更新布局参数中的位置
        layoutParams.x = (int) currentX;
        layoutParams.y = (int) currentY;
        
        Log.d(TAG, "Loaded position: (" + savedX + ", " + savedY + ") -> constrained to: (" + currentX + ", " + currentY + ")");
        setupTouchListener();
    }

    private void setupLayoutParams() {
        // 使用WindowsMethods的统一方法创建控制按钮布局参数
        layoutParams = tool.xfy9326.floatpicture.Methods.WindowsMethods.getDefaultLayout(getContext(), true, false, true);
        
        // 覆盖位置设置为自定义位置
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = (int) currentX;
        layoutParams.y = (int) currentY;
        layoutParams.width = 120; // 眼睛按钮大小
        layoutParams.height = 120;
        
        android.util.Log.d("FloatEyeButton", "Using unified layout params for eye button to avoid conflicts");
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
                        // 记录当前窗口位置
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
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
                            // 如果还没进入拖动模式但取消了长按，清除可能的视觉效果
                            if (!isMoving) {
                                setDragModeVisual(false);
                            }
                        }
                        
                        // 如果已经长按或者移动距离较大，则进入拖动模式
                        if (isLongPressed || (deltaX > 20 || deltaY > 20)) {
                            if (!isMoving) {
                                // 首次进入拖动模式时设置灰色视觉效果
                                isMoving = true;
                                setDragModeVisual(true);
                                Log.d(TAG, "Entering drag mode with visual feedback");
                            }
                            
                            // 正确计算位置：初始窗口位置 + 手指移动的偏移量
                            float moveDeltaX = event.getRawX() - startRawX;
                            float moveDeltaY = event.getRawY() - startRawY;
                            
                            int newX = (int) (initialX + moveDeltaX);
                            int newY = (int) (initialY + moveDeltaY);
                            
                            // 添加边界检查，确保眼睛按钮始终可访问
                            newX = constrainX(newX);
                            newY = constrainY(newY);
                            
                            currentX = newX;
                            currentY = newY;
                            
                            layoutParams.x = newX;
                            layoutParams.y = newY;
                            
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
                        
                        // 恢复原始颜色（无论是否在拖动）
                        setDragModeVisual(false);
                        
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
            mainApplication.setWinVisible(false);
        } else {
            // 显示所有浮动图片
            Log.d(TAG, "Showing all floating windows");
            ManageMethods.setAllWindowsVisible(getContext(), true);
            mainApplication.setWinVisible(true);
        }
        
        // Note: setAllWindowsVisible will call updateToggleButtonIcon which will update our icon
        // No need to manually set the icon here as it will be updated by the system
    }

    private int constrainX(int x) {
        // 获取屏幕宽度
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int buttonWidth = layoutParams.width;
        
        // 确保按钮不会超出屏幕左右边界
        if (x < 0) {
            return 0;
        }
        if (x + buttonWidth > screenWidth) {
            return screenWidth - buttonWidth;
        }
        return x;
    }
    
    private int constrainY(int y) {
        // 获取屏幕高度和状态栏高度
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int buttonHeight = layoutParams.height;
        
        // 获取状态栏高度
        int statusBarHeight = getStatusBarHeight();
        
        // 确保按钮不会被状态栏遮挡，也不会超出屏幕底部
        int minY = statusBarHeight + 20; // 状态栏高度 + 20px的安全边距
        int maxY = screenHeight - buttonHeight - 100; // 屏幕底部 - 按钮高度 - 导航栏预留空间
        
        if (y < minY) {
            Log.d(TAG, "Constraining Y from " + y + " to " + minY + " (status bar protection)");
            return minY;
        }
        if (y > maxY) {
            Log.d(TAG, "Constraining Y from " + y + " to " + maxY + " (bottom boundary)");
            return maxY;
        }
        return y;
    }
    
    private int getStatusBarHeight() {
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getContext().getResources().getDimensionPixelSize(resourceId);
        }
        // 如果无法获取，使用默认值 (24dp)
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (24 * density);
    }

    private void savePosition() {
        // 保存位置前也进行边界检查
        int constrainedX = constrainX((int) currentX);
        int constrainedY = constrainY((int) currentY);
        
        if (constrainedX != currentX || constrainedY != currentY) {
            // 如果位置被调整了，更新当前位置和窗口位置
            currentX = constrainedX;
            currentY = constrainedY;
            layoutParams.x = constrainedX;
            layoutParams.y = constrainedY;
            
            try {
                windowManager.updateViewLayout(this, layoutParams);
            } catch (Exception e) {
                Log.e(TAG, "Error updating layout after position constraint", e);
            }
        }
        
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(PREF_POS_X, currentX);
        editor.putFloat(PREF_POS_Y, currentY);
        editor.apply();
        
        Log.d(TAG, "Saved eye button position: (" + currentX + ", " + currentY + ")");
    }

    public void show() {
        try {
            if (getParent() == null) {
                Log.d(TAG, "Adding FloatEyeButton to window manager at position (" + layoutParams.x + ", " + layoutParams.y + ")");
                windowManager.addView(this, layoutParams);
                Log.d(TAG, "FloatEyeButton successfully added to window manager");
            } else {
                Log.d(TAG, "FloatEyeButton already has a parent, attempting to bring to front");
                // Eye button is already added, bring it to front
                bringToFront();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding FloatEyeButton to window manager", e);
            e.printStackTrace();
        }
    }
    
    public void bringToFront() {
        try {
            if (getParent() != null) {
                Log.d(TAG, "Bringing FloatEyeButton to front by removing and re-adding");
                // Remove and re-add to ensure it's on the absolute top layer
                windowManager.removeView(this);
                windowManager.addView(this, layoutParams);
                Log.d(TAG, "FloatEyeButton successfully brought to front");
            } else {
                Log.d(TAG, "FloatEyeButton not attached, showing it now");
                show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error bringing FloatEyeButton to front", e);
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
    
    private void setDragModeVisual(boolean dragMode) {
        if (dragMode) {
            // 进入拖动模式：设置灰色滤镜
            setColorFilter(0x80808080); // 半透明灰色滤镜
            setAlpha(0.7f); // 降低透明度
            Log.d(TAG, "Enabled drag mode visual (gray tint)");
        } else {
            // 退出拖动模式：清除滤镜，恢复原始颜色
            clearColorFilter();
            setAlpha(1.0f); // 恢复完全不透明
            Log.d(TAG, "Disabled drag mode visual (restored original color)");
        }
    }
}