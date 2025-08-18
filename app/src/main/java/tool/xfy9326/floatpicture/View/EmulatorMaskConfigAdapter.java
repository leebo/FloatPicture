package tool.xfy9326.floatpicture.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.LinkedHashMap;
import java.util.List;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.EmulatorDetector;
import tool.xfy9326.floatpicture.Utils.EmulatorMaskConfig;
import tool.xfy9326.floatpicture.Utils.PictureData;

/**
 * 模拟器遮罩配置适配器
 */
public class EmulatorMaskConfigAdapter extends RecyclerView.Adapter<EmulatorMaskConfigAdapter.ViewHolder> {

    private final Context context;
    private final List<EmulatorDetector.InstalledEmulator> emulators;
    private final EmulatorMaskConfig emulatorMaskConfig;
    private boolean globalEnabled;

    public EmulatorMaskConfigAdapter(Context context, 
                                   List<EmulatorDetector.InstalledEmulator> emulators,
                                   EmulatorMaskConfig emulatorMaskConfig) {
        this.context = context;
        this.emulators = emulators;
        this.emulatorMaskConfig = emulatorMaskConfig;
        this.globalEnabled = emulatorMaskConfig.isAutoMaskEnabled();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emulator_mask_config, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmulatorDetector.InstalledEmulator emulator = emulators.get(position);
        String packageName = emulator.emulatorInfo.packageName;
        
        // 获取当前配置
        EmulatorMaskConfig.MaskConfig config = emulatorMaskConfig.getMaskConfig(packageName);
        
        // 设置模拟器信息
        holder.emulatorName.setText(emulator.emulatorInfo.name);
        holder.emulatorConsole.setText(emulator.emulatorInfo.console);
        holder.emulatorPackage.setText(packageName);
        holder.emulatorVersion.setText("v" + emulator.versionName);
        
        // 设置模拟器图标（可以根据不同模拟器显示不同图标）
        holder.emulatorIcon.setImageResource(getEmulatorIcon(emulator.emulatorInfo.console));
        
        // 设置自动显示开关
        holder.autoShowSwitch.setChecked(config.autoShowEnabled);
        holder.autoShowSwitch.setEnabled(globalEnabled);
        holder.autoShowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            config.autoShowEnabled = isChecked;
            emulatorMaskConfig.setMaskConfig(packageName, config);
            
            if (isChecked && config.pictureId.isEmpty()) {
                Toast.makeText(context, "请先选择遮罩图片", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置图片选择器
        setupPictureSpinner(holder, packageName, config);
        
        // 设置卡片点击效果
        holder.cardView.setOnClickListener(v -> {
            Toast.makeText(context, emulator.emulatorInfo.description, Toast.LENGTH_SHORT).show();
        });
        
        // 根据全局开关状态更新UI
        updateItemEnabled(holder, globalEnabled);
    }

    private void setupPictureSpinner(ViewHolder holder, String packageName, EmulatorMaskConfig.MaskConfig config) {
        // 获取所有可用的图片
        PictureData pictureData = new PictureData();
        LinkedHashMap<String, String> pictures = pictureData.getListArray();
        
        // 创建下拉选项
        String[] pictureOptions = new String[pictures.size() + 1];
        pictureOptions[0] = "请选择遮罩图片";
        
        int selectedPosition = 0;
        int index = 1;
        for (LinkedHashMap.Entry<String, String> entry : pictures.entrySet()) {
            String pictureId = entry.getKey();
            String pictureName = entry.getValue();
            pictureOptions[index] = pictureName;
            
            if (pictureId.equals(config.pictureId)) {
                selectedPosition = index;
            }
            index++;
        }
        
        // 设置适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, 
                android.R.layout.simple_spinner_item, pictureOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.pictureSpinner.setAdapter(adapter);
        holder.pictureSpinner.setSelection(selectedPosition);
        
        // 设置选择监听器
        holder.pictureSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 未选择图片
                    config.pictureId = "";
                } else {
                    // 选择了图片
                    int pictureIndex = 0;
                    for (String pictureId : pictures.keySet()) {
                        if (pictureIndex == position - 1) {
                            config.pictureId = pictureId;
                            break;
                        }
                        pictureIndex++;
                    }
                }
                emulatorMaskConfig.setMaskConfig(packageName, config);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private int getEmulatorIcon(String console) {
        // 根据游戏机类型返回对应图标
        switch (console) {
            case "GBA":
            case "GBC":
                return R.drawable.ic_gameboy; // 需要添加对应图标
            case "PSP":
                return R.drawable.ic_psp;
            case "N64":
                return R.drawable.ic_n64;
            case "SNES":
            case "NES":
                return R.drawable.ic_nintendo;
            case "MD":
            case "SMS":
                return R.drawable.ic_sega;
            case "PSX":
                return R.drawable.ic_playstation;
            case "Arcade":
                return R.drawable.ic_arcade;
            default:
                return R.drawable.ic_gamepad; // 默认手柄图标
        }
    }

    private void updateItemEnabled(ViewHolder holder, boolean enabled) {
        holder.autoShowSwitch.setEnabled(enabled);
        holder.pictureSpinner.setEnabled(enabled);
        
        // 设置卡片透明度
        float alpha = enabled ? 1.0f : 0.6f;
        holder.cardView.setAlpha(alpha);
    }

    public void updateGlobalEnabled(boolean enabled) {
        this.globalEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return emulators.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView emulatorIcon;
        TextView emulatorName;
        TextView emulatorConsole;
        TextView emulatorPackage;
        TextView emulatorVersion;
        SwitchMaterial autoShowSwitch;
        Spinner pictureSpinner;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            emulatorIcon = itemView.findViewById(R.id.iv_emulator_icon);
            emulatorName = itemView.findViewById(R.id.tv_emulator_name);
            emulatorConsole = itemView.findViewById(R.id.tv_emulator_console);
            emulatorPackage = itemView.findViewById(R.id.tv_emulator_package);
            emulatorVersion = itemView.findViewById(R.id.tv_emulator_version);
            autoShowSwitch = itemView.findViewById(R.id.switch_auto_show);
            pictureSpinner = itemView.findViewById(R.id.spinner_picture);
        }
    }
}