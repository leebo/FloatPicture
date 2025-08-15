package tool.xfy9326.floatpicture.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import tool.xfy9326.floatpicture.Methods.ImageMethods;
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.Methods.WindowsMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;

public class PictureSettingsFragment extends Fragment {
    private final static String WINDOW_CREATED = "WINDOW_CREATED";
    private boolean Edit_Mode;
    private boolean Window_Created;
    private boolean onUseEditPicture = false;
    private LayoutInflater inflater;
    private PictureData pictureData;
    private String PictureId;
    private String PictureName;
    private WindowManager windowManager;
    private FloatImageView floatImageView;
    private Bitmap bitmap;
    private Bitmap bitmap_Edit;
    private FloatImageView floatImageView_Edit;
    private float default_zoom;
    private float zoom;
    private float zoom_temp;
    private float picture_degree;
    private float picture_degree_temp;
    private int position_x;
    private int position_y;
    private int position_x_temp;
    private int position_y_temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window_Created = false;
        Edit_Mode = false;
        pictureData = new PictureData();
        inflater = LayoutInflater.from(getActivity());
        windowManager = WindowsMethods.getWindowManager(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_settings_realtime, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreData(savedInstanceState);
        setMode();
        setupRealtimeControls(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WINDOW_CREATED, true);
        super.onSaveInstanceState(outState);
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Window_Created = savedInstanceState.getBoolean(WINDOW_CREATED, false);
            windowManager = WindowsMethods.getWindowManager(requireActivity());
        }
    }

    private void setMode() {
        Intent intent = Objects.requireNonNull(requireActivity().getIntent());
        Edit_Mode = intent.getBooleanExtra(Config.INTENT_PICTURE_EDIT_MODE, false);
        AlertDialog.Builder loading = new AlertDialog.Builder(requireActivity());
        loading.setCancelable(false);
        // Note: For new images, floating window will be created after image processing is complete
        View mView = inflater.inflate(R.layout.dialog_loading, requireActivity().findViewById(R.id.layout_dialog_loading));
        loading.setView(mView);
        final AlertDialog alertDialog = loading.show();
        new Thread(() -> {
            if (!Window_Created) {
                if (Edit_Mode) {
                    //Edit
                    PictureId = intent.getStringExtra(Config.INTENT_PICTURE_EDIT_ID);
                    pictureData.setDataControl(PictureId);
                    PictureName = pictureData.getListArray().get(PictureId);
                    position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
                    position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
                    picture_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, true);  // Use max size for fullscreen floating pictures
                    zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, default_zoom);
                    floatImageView = ImageMethods.getFloatImageViewById(requireContext(), PictureId);
                    
                    requireActivity().runOnUiThread(() -> {
                        alertDialog.cancel();
                    });
                } else {
                    //New
                    if (intent.getData() == null) {
                        requireActivity().runOnUiThread(() -> {
                            alertDialog.cancel();
                            requireActivity().finish();
                        });
                        return;
                    }
                    
                    // Take persistent URI permission
                    try {
                        requireActivity().getContentResolver().takePersistableUriPermission(
                            intent.getData(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        android.util.Log.d("FloatPicture", "Took persistent URI permission for: " + intent.getData());
                    } catch (SecurityException e) {
                        android.util.Log.w("FloatPicture", "Failed to take persistent URI permission", e);
                    }
                    
                    PictureId = ImageMethods.setNewImage(getActivity(), intent.getData());
                    if (PictureId == null) {
                        requireActivity().runOnUiThread(() -> {
                            alertDialog.cancel();
                            requireActivity().finish();
                        });
                        return;
                    }
                    
                    pictureData.setDataControl(PictureId);
                    PictureName = getString(R.string.new_picture_name);
                    position_x = Config.DATA_DEFAULT_PICTURE_POSITION_X;
                    position_y = Config.DATA_DEFAULT_PICTURE_POSITION_Y;
                    picture_degree = Config.DATA_DEFAULT_PICTURE_DEGREE;
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    if (bitmap == null) {
                        requireActivity().runOnUiThread(() -> {
                            alertDialog.cancel();
                            requireActivity().finish();
                        });
                        return;
                    }
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, true);  // Use max size for fullscreen floating pictures
                    zoom = 1.0f;
                    
                    // Create FloatImageView on main thread
                    requireActivity().runOnUiThread(() -> {
                        floatImageView = ImageMethods.createPictureView(requireContext(), bitmap, false, false, zoom, picture_degree);
                        // No alpha settings - always fully opaque
                        floatImageView.setPictureId(PictureId);
                        
                        // For new pictures, save initial configuration before showing
                        if (!Edit_Mode && PictureId != null) {
                            // First set data control for this specific picture
                            pictureData.setDataControl(PictureId);
                            
                            // Save basic configuration (no URI path needed since files are saved)
                            pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                            pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
                            pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
                            pictureData.put(Config.DATA_PICTURE_POSITION_X, position_x);
                            pictureData.put(Config.DATA_PICTURE_POSITION_Y, position_y);
                            pictureData.put(Config.DATA_PICTURE_DEGREE, picture_degree);
                            pictureData.commit(PictureName);
                            
                            android.util.Log.d("FloatPicture", "Saved picture configuration for ID: " + PictureId);
                            
                            // Now show the floating image
                            ManageMethods.showSingleFloatingImage(requireContext(), PictureId);
                        }
                        
                        alertDialog.cancel();
                    });
                }
            }
        }).start();
    }

    private void setupRealtimeControls(View view) {
        // Degree control
        RadioGroup degreeRadioGroup = view.findViewById(R.id.radio_group_degree_realtime);
        if (picture_degree == 0) {
            degreeRadioGroup.check(R.id.radio_0_degree_realtime);
        } else if (picture_degree == 90) {
            degreeRadioGroup.check(R.id.radio_90_degree_realtime);
        } else if (picture_degree == 180) {
            degreeRadioGroup.check(R.id.radio_180_degree_realtime);
        } else if (picture_degree == 270) {
            degreeRadioGroup.check(R.id.radio_270_degree_realtime);
        }
        degreeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_0_degree_realtime) {
                picture_degree = 0;
            } else if (checkedId == R.id.radio_90_degree_realtime) {
                picture_degree = 90;
            } else if (checkedId == R.id.radio_180_degree_realtime) {
                picture_degree = 180;
            } else if (checkedId == R.id.radio_270_degree_realtime) {
                picture_degree = 270;
            }
            updateFloatingImageRealtime();
            autoSaveSettings();
        });
    }


    private void updateFloatingImageRealtime() {
        try {
            if (floatImageView != null && bitmap != null) {
                floatImageView.setAlpha(1.0f);  // Always fully opaque
                floatImageView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, picture_degree));
                WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, false, false, zoom, picture_degree);
                android.util.Log.d("FloatPicture", "Updated floating image - degree: " + picture_degree);
            } else {
                android.util.Log.w("FloatPicture", "Cannot update floating image - floatImageView: " + floatImageView + ", bitmap: " + bitmap);
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error updating floating image", e);
        }
    }

    private void autoSaveSettings() {
        // Auto-save all settings in real-time
        if (pictureData != null && PictureId != null) {
            // Ensure we're controlling the correct picture data
            pictureData.setDataControl(PictureId);
            
            // For new pictures, set as active (single active design)
            // For edit mode, maintain current active state
            if (!Edit_Mode) {
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                android.util.Log.d("FloatPicture", "Auto-saved settings for new picture ID: " + PictureId);
            } else {
                // For edit mode, preserve existing enabled state
                boolean currentState = pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED);
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, currentState);
                android.util.Log.d("FloatPicture", "Auto-saved settings for existing picture ID: " + PictureId);
            }
            
            pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
            pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
            pictureData.put(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);  // Fixed position
            pictureData.put(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);  // Fixed position
            pictureData.put(Config.DATA_PICTURE_DEGREE, picture_degree);
            pictureData.commit(PictureName);
            ImageMethods.saveFloatImageViewById(requireActivity(), PictureId, floatImageView);
        }
    }


    private void setPictureDegree() {
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, false, false, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);

        View mView = inflater.inflate(R.layout.dialog_set_size, requireActivity().findViewById(R.id.layout_dialog_set_size));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_degree);
        dialog.setCancelable(false);
        TextView name = mView.findViewById(R.id.textview_set_size);
        name.setText(R.string.degree);

        // New RadioGroup and RadioButton handling
        RadioGroup radioGroup = mView.findViewById(R.id.radio_group_degree);
        if (picture_degree == 0) {
            radioGroup.check(R.id.radio_0_degree);
        } else if (picture_degree == 90) {
            radioGroup.check(R.id.radio_90_degree);
        } else if (picture_degree == 180) {
            radioGroup.check(R.id.radio_180_degree);
        } else if (picture_degree == 270) {
            radioGroup.check(R.id.radio_270_degree);
        } else {
            // Default to 0 if current degree is not one of the options
            radioGroup.check(R.id.radio_0_degree);
            picture_degree = 0; // Set initial degree to 0 if it's not 0, 90, 180, or 270
        }
        picture_degree_temp = picture_degree; // Initialize temp with current or default value

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_0_degree) {
                picture_degree_temp = 0;
            } else if (checkedId == R.id.radio_90_degree) {
                picture_degree_temp = 90;
            } else if (checkedId == R.id.radio_180_degree) {
                picture_degree_temp = 180;
            } else if (checkedId == R.id.radio_270_degree) {
                picture_degree_temp = 270;
            }
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, false, false, zoom, picture_degree_temp);
        });

        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_degree = picture_degree_temp;
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    



    private void onEditPicture(FloatImageView FloatImageView_Edit) {
        if (!onUseEditPicture) {
            // Only remove view if it's actually attached to window manager
            if (floatImageView != null && floatImageView.getParent() != null) {
                try {
                    windowManager.removeView(floatImageView);
                    floatImageView.refreshDrawableState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            WindowsMethods.createWindow(windowManager, FloatImageView_Edit, false, false);
            onUseEditPicture = true;
        }
    }

    private void onSuccessEditPicture(FloatImageView floatImageView_Edit, Bitmap bitmap_Edit) {
        if (onUseEditPicture) {
            if (floatImageView_Edit != null && floatImageView_Edit.getParent() != null) {
                try {
                    windowManager.removeView(floatImageView_Edit);
                    floatImageView_Edit.refreshDrawableState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bitmap_Edit.recycle();
            floatImageView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, picture_degree));
            WindowsMethods.createWindow(windowManager, floatImageView, false, false);
            onUseEditPicture = false;
        }
    }

    private void onFailedEditPicture(FloatImageView floatImageView_Edit, Bitmap bitmap_Edit) {
        if (onUseEditPicture) {
            if (floatImageView_Edit != null && floatImageView_Edit.getParent() != null) {
                try {
                    windowManager.removeView(floatImageView_Edit);
                    floatImageView_Edit.refreshDrawableState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bitmap_Edit.recycle();
            WindowsMethods.createWindow(windowManager, floatImageView, false, false);
            onUseEditPicture = false;
        }
    }

    public void saveAllData() {
        // Settings are now auto-saved in real-time, this method is kept for compatibility
        autoSaveSettings();
        
        // For new pictures, ensure single active image design
        if (!Edit_Mode && PictureId != null) {
            // Make this new picture the only active image by using the single active mechanism
            ManageMethods.setWindowVisible(requireContext(), pictureData, PictureId, true);
        }
    }

    public void clearEditView() {
        try {
            if (onUseEditPicture) {
                if (floatImageView_Edit != null && bitmap_Edit != null) {
                    onFailedEditPicture(floatImageView_Edit, bitmap_Edit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        try {
            if (!Edit_Mode) {
                // For new pictures, don't clean up - the picture has been saved and should remain
                android.util.Log.d("FloatPicture", "Exiting new picture mode - keeping saved picture with ID: " + PictureId);
                // Don't recycle bitmap or clear temp data for new pictures
                // The floating image should remain active
            } else {
                // For edit mode, restore original settings
                if (floatImageView != null && pictureData != null && bitmap != null && windowManager != null) {
                    float original_zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, zoom);
                    float original_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, picture_degree);
                    int original_position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, position_x);
                    int original_position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, position_y);
                    // No alpha settings - always fully opaque
                    WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, false, false, original_zoom, original_degree);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("FloatPicture", "Error in exit()", e);
        }
    }

}
