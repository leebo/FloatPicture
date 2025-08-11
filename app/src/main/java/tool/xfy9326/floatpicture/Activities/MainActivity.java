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
import tool.xfy9326.floatpicture.View.AdvancedRecyclerView;
import tool.xfy9326.floatpicture.View.ManageListAdapter;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.content.Context;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private ManageListAdapter manageListAdapter;
    private long BackClickTime;

    public static void SnackShow(Activity mActivity, int resourceId) {
        CoordinatorLayout coordinatorLayout = mActivity.findViewById(R.id.main_layout_content);
        Snackbar.make(coordinatorLayout, mActivity.getString(resourceId), Snackbar.LENGTH_SHORT).show();
        System.gc();
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
        ManageMethods.createToggleButton(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ManageMethods.removeToggleButton(this);
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

        FloatingActionButton floatingActionButton = findViewById(R.id.main_button_add);
        floatingActionButton.setOnClickListener(view -> ManageMethods.SelectPicture(MainActivity.this));

        // Display screen information
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView screenResolutionTextView = findViewById(R.id.text_screen_resolution);
        TextView screenRatioTextView = findViewById(R.id.text_screen_ratio);
        TextView screenSizeTextView = findViewById(R.id.text_screen_size);

        int screenWidth = sharedPreferences.getInt(Config.PREFERENCE_SCREEN_WIDTH, 0);
        int screenHeight = sharedPreferences.getInt(Config.PREFERENCE_SCREEN_HEIGHT, 0);
        float screenRatio = sharedPreferences.getFloat(Config.PREFERENCE_SCREEN_RATIO, 0.0f);
        float screenSizeInches = sharedPreferences.getFloat(Config.PREFERENCE_SCREEN_SIZE_INCHES, 0.0f);

        screenResolutionTextView.setText(getString(R.string.screen_resolution_format, screenWidth, screenHeight));
        screenRatioTextView.setText(getString(R.string.screen_ratio_format, String.format("%.2f", screenRatio)));
        screenSizeTextView.setText(getString(R.string.screen_size_format, String.format("%.2f", screenSizeInches)));

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
            }
        } else if (requestCode == Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_CHANGE) {
            if (data != null) {
                int position = data.getIntExtra(Config.INTENT_PICTURE_EDIT_POSITION, -1);
                if (position >= 0) {
                    manageListAdapter.updateData();
                    manageListAdapter.notifyItemChanged(position);
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
