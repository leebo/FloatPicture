package tool.xfy9326.floatpicture.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.Methods.ApplicationMethods;
import tool.xfy9326.floatpicture.Methods.IOMethods;
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.Methods.PermissionMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;
import tool.xfy9326.floatpicture.View.AdvancedRecyclerView;
import tool.xfy9326.floatpicture.View.ManageListAdapter;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.content.Context;
import android.view.WindowManager;

import com.google.android.material.button.MaterialButton;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ManageListAdapter manageListAdapter;
    private MaterialButton globalToggleButton;
    private long BackClickTime;

    public static void SnackShow(Activity mActivity, int resourceId) {
        CoordinatorLayout coordinatorLayout = mActivity.findViewById(R.id.main_layout_content);
        Snackbar.make(coordinatorLayout, mActivity.getString(resourceId), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
        ApplicationMethods.startNotificationControl(this);
        ApplicationMethods.ClearUselessTemp(this);
    }

    private void init(Bundle savedInstanceState) {
        BackClickTime = System.currentTimeMillis();
        ViewSet();
        MainApplication mainApplication = (MainApplication) getApplicationContext();
        if (mainApplication.isAppInit() || savedInstanceState == null) {
            ManageMethods.RunWin(this);
            mainApplication.setAppInit(true);
            IOMethods.setNoMedia();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGlobalToggleButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void ViewSet() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        manageListAdapter = new ManageListAdapter(this);
        ((MainApplication) getApplicationContext()).setManageListAdapter(manageListAdapter);
        AdvancedRecyclerView recyclerView = findViewById(R.id.main_list_manage);
        recyclerView.setAdapter(manageListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(findViewById(R.id.layout_widget_empty_view));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FloatingActionButton floatingActionButton = findViewById(R.id.main_button_add);
        floatingActionButton.setOnClickListener(view -> ManageMethods.SelectPicture(MainActivity.this));

        // Setup global toggle button
        globalToggleButton = findViewById(R.id.button_global_toggle);
        globalToggleButton.setOnClickListener(view -> toggleGlobalVisibility());
        updateGlobalToggleButton();

        final DrawerLayout drawerLayout = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.main_navigation_view);
        ApplicationMethods.disableNavigationViewScrollbars(navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int itemId = item.getItemId();
            if (itemId == R.id.menu_global_settings) {
                startActivity(new Intent(MainActivity.this, GlobalSettingsActivity.class));
            } else if (itemId == R.id.menu_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            } else if (itemId == R.id.menu_back_to_launcher) {
                MainActivity.this.moveTaskToBack(true);
            } else if (itemId == R.id.menu_exit) {
                ApplicationMethods.CloseApplication(MainActivity.this);
            }
            return false;
        });
    }

    private void toggleGlobalVisibility() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentState = preferences.getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
        boolean newState = !currentState;
        
        android.util.Log.d("FloatPicture", "Toggling global visibility from " + currentState + " to " + newState);
        
        if (newState) {
            // Show floating pictures - activate the currently selected one
            String activeImageId = getActiveImageId();
            if (activeImageId != null) {
                PictureData pictureData = new PictureData();
                pictureData.setDataControl(activeImageId);
                ManageMethods.setWindowVisible(this, pictureData, activeImageId, true);
                android.util.Log.d("FloatPicture", "Showing active image: " + activeImageId);
            } else {
                android.util.Log.w("FloatPicture", "No active image found to show");
            }
        } else {
            // Hide all floating pictures
            ManageMethods.hideAllWindows(this);
            // Update global state after hiding
            preferences.edit().putBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, false).apply();
            android.util.Log.d("FloatPicture", "Hiding all floating windows");
        }
        
        updateGlobalToggleButton();
        manageListAdapter.updateData();
        manageListAdapter.notifyDataSetChanged();
    }
    
    private String getActiveImageId() {
        PictureData tempPictureData = new PictureData();
        LinkedHashMap<String, String> allPictures = tempPictureData.getListArray();
        if (allPictures != null) {
            for (Map.Entry<String, String> entry : allPictures.entrySet()) {
                String imageId = entry.getKey();
                PictureData imagePictureData = new PictureData();
                imagePictureData.setDataControl(imageId);
                if (imagePictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, false)) {
                    return imageId;
                }
            }
        }
        return null;
    }
    
    private boolean hasActiveImages() {
        return getActiveImageId() != null;
    }
    
    public void updateGlobalToggleButton() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean globalState = preferences.getBoolean(Config.PREFERENCE_GLOBAL_VISIBILITY_STATE, Config.DATA_DEFAULT_GLOBAL_VISIBILITY);
        boolean hasActive = hasActiveImages();
        
        if (hasActive) {
            globalToggleButton.setEnabled(true);
            globalToggleButton.setAlpha(1.0f);
            if (globalState) {
                globalToggleButton.setIconResource(R.drawable.ic_visibility);
            } else {
                globalToggleButton.setIconResource(R.drawable.ic_visibility_off);
            }
        } else {
            // No active images - button is disabled/grayed out
            globalToggleButton.setEnabled(false);
            globalToggleButton.setAlpha(0.5f);
            globalToggleButton.setIconResource(R.drawable.ic_visibility_off);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_ADD) {
            manageListAdapter.updateData();
            if (manageListAdapter.getItemCount() != 0) {
                if (manageListAdapter.getItemCount() == 1) {
                    manageListAdapter.notifyDataSetChanged();
                } else {
                    manageListAdapter.notifyItemInserted(manageListAdapter.getItemCount());
                }
                SnackShow(this, R.string.action_add_window);
                ManageMethods.updateNotificationCount(this);
                updateGlobalToggleButton();
            }
        } else if (requestCode == Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_CHANGE) {
            if (data != null) {
                int position = data.getIntExtra(Config.INTENT_PICTURE_EDIT_POSITION, -1);
                if (position >= 0) {
                    manageListAdapter.updateData();
                    manageListAdapter.notifyItemChanged(position);
                    updateGlobalToggleButton();
                }
            }
        } else if (requestCode == Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_GET_PICTURE) {
            if (data != null) {
                Intent intent = new Intent(MainActivity.this, PictureSettingsActivity.class);
                intent.putExtra(Config.INTENT_PICTURE_EDIT_MODE, false);
                intent.setData(data.getData());
                ActivityCompat.startActivityForResult(this, intent, Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_ADD, null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.main_drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        long BackNowClickTime = System.currentTimeMillis();
        if ((BackNowClickTime - BackClickTime) < 2200) {
            MainApplication mainApplication = (MainApplication) getApplicationContext();
            mainApplication.setAppInit(false);
            ApplicationMethods.DoubleClickCloseSnackBar(this, true);
        } else {
            ApplicationMethods.DoubleClickCloseSnackBar(this, false);
            BackClickTime = System.currentTimeMillis();
        }
    }
}
