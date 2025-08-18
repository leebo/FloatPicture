package tool.xfy9326.floatpicture.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模拟器遮罩配置管理器
 * 管理每个模拟器对应的遮罩图片配置
 */
public class EmulatorMaskConfig {
    
    /**
     * 模拟器遮罩配置信息
     */
    public static class MaskConfig {
        public String pictureId;          // 遮罩图片ID
        public boolean autoShowEnabled;   // 是否启用自动显示
        public String maskType;           // 遮罩类型（全屏、控制器等）
        public String description;        // 配置描述
        
        public MaskConfig() {
            this.pictureId = "";
            this.autoShowEnabled = true;
            this.maskType = "controller";
            this.description = "";
        }
        
        public MaskConfig(String pictureId, boolean autoShowEnabled, String maskType, String description) {
            this.pictureId = pictureId;
            this.autoShowEnabled = autoShowEnabled;
            this.maskType = maskType;
            this.description = description;
        }
    }
    
    private final SharedPreferences preferences;
    private final Context context;
    
    public EmulatorMaskConfig(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    /**
     * 设置模拟器的遮罩配置
     * @param packageName 模拟器包名
     * @param config 遮罩配置
     */
    public void setMaskConfig(String packageName, MaskConfig config) {
        String prefix = Config.PREFERENCE_EMULATOR_MASK_CONFIG_PREFIX + packageName + "_";
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(prefix + "picture_id", config.pictureId);
        editor.putBoolean(prefix + "auto_show_enabled", config.autoShowEnabled);
        editor.putString(prefix + "mask_type", config.maskType);
        editor.putString(prefix + "description", config.description);
        editor.apply();
    }
    
    /**
     * 获取模拟器的遮罩配置
     * @param packageName 模拟器包名
     * @return 遮罩配置，如果不存在返回默认配置
     */
    public MaskConfig getMaskConfig(String packageName) {
        String prefix = Config.PREFERENCE_EMULATOR_MASK_CONFIG_PREFIX + packageName + "_";
        
        MaskConfig config = new MaskConfig();
        config.pictureId = preferences.getString(prefix + "picture_id", "");
        config.autoShowEnabled = preferences.getBoolean(prefix + "auto_show_enabled", true);
        config.maskType = preferences.getString(prefix + "mask_type", "controller");
        config.description = preferences.getString(prefix + "description", "");
        
        return config;
    }
    
    /**
     * 检查模拟器是否有配置
     * @param packageName 模拟器包名
     * @return 是否有配置
     */
    public boolean hasMaskConfig(String packageName) {
        String prefix = Config.PREFERENCE_EMULATOR_MASK_CONFIG_PREFIX + packageName + "_";
        return preferences.contains(prefix + "picture_id");
    }
    
    /**
     * 删除模拟器的遮罩配置
     * @param packageName 模拟器包名
     */
    public void removeMaskConfig(String packageName) {
        String prefix = Config.PREFERENCE_EMULATOR_MASK_CONFIG_PREFIX + packageName + "_";
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(prefix + "picture_id");
        editor.remove(prefix + "auto_show_enabled");
        editor.remove(prefix + "mask_type");
        editor.remove(prefix + "description");
        editor.apply();
    }
    
    /**
     * 获取所有已配置的模拟器包名
     * @return 模拟器包名列表
     */
    public Map<String, MaskConfig> getAllMaskConfigs() {
        Map<String, MaskConfig> configs = new HashMap<>();
        Set<String> allKeys = preferences.getAll().keySet();
        
        String prefix = Config.PREFERENCE_EMULATOR_MASK_CONFIG_PREFIX;
        
        for (String key : allKeys) {
            if (key.startsWith(prefix) && key.endsWith("_picture_id")) {
                // 提取包名
                String packageName = key.substring(prefix.length(), key.length() - "_picture_id".length());
                configs.put(packageName, getMaskConfig(packageName));
            }
        }
        
        return configs;
    }
    
    /**
     * 设置全局自动遮罩功能是否启用
     * @param enabled 是否启用
     */
    public void setAutoMaskEnabled(boolean enabled) {
        preferences.edit()
                  .putBoolean(Config.PREFERENCE_EMULATOR_AUTO_MASK_ENABLED, enabled)
                  .apply();
    }
    
    /**
     * 获取全局自动遮罩功能是否启用
     * @return 是否启用
     */
    public boolean isAutoMaskEnabled() {
        return preferences.getBoolean(Config.PREFERENCE_EMULATOR_AUTO_MASK_ENABLED, 
                                    Config.DATA_DEFAULT_EMULATOR_AUTO_MASK_ENABLED);
    }
    
    /**
     * 检查模拟器是否应该自动显示遮罩
     * @param packageName 模拟器包名
     * @return 是否应该自动显示
     */
    public boolean shouldAutoShowMask(String packageName) {
        if (!isAutoMaskEnabled()) {
            return false;
        }
        
        if (!hasMaskConfig(packageName)) {
            return false;
        }
        
        MaskConfig config = getMaskConfig(packageName);
        return config.autoShowEnabled && !config.pictureId.isEmpty();
    }
    
    /**
     * 获取模拟器对应的图片ID
     * @param packageName 模拟器包名
     * @return 图片ID，如果没有配置返回null
     */
    public String getMaskPictureId(String packageName) {
        if (!shouldAutoShowMask(packageName)) {
            return null;
        }
        
        MaskConfig config = getMaskConfig(packageName);
        return config.pictureId.isEmpty() ? null : config.pictureId;
    }
    
    /**
     * 为已安装的模拟器创建默认配置
     * @param installedEmulators 已安装的模拟器列表
     */
    public void createDefaultConfigsForInstalledEmulators(List<EmulatorDetector.InstalledEmulator> installedEmulators) {
        for (EmulatorDetector.InstalledEmulator emulator : installedEmulators) {
            String packageName = emulator.emulatorInfo.packageName;
            
            // 如果还没有配置，创建默认配置
            if (!hasMaskConfig(packageName)) {
                MaskConfig defaultConfig = new MaskConfig();
                defaultConfig.autoShowEnabled = true;
                defaultConfig.maskType = getDefaultMaskType(emulator.emulatorInfo.console);
                defaultConfig.description = "自动生成的" + emulator.emulatorInfo.name + "配置";
                
                setMaskConfig(packageName, defaultConfig);
            }
        }
    }
    
    /**
     * 根据游戏机类型获取默认遮罩类型
     * @param console 游戏机类型
     * @return 默认遮罩类型
     */
    private String getDefaultMaskType(String console) {
        switch (console) {
            case "GBA":
            case "GBC":
            case "PSP":
            case "3DS":
                return "handheld";
            case "Arcade":
                return "arcade_stick";
            default:
                return "controller";
        }
    }
    
    /**
     * 清理不再存在的模拟器配置
     * @param installedPackages 当前已安装的模拟器包名列表
     */
    public void cleanupRemovedEmulatorConfigs(List<String> installedPackages) {
        Map<String, MaskConfig> allConfigs = getAllMaskConfigs();
        
        for (String configuredPackage : allConfigs.keySet()) {
            if (!installedPackages.contains(configuredPackage)) {
                removeMaskConfig(configuredPackage);
            }
        }
    }
    
    /**
     * 导出配置到字符串
     * @return 配置字符串
     */
    public String exportConfigs() {
        StringBuilder sb = new StringBuilder();
        Map<String, MaskConfig> configs = getAllMaskConfigs();
        
        sb.append("模拟器遮罩配置导出:\n");
        sb.append("全局启用: ").append(isAutoMaskEnabled()).append("\n\n");
        
        for (Map.Entry<String, MaskConfig> entry : configs.entrySet()) {
            String packageName = entry.getKey();
            MaskConfig config = entry.getValue();
            RetroEmulatorDatabase.EmulatorInfo emulatorInfo = 
                RetroEmulatorDatabase.getEmulatorInfo(packageName);
            
            sb.append("模拟器: ").append(emulatorInfo != null ? emulatorInfo.name : packageName).append("\n");
            sb.append("包名: ").append(packageName).append("\n");
            sb.append("图片ID: ").append(config.pictureId).append("\n");
            sb.append("自动显示: ").append(config.autoShowEnabled).append("\n");
            sb.append("遮罩类型: ").append(config.maskType).append("\n");
            sb.append("描述: ").append(config.description).append("\n\n");
        }
        
        return sb.toString();
    }
}