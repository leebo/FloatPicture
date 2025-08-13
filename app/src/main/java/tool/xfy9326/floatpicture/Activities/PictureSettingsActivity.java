package tool.xfy9326.floatpicture.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.View.PictureSettingsFragment;

public class PictureSettingsActivity extends AppCompatActivity {
    private PictureSettingsFragment mPictureSettingsFragment;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ViewSet();
        fragmentSet(savedInstanceState);
        setBackResult();
    }

    private void ViewSet() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        if (actionBar != null && intent != null) {
            isEditMode = intent.getBooleanExtra(Config.INTENT_PICTURE_EDIT_MODE, false);
            if (!isEditMode) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void fragmentSet(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mPictureSettingsFragment = new PictureSettingsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout_picture_settings_content, mPictureSettingsFragment);
            fragmentTransaction.commit();
        } else {
            mPictureSettingsFragment = (PictureSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.layout_picture_settings_content);
        }
    }

    private void setBackResult() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra(Config.INTENT_PICTURE_EDIT_MODE, false)) {
                Intent result_intent = new Intent();
                result_intent.putExtra(Config.INTENT_PICTURE_EDIT_POSITION, getIntent().getIntExtra(Config.INTENT_PICTURE_EDIT_POSITION, -1));
                setResult(Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_CHANGE, result_intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Settings are auto-saved in real-time, just exit
        try {
            if (mPictureSettingsFragment != null) {
                mPictureSettingsFragment.exit();
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_picture_settings, menu);
        
        // Only show save button for new pictures, not when editing existing ones
        MenuItem saveItem = menu.findItem(R.id.action_save_new_picture);
        if (saveItem != null) {
            saveItem.setVisible(!isEditMode);
        }
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            try {
                if (mPictureSettingsFragment != null) {
                    mPictureSettingsFragment.exit();
                }
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        } else if (itemId == R.id.action_save_new_picture) {
            // Save the new picture and finish the activity (single active image design)
            try {
                if (mPictureSettingsFragment != null) {
                    // Save the new picture configuration
                    mPictureSettingsFragment.saveAllData();
                    
                    // For new pictures, ensure single active state by deactivating other images
                    // and making this new picture the only active one
                    mPictureSettingsFragment.exit();
                }
                setResult(RESULT_OK);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            if (mPictureSettingsFragment != null) {
                mPictureSettingsFragment.clearEditView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
        super.onDestroy();
    }
}
