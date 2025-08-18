package tool.xfy9326.floatpicture.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟器检测器
 * 检测已安装的复古游戏模拟器
 */
public class EmulatorDetector {
    
    /**
     * 已安装的模拟器信息
     */
    public static class InstalledEmulator {
        public final RetroEmulatorDatabase.EmulatorInfo emulatorInfo;
        public final String appName;
        public final String versionName;
        public final int versionCode;
        public final boolean isEnabled;
        
        public InstalledEmulator(RetroEmulatorDatabase.EmulatorInfo emulatorInfo, 
                               String appName, String versionName, int versionCode, boolean isEnabled) {
            this.emulatorInfo = emulatorInfo;
            this.appName = appName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.isEnabled = isEnabled;
        }
    }
    
    /**
     * 检测所有已安装的模拟器
     * @param context 上下文
     * @return 已安装的模拟器列表
     */
    public static List<InstalledEmulator> detectInstalledEmulators(Context context) {
        List<InstalledEmulator> installedEmulators = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        
        // 获取所有已知模拟器包名
        Map<String, RetroEmulatorDatabase.EmulatorInfo> allEmulators = 
            RetroEmulatorDatabase.getAllEmulators();
        
        for (Map.Entry<String, RetroEmulatorDatabase.EmulatorInfo> entry : allEmulators.entrySet()) {
            String packageName = entry.getKey();
            RetroEmulatorDatabase.EmulatorInfo emulatorInfo = entry.getValue();
            
            try {
                // 检查应用是否已安装
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                
                // 获取应用名称
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                
                // 检查应用是否启用
                boolean isEnabled = appInfo.enabled;
                
                // 创建已安装模拟器信息
                InstalledEmulator installedEmulator = new InstalledEmulator(
                    emulatorInfo,
                    appName,
                    packageInfo.versionName != null ? packageInfo.versionName : "未知",
                    packageInfo.versionCode,
                    isEnabled
                );
                
                installedEmulators.add(installedEmulator);
                
            } catch (PackageManager.NameNotFoundException e) {
                // 应用未安装，跳过
            }
        }
        
        return installedEmulators;
    }
    
    /**
     * 检查特定模拟器是否已安装
     * @param context 上下文
     * @param packageName 包名
     * @return 是否已安装
     */
    public static boolean isEmulatorInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 获取已安装模拟器的包名列表
     * @param context 上下文
     * @return 包名列表
     */
    public static List<String> getInstalledEmulatorPackages(Context context) {
        List<String> packageNames = new ArrayList<>();
        List<InstalledEmulator> installedEmulators = detectInstalledEmulators(context);
        
        for (InstalledEmulator emulator : installedEmulators) {
            packageNames.add(emulator.emulatorInfo.packageName);
        }
        
        return packageNames;
    }
    
    /**
     * 按游戏机类型分组已安装的模拟器
     * @param context 上下文
     * @return 按游戏机类型分组的模拟器Map
     */
    public static Map<String, List<InstalledEmulator>> getInstalledEmulatorsByConsole(Context context) {
        Map<String, List<InstalledEmulator>> groupedEmulators = new HashMap<>();
        List<InstalledEmulator> installedEmulators = detectInstalledEmulators(context);
        
        for (InstalledEmulator emulator : installedEmulators) {
            String console = emulator.emulatorInfo.console;
            
            if (!groupedEmulators.containsKey(console)) {
                groupedEmulators.put(console, new ArrayList<>());
            }
            
            groupedEmulators.get(console).add(emulator);
        }
        
        return groupedEmulators;
    }
    
    /**
     * 检查给定的包名是否属于已安装的模拟器
     * @param context 上下文
     * @param packageName 要检查的包名
     * @return 如果是已安装的模拟器返回EmulatorInfo，否则返回null
     */
    public static RetroEmulatorDatabase.EmulatorInfo checkIfInstalledEmulator(Context context, String packageName) {
        if (RetroEmulatorDatabase.isKnownEmulator(packageName) && 
            isEmulatorInstalled(context, packageName)) {
            return RetroEmulatorDatabase.getEmulatorInfo(packageName);
        }
        return null;
    }
    
    /**
     * 获取模拟器统计信息
     * @param context 上下文
     * @return 统计信息字符串
     */
    public static String getEmulatorStatistics(Context context) {
        List<InstalledEmulator> installedEmulators = detectInstalledEmulators(context);
        Map<String, Integer> consoleCount = new HashMap<>();
        
        for (InstalledEmulator emulator : installedEmulators) {
            String console = emulator.emulatorInfo.console;
            consoleCount.put(console, consoleCount.getOrDefault(console, 0) + 1);
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("已安装模拟器统计:\n");
        stats.append("总数: ").append(installedEmulators.size()).append("\n");
        
        for (Map.Entry<String, Integer> entry : consoleCount.entrySet()) {
            stats.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return stats.toString();
    }
}