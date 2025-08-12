package tool.xfy9326.floatpicture.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import tool.xfy9326.floatpicture.Methods.ImageMethods;
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.Methods.WindowsMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;

public class PictureSettingsFragment extends PreferenceFragmentCompat {
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
    private boolean touch_and_move;
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
    private boolean allow_picture_over_layout;

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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_picture_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreData(savedInstanceState);
        setMode();
        PreferenceSet();
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
                    touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
                    allow_picture_over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
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
                    touch_and_move = Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE;
                    allow_picture_over_layout = Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT;
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
                        floatImageView = ImageMethods.createPictureView(requireContext(), bitmap, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
                        floatImageView.setAlpha(picture_alpha);
                        floatImageView.setPictureId(PictureId);
                        
                        // Show single floating image for new pictures
                        if (!Edit_Mode && PictureId != null) {
                            ManageMethods.showSingleFloatingImage(requireContext(), PictureId);
                        }
                        
                        alertDialog.cancel();
                    });
                }
            }
        }).start();
    }

    @NonNull
    private Preference requirePreference(CharSequence key) {
        return Objects.requireNonNull(findPreference(key));
    }

    private void PreferenceSet() {
        requirePreference(Config.PREFERENCE_PICTURE_DEGREE).setOnPreferenceClickListener(preference -> {
            setPictureDegree();
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_ALPHA).setOnPreferenceClickListener(preference -> {
            // setPictureAlpha();
            return true;
        });
        CheckBoxPreference preference_touch_and_move = findPreference(Config.PREFERENCE_PICTURE_TOUCH_AND_MOVE);
        assert preference_touch_and_move != null;
        preference_touch_and_move.setChecked(touch_and_move);
        preference_touch_and_move.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                PictureTouchAndMoveAlert();
                return false;
            } else {
                setPictureTouchAndMove(false);
                return true;
            }
        });
        CheckBoxPreference preference_over_layout = findPreference(Config.PREFERENCE_ALLOW_PICTURE_OVER_LAYOUT);
        assert preference_over_layout != null;
        preference_over_layout.setChecked(allow_picture_over_layout);
        preference_over_layout.setOnPreferenceChangeListener((preference, newValue) -> {
            setAllowPictureOverLayout((boolean) newValue);
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_POSITION).setOnPreferenceClickListener(preference -> {
            setPicturePosition();
            return true;
        });
    }

    private void setAllowPictureOverLayout(boolean allow) {
        allow_picture_over_layout = allow;
        if (floatImageView != null && floatImageView.getParent() != null) {
            try {
                windowManager.removeView(floatImageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        floatImageView.setOverLayout(allow_picture_over_layout);
        WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow, position_x, position_y);
    }

    private void setPictureTouchAndMove(boolean touchable_and_moveable) {
        touch_and_move = touchable_and_moveable;
        if (floatImageView != null && floatImageView.getParent() != null) {
            try {
                windowManager.removeView(floatImageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        floatImageView.setMoveable(touchable_and_moveable);
        WindowsMethods.createWindow(windowManager, floatImageView, touchable_and_moveable, allow_picture_over_layout, position_x, position_y);
    }

    private void PictureTouchAndMoveAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.settings_picture_touchable_and_moveable);
        builder.setMessage(R.string.settings_picture_touchable_and_moveable_warn);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.done, (dialog, which) -> {
            ((CheckBoxPreference) Objects.requireNonNull(findPreference(Config.PREFERENCE_PICTURE_TOUCH_AND_MOVE))).setChecked(true);
            setPictureTouchAndMove(true);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void setPictureDegree() {
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
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
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree_temp, position_x, position_y);
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
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
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
        final boolean touchable_edit = (touch_and_move || sharedPreferences.getBoolean(Config.PREFERENCE_TOUCHABLE_POSITION_EDIT, false));
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);
        if (touchable_edit) {
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, true, allow_picture_over_layout, zoom, picture_degree, position_x, position_y);
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
        if (!allow_picture_over_layout) {
            seekBar_x.setMax(Max_X);
            seekBar_x.setProgress(position_x);
        }
        final EditText editText_x = mView.findViewById(R.id.edittext_set_position_x);
        editText_x.setText(String.valueOf(position_x));
        final SeekBar seekBar_y = mView.findViewById(R.id.seekbar_set_position_y);
        if (!allow_picture_over_layout) {
            seekBar_y.setMax(Max_Y);
            seekBar_y.setProgress(position_y);
        }
        final EditText editText_y = mView.findViewById(R.id.edittext_set_position_y);
        editText_y.setText(String.valueOf(position_y));
        if (allow_picture_over_layout) {
            editText_x.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            editText_y.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        position_x_temp = position_x;
        position_y_temp = position_y;
        seekBar_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_x_temp = progress;
                editText_x.setText(String.valueOf(progress));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
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
                if (allow_picture_over_layout || (edittext_temp >= 0 && edittext_temp <= Max_X)) {
                    position_x_temp = edittext_temp;
                    if (!allow_picture_over_layout) {
                        seekBar_x.setProgress(edittext_temp);
                    }
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
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
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
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
                if (allow_picture_over_layout || (edittext_temp >= 0 && edittext_temp <= Max_Y)) {
                    position_y_temp = edittext_temp;
                    if (!allow_picture_over_layout) {
                        seekBar_y.setProgress(edittext_temp);
                    }
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
                } else {
                    Toast.makeText(getActivity(), R.string.settings_picture_position_warn, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        if (allow_picture_over_layout) {
            seekBar_x.setEnabled(false);
            seekBar_y.setEnabled(false);
        }
        if (touchable_edit) {
            dialog.setNeutralButton(R.string.save_moved_position, (dialog1, which) -> {
                position_x = (int) floatImageView_Edit.getMovedPositionX();
                position_y = (int) floatImageView_Edit.getMovedPositionY();
                onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
            });
        }
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            if (allow_picture_over_layout) {
                try {
                    position_x = Integer.parseInt(editText_x.getText().toString());
                    position_y = Integer.parseInt(editText_y.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    position_x = position_x_temp;
                    position_y = position_y_temp;
                }
            } else {
                position_x = position_x_temp;
                position_y = position_y_temp;
            }
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
            WindowsMethods.createWindow(windowManager, FloatImageView_Edit, touch_and_move, allow_picture_over_layout, position_x, position_y);
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
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
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
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
            onUseEditPicture = false;
        }
    }

    public void saveAllData() {
        pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
        pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
        pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
        pictureData.put(Config.DATA_PICTURE_ALPHA, picture_alpha);
        if (touch_and_move) {
            position_x = (int) floatImageView.getMovedPositionX();
            position_y = (int) floatImageView.getMovedPositionY();
        }
        pictureData.put(Config.DATA_PICTURE_POSITION_X, position_x);
        pictureData.put(Config.DATA_PICTURE_POSITION_Y, position_y);
        pictureData.put(Config.DATA_PICTURE_DEGREE, picture_degree);
        pictureData.put(Config.DATA_PICTURE_TOUCH_AND_MOVE, touch_and_move);
        pictureData.put(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, allow_picture_over_layout);
        pictureData.commit(PictureName);
        WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, touch_and_move, allow_picture_over_layout, zoom, picture_degree, position_x, position_y);
        ImageMethods.saveFloatImageViewById(requireActivity(), PictureId, floatImageView);
    }

    public void clearEditView() {
        if (onUseEditPicture) {
            if (floatImageView_Edit != null && bitmap_Edit != null) {
                onFailedEditPicture(floatImageView_Edit, bitmap_Edit);
            }
        }
    }

    public void exit() {
        if (!Edit_Mode) {
            if (floatImageView != null) {
                if (floatImageView.getParent() != null) {
                    try {
                        windowManager.removeView(floatImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bitmap.recycle();
                floatImageView = null;
            }
            ImageMethods.clearAllTemp(requireActivity(), PictureId);
        } else {
            float original_zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, zoom);
            float original_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, picture_alpha);
            float original_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, picture_degree);
            int original_position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, position_x);
            int original_position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, position_y);
            boolean original_allow_picture_over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, allow_picture_over_layout);
            boolean original_touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
            floatImageView.setAlpha(original_alpha);
            floatImageView.setOverLayout(original_allow_picture_over_layout);
            floatImageView.setMoveable(original_touch_and_move);
            WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, original_touch_and_move, original_allow_picture_over_layout, original_zoom, original_degree, original_position_x, original_position_y);
        }

    }

}
