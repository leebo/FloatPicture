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
    private float picture_alpha;
    private float picture_alpha_temp;
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
                    picture_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, Config.DATA_DEFAULT_PICTURE_ALPHA);
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, false);
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
                    picture_alpha = Config.DATA_DEFAULT_PICTURE_ALPHA;
                    picture_degree = Config.DATA_DEFAULT_PICTURE_DEGREE;
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    if (bitmap == null) {
                        requireActivity().runOnUiThread(() -> {
                            alertDialog.cancel();
                            requireActivity().finish();
                        });
                        return;
                    }
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, false);
                    zoom = 1.0f;
                    
                    // Create FloatImageView on main thread
                    requireActivity().runOnUiThread(() -> {
                        floatImageView = ImageMethods.createPictureView(requireContext(), bitmap, false, false, zoom, picture_degree);
                        floatImageView.setAlpha(picture_alpha);
                        floatImageView.setPictureId(PictureId);
                        
                        // For new pictures, save initial configuration before showing
                        if (!Edit_Mode && PictureId != null) {
                            // Save basic configuration first including URI path
                            pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                            pictureData.put(Config.DATA_PICTURE_URI_PATH, intent.getData().toString());
                            pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
                            pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
                            pictureData.put(Config.DATA_PICTURE_ALPHA, picture_alpha);
                            pictureData.put(Config.DATA_PICTURE_POSITION_X, position_x);
                            pictureData.put(Config.DATA_PICTURE_POSITION_Y, position_y);
                            pictureData.put(Config.DATA_PICTURE_DEGREE, picture_degree);
                            pictureData.commit(PictureName);
                            
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
        // Alpha control
        SeekBar alphaSeekBar = view.findViewById(R.id.seekbar_alpha_realtime);
        TextView alphaValueText = view.findViewById(R.id.textview_alpha_value_realtime);
        int currentAlphaPercentage = (int) (picture_alpha * 100);
        alphaSeekBar.setProgress(currentAlphaPercentage);
        alphaValueText.setText(currentAlphaPercentage + "%");
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    picture_alpha = (float) progress / 100.0f;
                    alphaValueText.setText(progress + "%");
                    updateFloatingImageRealtime();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoSaveSettings();
            }
        });

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

        // Position controls
        setupPositionControls(view);
    }

    private void setupPositionControls(View view) {
        SeekBar positionXSeekBar = view.findViewById(R.id.seekbar_position_x_realtime);
        SeekBar positionYSeekBar = view.findViewById(R.id.seekbar_position_y_realtime);
        EditText positionXEdit = view.findViewById(R.id.edittext_position_x_realtime);
        EditText positionYEdit = view.findViewById(R.id.edittext_position_y_realtime);

        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int maxX = size.x;
        final int maxY = size.y;

        // Setup seekbars
        positionXSeekBar.setMax(maxX);
        positionYSeekBar.setMax(maxY);
        positionXSeekBar.setProgress(Math.max(0, Math.min(position_x, maxX)));
        positionYSeekBar.setProgress(Math.max(0, Math.min(position_y, maxY)));
        positionXEdit.setText(String.valueOf(position_x));
        positionYEdit.setText(String.valueOf(position_y));

        // Position X controls
        positionXSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    position_x = progress;
                    positionXEdit.setText(String.valueOf(progress));
                    updateFloatingImageRealtime();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoSaveSettings();
            }
        });

        positionXEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = Integer.parseInt(s.toString());
                    if (value >= 0 && value <= maxX) {
                        position_x = value;
                        positionXSeekBar.setProgress(value);
                        updateFloatingImageRealtime();
                        autoSaveSettings();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

        // Position Y controls
        positionYSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    position_y = progress;
                    positionYEdit.setText(String.valueOf(progress));
                    updateFloatingImageRealtime();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoSaveSettings();
            }
        });

        positionYEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = Integer.parseInt(s.toString());
                    if (value >= 0 && value <= maxY) {
                        position_y = value;
                        positionYSeekBar.setProgress(value);
                        updateFloatingImageRealtime();
                        autoSaveSettings();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

        updatePositionControlsVisibility(view);
    }

    private void updatePositionControlsVisibility(View view) {
        SeekBar positionXSeekBar = view.findViewById(R.id.seekbar_position_x_realtime);
        SeekBar positionYSeekBar = view.findViewById(R.id.seekbar_position_y_realtime);
        EditText positionXEdit = view.findViewById(R.id.edittext_position_x_realtime);
        EditText positionYEdit = view.findViewById(R.id.edittext_position_y_realtime);

        positionXSeekBar.setEnabled(true);
        positionYSeekBar.setEnabled(true);
        positionXEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        positionYEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void updateFloatingImageRealtime() {
        if (floatImageView != null) {
            floatImageView.setAlpha(picture_alpha);
            floatImageView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, picture_degree));
            WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, false, false, zoom, picture_degree, position_x, position_y);
        }
    }

    private void autoSaveSettings() {
        // Auto-save all settings in real-time
        if (pictureData != null && PictureId != null) {
            // For new pictures, set as active (single active design)
            // For edit mode, maintain current active state
            if (!Edit_Mode) {
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                // For new pictures, save URI path if available
                Intent intent = Objects.requireNonNull(requireActivity().getIntent());
                if (intent.getData() != null) {
                    pictureData.put(Config.DATA_PICTURE_URI_PATH, intent.getData().toString());
                }
            } else {
                // For edit mode, preserve existing enabled state and URI path
                boolean currentState = pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED);
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, currentState);
                // URI path should already be saved for existing pictures
            }
            
            pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
            pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
            pictureData.put(Config.DATA_PICTURE_ALPHA, picture_alpha);
            pictureData.put(Config.DATA_PICTURE_POSITION_X, position_x);
            pictureData.put(Config.DATA_PICTURE_POSITION_Y, position_y);
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
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, false, false, zoom, picture_degree_temp, position_x, position_y);
        });

        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_degree = picture_degree_temp;
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    

    private void setPictureAlpha() {
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, false, false, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);

        View mView = inflater.inflate(R.layout.dialog_set_alpha, requireActivity().findViewById(R.id.layout_dialog_set_alpha));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_alpha);
        dialog.setCancelable(false);

        TextView tvAlphaValue = mView.findViewById(R.id.textview_alpha_value);
        SeekBar seekBarAlpha = mView.findViewById(R.id.seekbar_alpha);

        // Convert alpha (0.0-1.0) to percentage (0-100)
        int currentAlphaPercentage = (int) (picture_alpha * 100);
        tvAlphaValue.setText(getString(R.string.transparency_format, currentAlphaPercentage));
        seekBarAlpha.setProgress(currentAlphaPercentage);
        picture_alpha_temp = picture_alpha; // Initialize temp with current value

        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                picture_alpha_temp = (float) progress / 100.0f;
                tvAlphaValue.setText(getString(R.string.transparency_format, progress));
                floatImageView_Edit.setAlpha(picture_alpha_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_alpha = picture_alpha_temp;
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    private void setPicturePosition() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final boolean touchable_edit = sharedPreferences.getBoolean(Config.PREFERENCE_TOUCHABLE_POSITION_EDIT, false);
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touchable_edit, false, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);
        if (touchable_edit) {
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, true, false, zoom, picture_degree, position_x, position_y);
        }

        View mView = inflater.inflate(R.layout.dialog_set_position, requireActivity().findViewById(R.id.layout_dialog_set_position));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_position);
        dialog.setCancelable(false);
        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int Max_X = size.x;
        final int Max_Y = size.y;
        final SeekBar seekBar_x = mView.findViewById(R.id.seekbar_set_position_x);
        seekBar_x.setMax(Max_X);
        seekBar_x.setProgress(Math.max(0, Math.min(position_x, Max_X)));
        final EditText editText_x = mView.findViewById(R.id.edittext_set_position_x);
        editText_x.setText(String.valueOf(position_x));
        final SeekBar seekBar_y = mView.findViewById(R.id.seekbar_set_position_y);
        seekBar_y.setMax(Max_Y);
        seekBar_y.setProgress(Math.max(0, Math.min(position_y, Max_Y)));
        final EditText editText_y = mView.findViewById(R.id.edittext_set_position_y);
        editText_y.setText(String.valueOf(position_y));
        position_x_temp = position_x;
        position_y_temp = position_y;
        seekBar_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_x_temp = progress;
                editText_x.setText(String.valueOf(progress));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, false, zoom, picture_degree, position_x_temp, position_y_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText_x.setOnEditorActionListener((v, actionId, event) -> {
            try {
                int edittext_temp = Integer.parseInt(v.getText().toString());
                if (edittext_temp >= 0 && edittext_temp <= Max_X) {
                    position_x_temp = edittext_temp;
                    seekBar_x.setProgress(edittext_temp);
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, false, zoom, picture_degree, position_x_temp, position_y_temp);
                } else {
                    Toast.makeText(getActivity(), R.string.settings_picture_position_warn, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        seekBar_y.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_y_temp = progress;
                editText_y.setText(String.valueOf(progress));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, false, zoom, picture_degree, position_x_temp, position_y_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText_y.setOnEditorActionListener((v, actionId, event) -> {
            try {
                int edittext_temp = Integer.parseInt(v.getText().toString());
                if (edittext_temp >= 0 && edittext_temp <= Max_Y) {
                    position_y_temp = edittext_temp;
                    seekBar_y.setProgress(edittext_temp);
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, false, zoom, picture_degree, position_x_temp, position_y_temp);
                } else {
                    Toast.makeText(getActivity(), R.string.settings_picture_position_warn, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        if (touchable_edit) {
            dialog.setNeutralButton(R.string.save_moved_position, (dialog1, which) -> {
                position_x = (int) floatImageView_Edit.getMovedPositionX();
                position_y = (int) floatImageView_Edit.getMovedPositionY();
                onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
            });
        }
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            position_x = position_x_temp;
            position_y = position_y_temp;
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
            WindowsMethods.createWindow(windowManager, FloatImageView_Edit, false, false, position_x, position_y);
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
            WindowsMethods.createWindow(windowManager, floatImageView, false, false, position_x, position_y);
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
            WindowsMethods.createWindow(windowManager, floatImageView, false, false, position_x, position_y);
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
                if (floatImageView != null) {
                    if (floatImageView.getParent() != null) {
                        try {
                            windowManager.removeView(floatImageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    floatImageView = null;
                }
                if (PictureId != null && getActivity() != null) {
                    ImageMethods.clearAllTemp(getActivity(), PictureId);
                }
            } else {
                if (floatImageView != null && pictureData != null && bitmap != null && windowManager != null) {
                    float original_zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, zoom);
                    float original_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, picture_alpha);
                    float original_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, picture_degree);
                    int original_position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, position_x);
                    int original_position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, position_y);
                    floatImageView.setAlpha(original_alpha);
                    WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, false, false, original_zoom, original_degree, original_position_x, original_position_y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
