package tool.xfy9326.floatpicture.Methods;

import static android.graphics.Bitmap.createBitmap;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
            
            // Test if we can load the bitmap from URI to validate it
            Bitmap testBitmap = getNewBitmap(mContext, uri);
            if (testBitmap == null) {
                android.util.Log.e("FloatPicture", "setNewImage: failed to load bitmap from uri");
                return null;
            }
            android.util.Log.d("FloatPicture", "Validated bitmap: " + testBitmap.getWidth() + "x" + testBitmap.getHeight());
            testBitmap.recycle(); // Clean up test bitmap
            
            android.util.Log.d("FloatPicture", "Image URI registered successfully with ID: " + id);
            return id;
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
        imageView.setMoveable(touchable);
        imageView.setOverLayout(overLayout);
        imageView.setImageBitmap(resizeBitmap(bitmap, zoom, degree));
        imageView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        imageView.getBackground().setAlpha(0);
        return imageView;
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
            Bitmap resized = createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (resized != bitmap) {
                bitmap.recycle();
            }
            return resized;
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
                    bitmap.recycle();
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
            // Get picture data to retrieve URI path
            PictureData pictureData = new PictureData();
            pictureData.setDataControl(id);
            String uriPath = pictureData.getString(Config.DATA_PICTURE_URI_PATH, null);
            
            if (uriPath != null) {
                android.util.Log.d("FloatPicture", "Loading preview bitmap from URI: " + uriPath + " for ID: " + id);
                Uri uri = Uri.parse(uriPath);
                Bitmap bitmap = getNewBitmap(mContext, uri);
                if (bitmap != null) {
                    // Create a smaller preview version
                    float scale = Math.min(50.0f / bitmap.getWidth(), 50.0f / bitmap.getHeight());
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
                    android.util.Log.e("FloatPicture", "Failed to load preview bitmap from URI: " + uriPath);
                }
            } else {
                android.util.Log.e("FloatPicture", "No URI path found for preview of ID: " + id);
            }
            
            // Fallback to default placeholder
            android.util.Log.d("FloatPicture", "Using default placeholder for preview");
            return getEditBitmap(mContext, 50, 50);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getPreviewBitmap: Exception occurred", e);
            return getEditBitmap(mContext, 50, 50);
        }
    }

    public static Bitmap getShowBitmap(Context mContext, String id) {
        try {
            // Get picture data to retrieve URI path
            PictureData pictureData = new PictureData();
            pictureData.setDataControl(id);
            String uriPath = pictureData.getString(Config.DATA_PICTURE_URI_PATH, null);
            
            if (uriPath != null) {
                android.util.Log.d("FloatPicture", "Loading bitmap from URI: " + uriPath + " for ID: " + id);
                Uri uri = Uri.parse(uriPath);
                Bitmap bitmap = getNewBitmap(mContext, uri);
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully loaded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    return bitmap;
                } else {
                    android.util.Log.e("FloatPicture", "Failed to load bitmap from URI: " + uriPath);
                }
            } else {
                android.util.Log.e("FloatPicture", "No URI path found for ID: " + id);
            }
            
            // Fallback to default placeholder
            android.util.Log.d("FloatPicture", "Using default placeholder bitmap");
            return getEditBitmap(mContext, 50, 50);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "getShowBitmap: Exception occurred", e);
            return getEditBitmap(mContext, 50, 50);
        }
    }

    public static boolean isPictureFileExist(String id) {
        try {
            // Get picture data to check if URI path exists
            PictureData pictureData = new PictureData();
            pictureData.setDataControl(id);
            String uriPath = pictureData.getString(Config.DATA_PICTURE_URI_PATH, null);
            
            if (uriPath != null && !uriPath.isEmpty()) {
                android.util.Log.d("FloatPicture", "Picture exists with URI: " + uriPath + " for ID: " + id);
                return true;
            } else {
                android.util.Log.d("FloatPicture", "No URI path found for ID: " + id);
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
        
        // No longer need to delete files since we're using URI-based storage
        // The configuration data will be cleaned up by the caller if needed
        android.util.Log.d("FloatPicture", "Cleared temporary data for ID: " + id);
    }
}
