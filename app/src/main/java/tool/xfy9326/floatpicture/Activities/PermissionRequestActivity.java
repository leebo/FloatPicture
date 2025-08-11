package tool.xfy9326.floatpicture.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import tool.xfy9326.floatpicture.Methods.PermissionMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;

public class PermissionRequestActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1002;

    private TextView tvPermissionStatus;
    private Button btnGrantOverlay;
    private Button btnGrantStorage;
    private Button btnEnterApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);

        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        btnGrantOverlay = findViewById(R.id.btn_grant_overlay);
        btnGrantStorage = findViewById(R.id.btn_grant_storage);
        btnEnterApp = findViewById(R.id.btn_enter_app);

        btnGrantOverlay.setOnClickListener(v -> requestOverlayPermission());
        btnGrantStorage.setOnClickListener(v -> requestStoragePermission());
        btnEnterApp.setOnClickListener(v -> checkAndEnterApp());

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions() {
        boolean hasOverlayPermission = checkOverlayPermission();
        boolean hasStoragePermission = checkStoragePermission();

        if (hasOverlayPermission && hasStoragePermission) {
            tvPermissionStatus.setText(R.string.permission_status_all_granted);
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            btnGrantOverlay.setVisibility(View.GONE);
            btnGrantStorage.setVisibility(View.GONE);
            btnEnterApp.setVisibility(View.VISIBLE);
        } else {
            tvPermissionStatus.setText(R.string.permission_status_missing);
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnEnterApp.setVisibility(View.GONE);

            if (!hasOverlayPermission) {
                btnGrantOverlay.setVisibility(View.VISIBLE);
            } else {
                btnGrantOverlay.setVisibility(View.GONE);
            }

            if (!hasStoragePermission) {
                btnGrantStorage.setVisibility(View.VISIBLE);
            } else {
                btnGrantStorage.setVisibility(View.GONE);
            }
        }
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true; // Permission not required on older Android versions
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        } else {
            Toast.makeText(this, R.string.permission_overlay_already_granted, Toast.LENGTH_SHORT).show();
            checkPermissions();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(this, PermissionMethods.StoragePermission[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                Toast.makeText(this, R.string.permission_storage_already_granted, Toast.LENGTH_SHORT).show();
                checkPermissions();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, PermissionMethods.StoragePermission[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, PermissionMethods.StoragePermission[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PermissionMethods.StoragePermission, REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                Toast.makeText(this, R.string.permission_storage_already_granted, Toast.LENGTH_SHORT).show();
                checkPermissions();
            }
        }
    }

    private void checkAndEnterApp() {
        if (checkOverlayPermission() && checkStoragePermission()) {
            Intent intent = new Intent(PermissionRequestActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity so user can't go back to it
        } else {
            Toast.makeText(this, R.string.permission_grant_all_required, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            checkPermissions();
        }
    }
}
