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
import java.io.FileOutputStream;
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
            String id = System.currentTimeMillis() + "-" + Math.abs(uri.toString().hashCode());
            android.util.Log.d("FloatPicture", "Generated picture ID: " + id + " for URI: " + uri);
            
            // Load bitmap from URI
            Bitmap bitmap = getNewBitmap(mContext, uri);
            if (bitmap == null) {
                android.util.Log.e("FloatPicture", "setNewImage: failed to load bitmap from uri");
                return null;
            }
            android.util.Log.d("FloatPicture", "Loaded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            // Initialize paths
            Config.initializePaths(mContext);
            
            // Enhanced save logic with retry mechanism
            // Try main save multiple times with delays
            boolean mainSaved = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                android.util.Log.d("FloatPicture", "Main save attempt " + attempt + "/3");
                mainSaved = saveBitmapToInternalStorage(mContext, bitmap, id + ".webp", false);
                if (mainSaved) {
                    android.util.Log.d("FloatPicture", "Main save succeeded on attempt " + attempt);
                    break;
                } else {
                    android.util.Log.w("FloatPicture", "Main save failed on attempt " + attempt);
                    if (attempt < 3) {
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                }
            }
            
            // Try temp save (for backward compatibility)
            boolean tempSaved = saveBitmapToInternalStorage(mContext, bitmap, id + "_temp.webp", true);
            
            // At minimum, main save must succeed
            if (mainSaved) {
                android.util.Log.d("FloatPicture", "Successfully saved main bitmap for ID: " + id + " (temp: " + tempSaved + ")");
                return id;
            } else {
                android.util.Log.e("FloatPicture", "Critical: Main save failed after 3 attempts - main: " + mainSaved + ", temp: " + tempSaved);
                // Clean up temp file if it was created but main failed
                if (tempSaved) {
                    try {
                        File tempFile = new File(mContext.getFilesDir(), "Pictures/.TEMP/" + id + "_temp.webp");
                        if (tempFile.exists()) {
                            tempFile.delete();
                            android.util.Log.d("FloatPicture", "Cleaned up temp file after main save failure");
                        }
                    } catch (Exception e) {
                        android.util.Log.w("FloatPicture", "Failed to clean up temp file", e);
                    }
                }
                return null;
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "setNewImage: Exception occurred", e);
            e.printStackTrace();
            return null;
        }
    }
    
    private static boolean saveBitmapToInternalStorage(Context context, Bitmap bitmap, String filename, boolean isTemp) {
        try {
            android.util.Log.d("FloatPicture", "=== SAVE BITMAP START === filename: " + filename + ", isTemp: " + isTemp);
            
            // Create the Pictures directory in internal storage
            File picturesDir = new File(context.getFilesDir(), "Pictures");
            android.util.Log.d("FloatPicture", "Pictures dir path: " + picturesDir.getAbsolutePath());
            if (!picturesDir.exists()) {
                boolean created = picturesDir.mkdirs();
                android.util.Log.d("FloatPicture", "Pictures directory created: " + created);
            } else {
                android.util.Log.d("FloatPicture", "Pictures directory already exists");
            }
            
            File targetDir = picturesDir;
            if (isTemp) {
                targetDir = new File(picturesDir, ".TEMP");
                android.util.Log.d("FloatPicture", "Temp target dir: " + targetDir.getAbsolutePath());
                if (!targetDir.exists()) {
                    boolean created = targetDir.mkdirs();
                    android.util.Log.d("FloatPicture", "Temp directory created: " + created);
                } else {
                    android.util.Log.d("FloatPicture", "Temp directory already exists");
                }
            } else {
                android.util.Log.d("FloatPicture", "Main target dir: " + targetDir.getAbsolutePath());
            }
            
            File imageFile = new File(targetDir, filename);
            android.util.Log.d("FloatPicture", "Final image file path: " + imageFile.getAbsolutePath());
            
            // Delete existing file if it exists
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                android.util.Log.d("FloatPicture", "Existing file deleted: " + deleted);
            }
            
            // Save bitmap to file
            android.util.Log.d("FloatPicture", "About to compress bitmap - format: WEBP, quality: 80, bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                android.util.Log.d("FloatPicture", "FileOutputStream created successfully for: " + imageFile.getAbsolutePath());
                
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.WEBP, 80, fos);
                android.util.Log.d("FloatPicture", "Bitmap compress result: " + compressed);
                
                fos.flush();
                android.util.Log.d("FloatPicture", "FileOutputStream flushed");
                
                // Verify file was created and has content
                boolean exists = imageFile.exists();
                long fileSize = exists ? imageFile.length() : 0;
                boolean success = compressed && exists && fileSize > 0;
                
                android.util.Log.d("FloatPicture", "Saved bitmap to: " + imageFile.getAbsolutePath() + 
                    " (compressed: " + compressed + ", exists: " + exists + ", size: " + fileSize + " bytes, success: " + success + ")");
                
                // Additional verification - try to read back the file
                if (success) {
                    try {
                        android.util.Log.d("FloatPicture", "Starting verification read-back...");
                        android.graphics.Bitmap testBitmap = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        if (testBitmap != null) {
                            android.util.Log.d("FloatPicture", "Verification: Successfully read back bitmap " + testBitmap.getWidth() + "x" + testBitmap.getHeight());
                            testBitmap.recycle();
                        } else {
                            android.util.Log.e("FloatPicture", "Verification: Failed to read back bitmap from " + imageFile.getAbsolutePath());
                            success = false;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FloatPicture", "Verification: Exception reading back bitmap", e);
                        success = false;
                    }
                } else {
                    android.util.Log.e("FloatPicture", "Initial save failed - skipping verification");
                }
                
                android.util.Log.d("FloatPicture", "=== SAVE BITMAP END === result: " + success);
                return success;
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error saving bitmap to internal storage: " + filename, e);
            return false;
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
        
        // Force full opacity at creation time
        imageView.setAlpha(1.0f);
        
        android.util.Log.d("FloatPicture", "Created fullscreen image view with bitmap: " 
            + fullscreenBitmap.getWidth() + "x" + fullscreenBitmap.getHeight() + " (forced alpha=1.0)");
        
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
            
            // Create completely opaque bitmap - force RGB_565 format with no alpha channel
            Bitmap opaqueBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);  // RGB_565 has no alpha channel
            Canvas canvas = new Canvas(opaqueBitmap);
            
            // Fill entire canvas with solid black to ensure complete opacity
            canvas.drawColor(0xFF000000);  // Solid black background
            
            // Force paint to be completely opaque
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(0xFF000000);  // Solid black
            backgroundPaint.setAlpha(255);  // Maximum opacity
            canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
            
            // Draw the rotated bitmap scaled to fill the screen with forced opacity
            android.graphics.Rect srcRect = new android.graphics.Rect(0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            android.graphics.Rect dstRect = new android.graphics.Rect(0, 0, screenWidth, screenHeight);
            
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setFilterBitmap(true);
            paint.setAlpha(255);  // Force maximum opacity
            paint.setXfermode(null);  // Use default blending mode
            canvas.drawBitmap(rotatedBitmap, srcRect, dstRect, paint);
            
            // Additional security: draw another black layer on top if needed
            Paint securityPaint = new Paint();
            securityPaint.setColor(0xFF000000);
            securityPaint.setAlpha(255);
            securityPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));
            canvas.drawRect(0, 0, screenWidth, screenHeight, securityPaint);
            
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
            
            // Try to load from main location first
            File imageFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + id + ".webp");
            
            if (imageFile.exists()) {
                android.util.Log.d("FloatPicture", "Loading preview bitmap from main: " + imageFile.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    return createPreviewFromBitmap(bitmap);
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode preview bitmap from main: " + imageFile.getAbsolutePath());
                }
            } else {
                android.util.Log.w("FloatPicture", "Preview main image file does not exist: " + imageFile.getAbsolutePath());
            }
            
            // Fallback: try to load from temp location
            File tempFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + ".TEMP" + File.separator + id + "_temp.webp");
            if (tempFile.exists()) {
                android.util.Log.d("FloatPicture", "Fallback: Loading preview from temp: " + tempFile.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully loaded preview from temp");
                    return createPreviewFromBitmap(bitmap);
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode preview bitmap from temp: " + tempFile.getAbsolutePath());
                }
            } else {
                android.util.Log.w("FloatPicture", "Preview temp image file also does not exist: " + tempFile.getAbsolutePath());
            }
            
            // Fallback to default placeholder
            android.util.Log.d("FloatPicture", "Using default placeholder for preview");
            return getEditBitmap(mContext, 100, 100);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getPreviewBitmap: Exception occurred", e);
            return getEditBitmap(mContext, 100, 100);
        }
    }
    
    private static Bitmap createPreviewFromBitmap(Bitmap bitmap) {
        try {
            // Create a smaller preview version
            float scale = Math.min(100.0f / bitmap.getWidth(), 100.0f / bitmap.getHeight());
            if (scale < 1.0f) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 
                    Math.round(bitmap.getWidth() * scale), 
                    Math.round(bitmap.getHeight() * scale), true);
                bitmap.recycle();
                android.util.Log.d("FloatPicture", "Successfully created scaled preview bitmap");
                return scaledBitmap;
            } else {
                android.util.Log.d("FloatPicture", "Using original bitmap as preview (already small enough)");
                return bitmap;
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error creating preview from bitmap", e);
            bitmap.recycle();
            return null;
        }
    }

    public static Bitmap getShowBitmap(Context mContext, String id) {
        try {
            android.util.Log.d("FloatPicture", "getShowBitmap called for ID: " + id);
            
            // Try to load from main location first
            File imageFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + id + ".webp");
            
            if (imageFile.exists()) {
                android.util.Log.d("FloatPicture", "Loading bitmap from main location: " + imageFile.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully loaded bitmap from main: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    return bitmap;
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode bitmap from main location: " + imageFile.getAbsolutePath());
                }
            } else {
                android.util.Log.w("FloatPicture", "Main image file does not exist: " + imageFile.getAbsolutePath());
            }
            
            // Fallback: try to load from temp location
            File tempFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + ".TEMP" + File.separator + id + "_temp.webp");
            if (tempFile.exists()) {
                android.util.Log.d("FloatPicture", "Fallback: Loading bitmap from temp location: " + tempFile.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully loaded bitmap from temp: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    
                    // Try to recreate main file from temp file
                    try {
                        android.util.Log.d("FloatPicture", "Attempting to recreate main file from temp...");
                        boolean recreated = saveBitmapToInternalStorage(mContext, bitmap, id + ".webp", false);
                        if (recreated) {
                            android.util.Log.d("FloatPicture", "Successfully recreated main file from temp");
                        } else {
                            android.util.Log.w("FloatPicture", "Failed to recreate main file from temp");
                        }
                    } catch (Exception e) {
                        android.util.Log.w("FloatPicture", "Exception while recreating main file", e);
                    }
                    
                    return bitmap;
                } else {
                    android.util.Log.e("FloatPicture", "Failed to decode bitmap from temp location: " + tempFile.getAbsolutePath());
                }
            } else {
                android.util.Log.w("FloatPicture", "Temp image file also does not exist: " + tempFile.getAbsolutePath());
            }
            
            // Last resort: default placeholder
            android.util.Log.e("FloatPicture", "Using default placeholder bitmap for ID: " + id);
            return getEditBitmap(mContext, 100, 100);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getShowBitmap: Exception occurred for ID: " + id, e);
            return getEditBitmap(mContext, 100, 100);
        }
    }

    public static boolean isPictureFileExist(String id) {
        return isPictureFileExist(null, id);
    }

    public static boolean isPictureFileExist(Context mContext, String id) {
        try {
            if (mContext != null) {
                // Check main file first
                File imageFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + id + ".webp");
                boolean mainExists = imageFile.exists();
                android.util.Log.d("FloatPicture", "Main picture file exists: " + mainExists + " for path: " + imageFile.getAbsolutePath() + " (ID: " + id + ")");
                
                if (mainExists) {
                    return true;
                }
                
                // Fallback: check temp file
                File tempFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + ".TEMP" + File.separator + id + "_temp.webp");
                boolean tempExists = tempFile.exists();
                android.util.Log.d("FloatPicture", "Temp picture file exists: " + tempExists + " for path: " + tempFile.getAbsolutePath() + " (ID: " + id + ")");
                
                return tempExists;
            } else {
                // Fallback when context not available - try common internal storage paths
                String[] mainPaths = {
                    "/data/user/0/tool.xfy9326.floatpicture/files/Pictures/" + id + ".webp",
                    "/data/data/tool.xfy9326.floatpicture/files/Pictures/" + id + ".webp"
                };
                
                String[] tempPaths = {
                    "/data/user/0/tool.xfy9326.floatpicture/files/Pictures/.TEMP/" + id + "_temp.webp",
                    "/data/data/tool.xfy9326.floatpicture/files/Pictures/.TEMP/" + id + "_temp.webp"
                };
                
                // Check main files first
                for (String path : mainPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        android.util.Log.d("FloatPicture", "Found main picture file at fallback path: " + path);
                        return true;
                    }
                }
                
                // Check temp files as fallback
                for (String path : tempPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        android.util.Log.d("FloatPicture", "Found temp picture file at fallback path: " + path);
                        return true;
                    }
                }
                
                android.util.Log.e("FloatPicture", "No picture file (main or temp) found for ID: " + id + " at any fallback paths");
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "isPictureFileExist: Exception occurred", e);
            return false;
        }
    }

    public static void clearAllTemp(Context mContext, String id) {
        // Unregister the view from MainApplication
        MainApplication mainApplication = (MainApplication) mContext.getApplicationContext();
        mainApplication.unregisterView(id);
        
        // Delete image files from internal storage
        try {
            File imageFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + id + ".webp");
            File tempFile = new File(mContext.getFilesDir(), "Pictures" + File.separator + ".TEMP" + File.separator + id + "_temp.webp");
            
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                android.util.Log.d("FloatPicture", "Deleted image file: " + imageFile.getAbsolutePath() + " (success: " + deleted + ")");
            }
            
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                android.util.Log.d("FloatPicture", "Deleted temp file: " + tempFile.getAbsolutePath() + " (success: " + deleted + ")");
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error deleting files for ID: " + id, e);
        }
        
        android.util.Log.d("FloatPicture", "Cleared all data for ID: " + id);
    }
}
