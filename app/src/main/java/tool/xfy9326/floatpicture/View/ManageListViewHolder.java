package tool.xfy9326.floatpicture.View;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tool.xfy9326.floatpicture.R;

public class ManageListViewHolder extends AdvancedRecyclerView.ViewHolder {
    final ImageView imageView_Picture_Preview;
    final TextView textView_Picture_Error;
    final View pictureFrame;
    final ImageView activeIndicator;
    ManageListViewHolder(View mView) {
        super(mView);
        imageView_Picture_Preview = mView.findViewById(R.id.adapter_picture_preview);
        textView_Picture_Error = mView.findViewById(R.id.adapter_picture_error);
        pictureFrame = mView.findViewById(R.id.adapter_picture_frame);
        activeIndicator = mView.findViewById(R.id.adapter_active_indicator);
    }
}
