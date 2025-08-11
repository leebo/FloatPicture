package tool.xfy9326.floatpicture.View;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import tool.xfy9326.floatpicture.R;

public class FloatToggleButton extends FrameLayout {

    private ImageView iconView;

    public FloatToggleButton(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        iconView = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        iconView.setLayoutParams(params);
        addView(iconView);
        // Set initial icon (will be updated by ManageMethods)
        iconView.setImageResource(R.drawable.ic_visibility);
        setContentDescription(context.getString(R.string.toggle_visibility));
    }

    public void setIcon(int drawableResId, int contentDescriptionResId) {
        iconView.setImageResource(drawableResId);
        setContentDescription(getContext().getString(contentDescriptionResId));
    }
}