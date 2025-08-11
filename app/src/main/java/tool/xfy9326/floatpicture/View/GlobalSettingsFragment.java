package tool.xfy9326.floatpicture.View;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;

public class GlobalSettingsFragment extends PreferenceFragmentCompat {
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(requireActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_global_settings);
        PreferenceSet();
    }

    @NonNull
    private Preference requirePreference(CharSequence key) {
        return Objects.requireNonNull(findPreference(key));
    }

    private void PreferenceSet() {
        requirePreference(Config.PREFERENCE_NEW_PICTURE_QUALITY).setOnPreferenceClickListener(preference -> {
            PictureQualitySet();
            return true;
        });
        requirePreference(Config.PREFERENCE_SHOW_NOTIFICATION_CONTROL).setOnPreferenceChangeListener((preference, newValue) -> {
            Toast.makeText(getActivity(), R.string.restart_to_apply_changes, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void PictureQualitySet() {
        // Directly set the quality to 100%
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Config.PREFERENCE_NEW_PICTURE_QUALITY, 100);
        editor.apply();
        Toast.makeText(getActivity(), "Default picture quality set to 100%.", Toast.LENGTH_SHORT).show();
    }
}
