package tool.xfy9326.floatpicture.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 复古游戏模拟器数据库
 * 包含主流Android复古游戏模拟器的包名和对应信息
 */
public class RetroEmulatorDatabase {
    
    /**
     * 模拟器信息类
     */
    public static class EmulatorInfo {
        public final String name;
        public final String packageName;
        public final String console;
        public final String description;
        
        public EmulatorInfo(String name, String packageName, String console, String description) {
            this.name = name;
            this.packageName = packageName;
            this.console = console;
            this.description = description;
        }
    }
    
    // 模拟器数据库
    private static final Map<String, EmulatorInfo> EMULATOR_DATABASE = new HashMap<>();
    
    static {
        // RetroArch 系列
        EMULATOR_DATABASE.put("com.retroarch", new EmulatorInfo(
            "RetroArch", "com.retroarch", "多平台", "强大的多平台模拟器前端"));
        EMULATOR_DATABASE.put("com.retroarch.aarch64", new EmulatorInfo(
            "RetroArch 64位", "com.retroarch.aarch64", "多平台", "RetroArch 64位版本"));
        EMULATOR_DATABASE.put("com.retroarch.ra32", new EmulatorInfo(
            "RetroArch 32位", "com.retroarch.ra32", "多平台", "RetroArch 32位版本"));
            
        // 任天堂模拟器
        EMULATOR_DATABASE.put("org.mupen64plusae.v3.fzurita", new EmulatorInfo(
            "Mupen64Plus FZ", "org.mupen64plusae.v3.fzurita", "N64", "任天堂64模拟器"));
        EMULATOR_DATABASE.put("com.snes9x.plus", new EmulatorInfo(
            "Snes9x EX+", "com.snes9x.plus", "SNES", "超级任天堂模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.NesEmu", new EmulatorInfo(
            "NES.emu", "com.explusalpha.NesEmu", "NES", "任天堂红白机模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.GbaEmu", new EmulatorInfo(
            "GBA.emu", "com.explusalpha.GbaEmu", "GBA", "Game Boy Advance模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.GbcEmu", new EmulatorInfo(
            "GBC.emu", "com.explusalpha.GbcEmu", "GBC", "Game Boy Color模拟器"));
        EMULATOR_DATABASE.put("com.fastemulator.gba", new EmulatorInfo(
            "My Boy!", "com.fastemulator.gba", "GBA", "Game Boy Advance模拟器"));
        EMULATOR_DATABASE.put("com.fastemulator.gbafree", new EmulatorInfo(
            "My Boy! Free", "com.fastemulator.gbafree", "GBA", "Game Boy Advance模拟器免费版"));
        EMULATOR_DATABASE.put("com.fastemulator.gbc", new EmulatorInfo(
            "My OldBoy!", "com.fastemulator.gbc", "GBC", "Game Boy模拟器"));
        EMULATOR_DATABASE.put("com.fastemulator.gbcfree", new EmulatorInfo(
            "My OldBoy! Free", "com.fastemulator.gbcfree", "GBC", "Game Boy模拟器免费版"));
            
        // 世嘉模拟器
        EMULATOR_DATABASE.put("com.explusalpha.MdEmu", new EmulatorInfo(
            "MD.emu", "com.explusalpha.MdEmu", "MD", "世嘉MD模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.SmsEmu", new EmulatorInfo(
            "SMS.emu", "com.explusalpha.SmsEmu", "SMS", "世嘉Master System模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.SaturnEmu", new EmulatorInfo(
            "Saturn.emu", "com.explusalpha.SaturnEmu", "Saturn", "世嘉土星模拟器"));
        EMULATOR_DATABASE.put("com.reicast.emulator", new EmulatorInfo(
            "Reicast", "com.reicast.emulator", "Dreamcast", "世嘉Dreamcast模拟器"));
        EMULATOR_DATABASE.put("org.flycast.emulator", new EmulatorInfo(
            "Flycast", "org.flycast.emulator", "Dreamcast", "世嘉Dreamcast模拟器"));
            
        // Sony PlayStation 模拟器
        EMULATOR_DATABASE.put("com.explusalpha.PsxEmu", new EmulatorInfo(
            "PSX.emu", "com.explusalpha.PsxEmu", "PSX", "PlayStation 1模拟器"));
        EMULATOR_DATABASE.put("org.pcsx2.emulator", new EmulatorInfo(
            "PCSX2", "org.pcsx2.emulator", "PS2", "PlayStation 2模拟器"));
        EMULATOR_DATABASE.put("com.epsxe.ePSXe", new EmulatorInfo(
            "ePSXe", "com.epsxe.ePSXe", "PSX", "PlayStation模拟器"));
        EMULATOR_DATABASE.put("org.ppsspp.ppsspp", new EmulatorInfo(
            "PPSSPP", "org.ppsspp.ppsspp", "PSP", "PlayStation Portable模拟器"));
        EMULATOR_DATABASE.put("org.ppsspp.ppssppgold", new EmulatorInfo(
            "PPSSPP Gold", "org.ppsspp.ppssppgold", "PSP", "PlayStation Portable模拟器金版"));
            
        // 街机模拟器
        EMULATOR_DATABASE.put("com.seleuco.mame4droid", new EmulatorInfo(
            "MAME4droid", "com.seleuco.mame4droid", "Arcade", "街机模拟器"));
        EMULATOR_DATABASE.put("com.seleuco.mame4droid.reloaded", new EmulatorInfo(
            "MAME4droid Reloaded", "com.seleuco.mame4droid.reloaded", "Arcade", "街机模拟器重制版"));
        EMULATOR_DATABASE.put("com.explusalpha.MameEmu", new EmulatorInfo(
            "MAME.emu", "com.explusalpha.MameEmu", "Arcade", "街机模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.NeoEmu", new EmulatorInfo(
            "NEO.emu", "com.explusalpha.NeoEmu", "NeoGeo", "Neo Geo模拟器"));
            
        // 任天堂GameCube/Wii模拟器
        EMULATOR_DATABASE.put("org.dolphinemu.dolphinemu", new EmulatorInfo(
            "Dolphin", "org.dolphinemu.dolphinemu", "GC/Wii", "GameCube和Wii模拟器"));
            
        // 任天堂3DS模拟器
        EMULATOR_DATABASE.put("org.citra.citra_emu", new EmulatorInfo(
            "Citra", "org.citra.citra_emu", "3DS", "任天堂3DS模拟器"));
        EMULATOR_DATABASE.put("org.citra.citra_emu.canary", new EmulatorInfo(
            "Citra Canary", "org.citra.citra_emu.canary", "3DS", "任天堂3DS模拟器开发版"));
            
        // 任天堂Switch模拟器
        EMULATOR_DATABASE.put("org.yuzu.yuzu_emu", new EmulatorInfo(
            "Yuzu", "org.yuzu.yuzu_emu", "Switch", "任天堂Switch模拟器"));
        EMULATOR_DATABASE.put("org.ryujinx.android", new EmulatorInfo(
            "Ryujinx", "org.ryujinx.android", "Switch", "任天堂Switch模拟器"));
            
        // PC Engine模拟器
        EMULATOR_DATABASE.put("com.explusalpha.PceEmu", new EmulatorInfo(
            "PCE.emu", "com.explusalpha.PceEmu", "PCE", "PC Engine模拟器"));
            
        // Atari模拟器
        EMULATOR_DATABASE.put("com.explusalpha.A2600Emu", new EmulatorInfo(
            "2600.emu", "com.explusalpha.A2600Emu", "Atari2600", "Atari 2600模拟器"));
        EMULATOR_DATABASE.put("com.explusalpha.LynxEmu", new EmulatorInfo(
            "Lynx.emu", "com.explusalpha.LynxEmu", "Lynx", "Atari Lynx模拟器"));
            
        // WonderSwan模拟器
        EMULATOR_DATABASE.put("com.explusalpha.WsEmu", new EmulatorInfo(
            "WS.emu", "com.explusalpha.WsEmu", "WonderSwan", "WonderSwan模拟器"));
            
        // MSX模拟器
        EMULATOR_DATABASE.put("com.explusalpha.MsxEmu", new EmulatorInfo(
            "MSX.emu", "com.explusalpha.MsxEmu", "MSX", "MSX电脑模拟器"));
            
        // Commodore 64模拟器
        EMULATOR_DATABASE.put("com.explusalpha.C64Emu", new EmulatorInfo(
            "C64.emu", "com.explusalpha.C64Emu", "C64", "Commodore 64模拟器"));
            
        // 其他流行模拟器
        EMULATOR_DATABASE.put("com.johnemulators.johngbc", new EmulatorInfo(
            "John GBC", "com.johnemulators.johngbc", "GBC", "Game Boy Color模拟器"));
        EMULATOR_DATABASE.put("com.johnemulators.johngba", new EmulatorInfo(
            "John GBA", "com.johnemulators.johngba", "GBA", "Game Boy Advance模拟器"));
        EMULATOR_DATABASE.put("com.johnemulators.johnnes", new EmulatorInfo(
            "John NES", "com.johnemulators.johnnes", "NES", "任天堂红白机模拟器"));
        EMULATOR_DATABASE.put("com.nostalgia.nosnes", new EmulatorInfo(
            "Nostalgia.NES", "com.nostalgia.nosnes", "NES", "任天堂红白机模拟器"));
        EMULATOR_DATABASE.put("ca.halsafar.BizHawkDroid", new EmulatorInfo(
            "BizHawk", "ca.halsafar.BizHawkDroid", "多平台", "多平台模拟器"));
        EMULATOR_DATABASE.put("com.lemuroid.app", new EmulatorInfo(
            "Lemuroid", "com.lemuroid.app", "多平台", "开源多平台模拟器"));
        EMULATOR_DATABASE.put("com.digdroid.alman.dig", new EmulatorInfo(
            "DIG", "com.digdroid.alman.dig", "前端", "游戏启动器前端"));
        EMULATOR_DATABASE.put("com.hyperspin.hyperspin", new EmulatorInfo(
            "HyperSpin", "com.hyperspin.hyperspin", "前端", "游戏启动器前端"));
        EMULATOR_DATABASE.put("com.bambuna.launchbox", new EmulatorInfo(
            "LaunchBox", "com.bambuna.launchbox", "前端", "游戏启动器前端"));
        EMULATOR_DATABASE.put("com.draco.gameboy", new EmulatorInfo(
            "Pizza Boy GBA", "com.draco.gameboy", "GBA", "Game Boy Advance模拟器"));
        EMULATOR_DATABASE.put("com.draco.gbcoid", new EmulatorInfo(
            "Pizza Boy GBC", "com.draco.gbcoid", "GBC", "Game Boy Color模拟器"));
    }
    
    /**
     * 获取所有模拟器信息
     */
    public static Map<String, EmulatorInfo> getAllEmulators() {
        return new HashMap<>(EMULATOR_DATABASE);
    }
    
    /**
     * 根据包名获取模拟器信息
     */
    public static EmulatorInfo getEmulatorInfo(String packageName) {
        return EMULATOR_DATABASE.get(packageName);
    }
    
    /**
     * 检查是否是已知的模拟器
     */
    public static boolean isKnownEmulator(String packageName) {
        return EMULATOR_DATABASE.containsKey(packageName);
    }
    
    /**
     * 获取所有模拟器包名
     */
    public static String[] getAllPackageNames() {
        return EMULATOR_DATABASE.keySet().toArray(new String[0]);
    }
    
    /**
     * 根据游戏机类型获取模拟器
     */
    public static Map<String, EmulatorInfo> getEmulatorsByConsole(String console) {
        Map<String, EmulatorInfo> result = new HashMap<>();
        for (Map.Entry<String, EmulatorInfo> entry : EMULATOR_DATABASE.entrySet()) {
            if (entry.getValue().console.equals(console)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}