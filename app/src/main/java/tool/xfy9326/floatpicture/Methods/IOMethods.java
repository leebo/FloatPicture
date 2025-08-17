package tool.xfy9326.floatpicture.Methods;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

import tool.xfy9326.floatpicture.Utils.Config;

public class IOMethods {

    static Bitmap readImageByUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            android.util.Log.d("FloatPicture", "Attempting to read image from URI: " + uri);
            
            // Try taking persistent permission if we don't have it
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                android.util.Log.d("FloatPicture", "Successfully took persistent URI permission for: " + uri);
            } catch (SecurityException e) {
                android.util.Log.w("FloatPicture", "Cannot take persistent URI permission (may already have it): " + uri);
            }
            
            // Check if we have persistent permission and log all permissions
            java.util.List<android.content.UriPermission> allPermissions = contentResolver.getPersistedUriPermissions();
            android.util.Log.d("FloatPicture", "Total persisted URI permissions: " + allPermissions.size());
            for (android.content.UriPermission permission : allPermissions) {
                android.util.Log.d("FloatPicture", "Persisted permission: " + permission.getUri() + 
                    " - Read: " + permission.isReadPermission() + 
                    " - Write: " + permission.isWritePermission());
            }
            
            boolean hasPermission = allPermissions.stream()
                .anyMatch(uriPermission -> uriPermission.getUri().equals(uri) && uriPermission.isReadPermission());
            
            if (hasPermission) {
                android.util.Log.d("FloatPicture", "Found persistent read permission for URI: " + uri);
            } else {
                android.util.Log.w("FloatPicture", "No persistent read permission found for URI: " + uri);
                android.util.Log.w("FloatPicture", "Target URI: " + uri);
                android.util.Log.w("FloatPicture", "Target URI string: " + uri.toString());
            }
            
            // Try opening the URI
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                if (inputStream == null) {
                    android.util.Log.e("FloatPicture", "ContentResolver returned null input stream for URI: " + uri);
                    return null;
                }
                
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    android.util.Log.d("FloatPicture", "Successfully decoded bitmap from URI: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                } else {
                    android.util.Log.e("FloatPicture", "BitmapFactory failed to decode stream for URI: " + uri);
                }
                return bitmap;
            }
        } catch (SecurityException e) {
            android.util.Log.e("FloatPicture", "Security exception accessing URI: " + uri, e);
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Exception reading image from URI: " + uri, e);
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    static void saveBitmap(Bitmap bitmap, int quality, String path) {
        File file = new File(path);
        try {
            // Ensure parent directory exists
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                android.util.Log.d("FloatPicture", "Parent directory created: " + created + " for path: " + parentDir.getAbsolutePath());
            }
            
            // Delete existing file if it exists
            if (file.exists()) {
                boolean deleted = file.delete();
                android.util.Log.d("FloatPicture", "Existing file deleted: " + deleted + " for path: " + path);
            }
            
            // Save the bitmap
            try (OutputStream outputStream = new FileOutputStream(file)) {
                boolean compressed = bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream);
                outputStream.flush();
                android.util.Log.d("FloatPicture", "Bitmap compression result: " + compressed + " for path: " + path);
                android.util.Log.d("FloatPicture", "Successfully saved bitmap to: " + path + " (file size: " + file.length() + " bytes)");
            }
            
        } catch (IOException e) {
            android.util.Log.e("FloatPicture", "Error saving bitmap to: " + path, e);
            e.printStackTrace();
        }
    }

    public static String readAssetText(Context mContext, String path) {
        try {
            StringBuilder result = new StringBuilder();
            InputStreamReader inputReader = new InputStreamReader(mContext.getResources().getAssets().open(path));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean setNoMedia() {
        File nomedia = new File(Config.NO_MEDIA_FILE_DIR);
        if (!nomedia.exists()) {
            try {
                return nomedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    private static boolean createPath(File file) {
        if (Objects.requireNonNull(file.getParent()).trim().length() != 1) {
            File filepath = file.getParentFile();
            if (!Objects.requireNonNull(filepath).exists()) {
                return filepath.mkdirs();
            }
        }
        return true;
    }

    private static boolean CheckFile(File file, boolean delete) throws IOException {
        if (file.exists()) {
            if (file.isFile()) {
                if (delete) {
                    if (file.delete()) {
                        return !file.createNewFile();
                    }
                } else {
                    return false;
                }
            }
        } else {
            if (!createPath(file)) {
                return true;
            }
            return !file.createNewFile();
        }
        return true;
    }

    public static boolean writeFile(String content, String path) {
        File file = new File(path);
        try {
            if (CheckFile(file, false)) {
                return false;
            }
            OutputStream writer = new FileOutputStream(file);
            byte[] Bytes = content.getBytes();
            writer.write(Bytes);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String readFile(String path) {
        File file = new File(path);
        try {
            if (CheckFile(file, false)) {
                return null;
            }
            InputStream file_stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file_stream));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            reader.close();
            file_stream.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
