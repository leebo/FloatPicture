package tool.xfy9326.floatpicture.View;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import tool.xfy9326.floatpicture.Activities.MainActivity;
import tool.xfy9326.floatpicture.Activities.PictureSettingsActivity;
import tool.xfy9326.floatpicture.Methods.ImageMethods;
import tool.xfy9326.floatpicture.Methods.ManageMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;

public class ManageListAdapter extends AdvancedRecyclerView.Adapter<ManageListViewHolder> {
    private final Activity mActivity;
    private final PictureData pictureData;
    private LinkedHashMap<String, String> pictureInfo;
    private ArrayList<String> PictureId_Array;

    public ManageListAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        pictureData = new PictureData();
        updateData();
    }

    public void updateData() {
        pictureInfo = pictureData.getListArray();
        PictureId_Array = new ArrayList<>();
        if (pictureInfo != null) {
            for (Map.Entry<?, ?> entry : pictureInfo.entrySet()) {
                PictureId_Array.add(entry.getKey().toString());
            }
        }
    }

    @Override
    public int getItemCount() {
        return pictureInfo.size();
    }

    @Override
    public void onBindViewHolder(final ManageListViewHolder holder, int position) {
        final String mPictureId = PictureId_Array.get(holder.getAdapterPosition());
        holder.imageView_Picture_Preview.setImageBitmap(ImageMethods.getPreviewBitmap(mActivity, mPictureId));
        final PictureData pictureData = new PictureData();
        pictureData.setDataControl(mPictureId);

        if (!ImageMethods.isPictureFileExist(mPictureId)) {
            holder.textView_Picture_Error.setVisibility(View.VISIBLE);
        } else {
            holder.textView_Picture_Error.setVisibility(View.INVISIBLE);
        }

        holder.imageView_Picture_Preview.setOnClickListener(v -> {
            SwitchCompat switch_Picture_Show = holder.switch_Picture_Show;
            boolean isChecked = !switch_Picture_Show.isChecked();
            switch_Picture_Show.setChecked(isChecked);
            ManageMethods.setWindowVisible(mActivity, pictureData, mPictureId, isChecked);
        });

        SwitchCompat switch_Picture_Show = holder.switch_Picture_Show;
        switch_Picture_Show.setChecked(pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED));
        switch_Picture_Show.setOnCheckedChangeListener((compoundButton, b) -> ManageMethods.setWindowVisible(mActivity, pictureData, mPictureId, b));

        holder.imageView_Picture_Preview.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle(mPictureId);
            builder.setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) { // Edit
                    PictureData pictureData1 = new PictureData();
                    pictureData1.setDataControl(mPictureId);
                    Intent intent = new Intent(mActivity, PictureSettingsActivity.class);
                    intent.putExtra(Config.INTENT_PICTURE_EDIT_MODE, true);
                    intent.putExtra(Config.INTENT_PICTURE_EDIT_ID, mPictureId);
                    intent.putExtra(Config.INTENT_PICTURE_EDIT_POSITION, holder.getAdapterPosition());
                    mActivity.startActivityForResult(intent, Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_CHANGE);
                } else { // Delete
                    ManageMethods.DeleteWin(mActivity, mPictureId);
                    updateData();
                    int position1 = holder.getAdapterPosition();
                    notifyItemRemoved(position1);
                    notifyItemRangeChanged(position1, getItemCount() - position1);
                    MainActivity.SnackShow(mActivity, R.string.action_delete_window);
                    ManageMethods.updateNotificationCount(mActivity);
                }
            });
            builder.show();
            Toast.makeText(mActivity, "Long press detected!", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    @NonNull
    public ManageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View mView = inflater.inflate(R.layout.adapter_manage_list, parent, false);
        return new ManageListViewHolder(mView);
    }
}
