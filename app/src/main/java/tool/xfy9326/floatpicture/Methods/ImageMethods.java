package tool.xfy9326.floatpicture.Methods;

import static android.graphics.Bitmap.createBitmap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.DisplayMetrics;

import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;
import tool.xfy9326.floatpicture.View.FloatImageView;

public class ImageMethods {

    // Removed obsolete file-based methods - now using URI-based storage directly

    public static String setNewImage(Context mContext, Uri uri) {
        try {
            if (uri == null) {
                android.util.Log.e("FloatPicture", "setNewImage: uri is null");
                return null;
            }
            
            // Generate a unique ID for this image
            String id = System.currentTimeMillis() + "-" + uri.toString().hashCode();
            android.util.Log.d("FloatPicture", "Generated picture ID: " + id + " for URI: " + uri);
            
            // Load bitmap from URI and save to internal storage
            Bitmap bitmap = getNewBitmap(mContext, uri);
            if (bitmap == null) {
                android.util.Log.e("FloatPicture", "setNewImage: failed to load bitmap from uri");
                return null;
            }
            android.util.Log.d("FloatPicture", "Loaded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            // Initialize paths
            Config.initializePaths(mContext);
            
            // Save bitmap to internal storage files
            String imagePath = Config.DEFAULT_PICTURE_DIR + id + ".webp";
            String tempPath = Config.DEFAULT_PICTURE_TEMP_DIR + id + "_temp.webp";
            
            try {
                // Create directories if they don't exist
                File imageDir = new File(Config.DEFAULT_PICTURE_DIR);
                File tempDir = new File(Config.DEFAULT_PICTURE_TEMP_DIR);
                if (!imageDir.exists()) imageDir.mkdirs();
                if (!tempDir.exists()) tempDir.mkdirs();
                
                // Save bitmap to files
                IOMethods.saveBitmap(bitmap, 80, imagePath);
                IOMethods.saveBitmap(bitmap, 80, tempPath);
                
                android.util.Log.d("FloatPicture", "Saved bitmap to: " + imagePath);
                android.util.Log.d("FloatPicture", "Saved temp bitmap to: " + tempPath);
                
                return id;
            } catch (Exception e) {
                android.util.Log.e("FloatPicture", "Failed to save bitmap files", e);
                return null;
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "setNewImage: Exception occurred", e);
            e.printStackTrace();
            return null;
        }
    }

    public static void saveFloatImageViewById(Context mContext, String id, FloatImageView FloatImageView) {
        MainApplication mainApplication = (MainApplication) mContext.getApplicationContext();
        mainApplication.registerView(id, FloatImageView);
    }

    public static FloatImageView getFloatImageViewById(Context mContext, String id) {
        MainApplication mainApplication = (MainApplication) mContext.getApplicationContext();
        return (FloatImageView) mainApplication.getRegisteredView(id);
    }

    public static FloatImageView createPictureView(Context mContext, Bitmap bitmap, boolean touchable, boolean overLayout, float zoom, float degree) {
        FloatImageView imageView = new FloatImageView(mContext);
        // setMoveable removed - fullscreen pictures don't need positioning
        imageView.setOverLayout(overLayout);
        
        // Create fullscreen scaled bitmap that matches screen dimensions exactly
        Bitmap fullscreenBitmap = createFullscreenBitmap(mContext, bitmap, degree);
        imageView.setImageBitmap(fullscreenBitmap);
        
        // Use FIT_XY to scale image to fill exactly
        imageView.setScaleType(android.widget.ImageView.ScaleType.FIT_XY);
        
        // Make the background black to ensure image is visible (no transparency)
        imageView.setBackgroundColor(0xFF000000);
        
        android.util.Log.d("FloatPicture", "Created fullscreen image view with bitmap: " 
            + fullscreenBitmap.getWidth() + "x" + fullscreenBitmap.getHeight());
        
        // Enable system UI hiding for fullscreen immersive experience
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            imageView.setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        
        android.util.Log.d("FloatPicture", "Created FloatImageView with fullscreen system UI hiding");
        return imageView;
    }
    
    // Create a bitmap scaled to exactly match screen dimensions for fullscreen display
    private static Bitmap createFullscreenBitmap(Context mContext, Bitmap originalBitmap, float degree) {
        try {
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            android.util.Log.d("FloatPicture", "Screen dimensions: " + screenWidth + "x" + screenHeight);
            android.util.Log.d("FloatPicture", "Original bitmap: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
            
            // First apply rotation if needed
            Bitmap rotatedBitmap = originalBitmap;
            if (degree != -1 && degree != 0) {
                Matrix rotationMatrix = new Matrix();
                rotationMatrix.postRotate(degree);
                rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, 
                    originalBitmap.getWidth(), originalBitmap.getHeight(), rotationMatrix, true);
                android.util.Log.d("FloatPicture", "Applied rotation: " + degree + " degrees");
            }
            
            // Create opaque bitmap with black background - no transparency allowed
            Bitmap opaqueBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);  // RGB_565 has no alpha channel
            Canvas canvas = new Canvas(opaqueBitmap);
            
            // Fill with black background to ensure no transparency
            canvas.drawColor(0xFF000000);  // Solid black background
            
            // Draw the rotated bitmap scaled to fill the screen
            android.graphics.Rect srcRect = new android.graphics.Rect(0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            android.graphics.Rect dstRect = new android.graphics.Rect(0, 0, screenWidth, screenHeight);
            
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setFilterBitmap(true);
            canvas.drawBitmap(rotatedBitmap, srcRect, dstRect, paint);
            
            // Clean up intermediate bitmap if rotation was applied
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle();
            }
            
            android.util.Log.d("FloatPicture", "Created opaque fullscreen bitmap: " 
                + opaqueBitmap.getWidth() + "x" + opaqueBitmap.getHeight());
            return opaqueBitmap;
            
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error creating fullscreen bitmap", e);
            return originalBitmap;
        }
    }

    public static Bitmap getEditBitmap(Context mContext, Bitmap bitmap) {
        return getEditBitmap(mContext, bitmap.getWidth(), bitmap.getHeight());
    }

    private static Bitmap getEditBitmap(Context mContext, int width, int height) {
        Bitmap transparent_bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transparent_bitmap);
        canvas.drawColor(mContext.getResources().getColor(R.color.colorImageViewEditBackground));
        return transparent_bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, float zoom, float degree) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        boolean needTransform = false;
        
        if (zoom != 0 && zoom != 1.0f) {
            matrix.postScale(zoom, zoom);
            needTransform = true;
        }
        if (degree != -1 && degree != 0) {
            matrix.postRotate(degree);
            needTransform = true;
        }
        
        if (!needTransform) {
            return bitmap;
        }
        
        synchronized (ImageMethods.class) {
            try {
                Bitmap resized = createBitmap(bitmap, 0, 0, width, height, matrix, true);
                // Don't recycle the original bitmap automatically - let caller manage lifecycle
                android.util.Log.d("FloatPicture", "Resized bitmap: " + width + "x" + height + " -> " + resized.getWidth() + "x" + resized.getHeight());
                return resized;
            } catch (Exception e) {
                android.util.Log.e("FloatPicture", "Error resizing bitmap", e);
                return bitmap; // Return original if resize fails
            }
        }
    }

    private static Bitmap getNewBitmap(Context mContext, Uri uri) {
        int degree = 0;
        Bitmap bitmap = IOMethods.readImageByUri(mContext, uri);
        if (bitmap != null) {
            try {
                ContentResolver contentResolver = mContext.getContentResolver();
                ExifInterface exifInterface = new ExifInterface(Objects.requireNonNull(contentResolver.openAssetFileDescriptor(uri, "r")).createInputStream());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90;
                    case ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180;
                    case ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270;
                }
                if (degree != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(degree);
                    Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    // Don't recycle the original bitmap here - let it be managed properly
                    // The rotated bitmap will be used and managed by the caller
                    bitmap = rotated;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static float getDefaultZoom(Context mContext, Bitmap bitmap, boolean isMax) {
        float image_width = bitmap.getWidth();
        float image_height = bitmap.getHeight();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float screen_width;
        float screen_height;
        if (isMax) {
            screen_width = displayMetrics.widthPixels;
            screen_height = displayMetrics.heightPixels;
        } else {
            screen_width = displayMetrics.widthPixels / 3.0f;
            screen_height = displayMetrics.heightPixels / 3.0f;
        }
        if (image_height <= image_width) {
            if (image_height > screen_height || isMax) {
                return ((float) Math.round((screen_height / image_height) * 100f)) / 100f;
            }
        } else {
            if (image_width > screen_width || isMax) {
                return ((float) Math.round((screen_width / image_width) * 100f)) / 100f;
            }
        }
        return 1;
    }

    public static Bitmap getPreviewBitmap(Context mContext, String id) {
        try {
            android.util.Log.d("FloatPicture", "getPreviewBitmap called for ID: " + id);
            
            // Initialize paths
            Config.initializePaths(mContext);
            
            // Try loading from internal storage file  
            String imagePath = Config.DEFAULT_PICTURE_DIR + id + ".webp";
            File imageFile = new File(imagePath);
            
            if (imageFile.exists()) {
                android.util.Log.d("FloatPicture", "Loading preview bitmap from file: " + imagePath);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    // Create a smaller preview version
                    float scale = Math.min(100.0f / bitmap.getWidth(), 100.0f / bitmap.getHeight());
                    if (scale < 1.0f) {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 
                            Math.round(bitmap.getWidth() * scale), 
                            Math.round(bitmap.getHeight() * scale), true);
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                    android.util.Log.d("FloatPicture", "Successfully created preview bitmap");
                    return bitmap;
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode preview bitmap from file: " + imagePath);
                }
            } else {
                android.util.Log.e("FloatPicture", "Preview image file does not exist: " + imagePath);
            }
            
            // Fallback to default placeholder
            android.util.Log.d("FloatPicture", "Using default placeholder for preview");
            return getEditBitmap(mContext, 100, 100);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getPreviewBitmap: Exception occurred", e);
            return getEditBitmap(mContext, 100, 100);
        }
    }

    public static Bitmap getShowBitmap(Context mContext, String id) {
        try {
            android.util.Log.d("FloatPicture", "getShowBitmap called for ID: " + id);
            
            // Initialize paths
            Config.initializePaths(mContext);
            
            // Try loading from internal storage file
            String imagePath = Config.DEFAULT_PICTURE_DIR + id + ".webp";
            File imageFile = new File(imagePath);
            
            if (imageFile.exists()) {
                android.util.Log.d("FloatPicture", "Loading bitmap from file: " + imagePath);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully loaded bitmap from file: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    return bitmap;
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode bitmap from file: " + imagePath);
                }
            } else {
                android.util.Log.e("FloatPicture", "Image file does not exist: " + imagePath);
            }
            
            // Fallback to default placeholder
            android.util.Log.e("FloatPicture", "Using default placeholder bitmap for ID: " + id);
            return getEditBitmap(mContext, 100, 100);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getShowBitmap: Exception occurred for ID: " + id, e);
            return getEditBitmap(mContext, 100, 100);
        }
    }

    public static boolean isPictureFileExist(String id) {
        try {
            // Use static fallback if context not available for path initialization
            String imagePath = (Config.DEFAULT_PICTURE_DIR != null ? Config.DEFAULT_PICTURE_DIR : "/data/user/0/tool.xfy9326.floatpicture/files/Pictures/") + id + ".webp";
            File imageFile = new File(imagePath);
            boolean exists = imageFile.exists();
            
            android.util.Log.d("FloatPicture", "Picture file exists: " + exists + " for path: " + imagePath + " (ID: " + id + ")");
            return exists;
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "isPictureFileExist: Exception occurred", e);
            return false;
        }
    }

    public static void clearAllTemp(Context mContext, String id) {
        // Unregister the view from MainApplication
        MainApplication mainApplication = (MainApplication) mContext.getApplicationContext();
        mainApplication.unregisterView(id);
        
        // Delete image files
        try {
            // Initialize paths
            Config.initializePaths(mContext);
            
            String imagePath = Config.DEFAULT_PICTURE_DIR + id + ".webp";
            String tempPath = Config.DEFAULT_PICTURE_TEMP_DIR + id + "_temp.webp";
            
            File imageFile = new File(imagePath);
            File tempFile = new File(tempPath);
            
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                android.util.Log.d("FloatPicture", "Deleted image file: " + imagePath + " (success: " + deleted + ")");
            }
            
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                android.util.Log.d("FloatPicture", "Deleted temp file: " + tempPath + " (success: " + deleted + ")");
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error deleting files for ID: " + id, e);
        }
        
        android.util.Log.d("FloatPicture", "Cleared all data for ID: " + id);
    }
}
