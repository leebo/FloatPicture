package tool.xfy9326.floatpicture.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.RetroEmulatorDatabase;

/**
 * 模拟器列表适配器
 */
public class EmulatorListAdapter extends RecyclerView.Adapter<EmulatorListAdapter.ViewHolder> {

    /**
     * 模拟器列表项
     */
    public static class EmulatorItem {
        public final RetroEmulatorDatabase.EmulatorInfo emulatorInfo;
        public final boolean isInstalled;
        public final String versionInfo;

        public EmulatorItem(RetroEmulatorDatabase.EmulatorInfo emulatorInfo, 
                           boolean isInstalled, String versionInfo) {
            this.emulatorInfo = emulatorInfo;
            this.isInstalled = isInstalled;
            this.versionInfo = versionInfo;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(EmulatorItem item);
    }

    private final Context context;
    private List<EmulatorItem> emulatorList;
    private final OnItemClickListener onItemClickListener;

    public EmulatorListAdapter(Context context, List<EmulatorItem> emulatorList, 
                              OnItemClickListener onItemClickListener) {
        this.context = context;
        this.emulatorList = emulatorList;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateList(List<EmulatorItem> newList) {
        this.emulatorList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emulator_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmulatorItem item = emulatorList.get(position);
        
        // 设置基本信息
        holder.emulatorName.setText(item.emulatorInfo.name);
        holder.emulatorDescription.setText(item.emulatorInfo.description);
        holder.packageName.setText(item.emulatorInfo.packageName);
        holder.consoleBadge.setText(item.emulatorInfo.console);
        
        // 设置模拟器图标
        int iconRes = getEmulatorIcon(item.emulatorInfo.console);
        holder.emulatorIcon.setImageResource(iconRes);
        
        // 设置安装状态
        if (item.isInstalled) {
            holder.installedBadge.setVisibility(View.VISIBLE);
            holder.status.setText(item.versionInfo.isEmpty() ? "已安装" : "v" + item.versionInfo);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.installed_color));
            holder.actionIcon.setImageResource(R.drawable.ic_launch);
            
            // 已安装的模拟器使用不同的卡片样式
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.installedBadge.setVisibility(View.GONE);
            holder.status.setText("未安装");
            holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.actionIcon.setImageResource(R.drawable.ic_download);
            
            // 未安装的模拟器稍微透明
            holder.itemView.setAlpha(0.8f);
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    private int getEmulatorIcon(String console) {
        switch (console) {
            case "GBA":
            case "GBC":
                return R.drawable.ic_gameboy;
            case "PSP":
                return R.drawable.ic_psp;
            case "N64":
                return R.drawable.ic_n64;
            case "SNES":
            case "NES":
                return R.drawable.ic_nintendo;
            case "MD":
            case "SMS":
            case "Saturn":
            case "Dreamcast":
                return R.drawable.ic_sega;
            case "PSX":
            case "PS2":
                return R.drawable.ic_playstation;
            case "Arcade":
            case "NeoGeo":
                return R.drawable.ic_arcade;
            case "GC":
            case "Wii":
                return R.drawable.ic_nintendo;
            case "3DS":
                return R.drawable.ic_nintendo;
            case "Switch":
                return R.drawable.ic_nintendo;
            case "多平台":
                return R.drawable.ic_multi_platform;
            case "前端":
                return R.drawable.ic_frontend;
            default:
                return R.drawable.ic_gamepad;
        }
    }

    @Override
    public int getItemCount() {
        return emulatorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView emulatorIcon;
        ImageView installedBadge;
        TextView emulatorName;
        TextView emulatorDescription;
        TextView packageName;
        TextView consoleBadge;
        TextView status;
        ImageView actionIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emulatorIcon = itemView.findViewById(R.id.iv_emulator_icon);
            installedBadge = itemView.findViewById(R.id.iv_installed_badge);
            emulatorName = itemView.findViewById(R.id.tv_emulator_name);
            emulatorDescription = itemView.findViewById(R.id.tv_emulator_description);
            packageName = itemView.findViewById(R.id.tv_package_name);
            consoleBadge = itemView.findViewById(R.id.tv_console_badge);
            status = itemView.findViewById(R.id.tv_status);
            actionIcon = itemView.findViewById(R.id.iv_action);
        }
    }
}