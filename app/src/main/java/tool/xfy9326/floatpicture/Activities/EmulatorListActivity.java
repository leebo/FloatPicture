package tool.xfy9326.floatpicture.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.EmulatorDetector;
import tool.xfy9326.floatpicture.Utils.RetroEmulatorDatabase;
import tool.xfy9326.floatpicture.View.EmulatorListAdapter;

/**
 * 模拟器列表显示界面
 * 显示所有已知的模拟器，已安装的排在前面
 */
public class EmulatorListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EmulatorListAdapter adapter;
    private TextView statsText;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabSearch;
    private String currentFilter = "全部";

    private List<EmulatorListAdapter.EmulatorItem> allEmulators = new ArrayList<>();
    private List<String> installedPackages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator_list);

        // 设置状态栏和导航栏样式
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
            } else {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        // 初始化工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("模拟器大全");
        }

        // 初始化UI组件
        initializeViews();
        
        // 加载模拟器数据
        loadEmulatorData();
        
        // 设置筛选器
        setupFilters();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_emulators);
        statsText = findViewById(R.id.tv_stats);
        filterChipGroup = findViewById(R.id.chip_group_filters);
        fabSearch = findViewById(R.id.fab_search);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 设置搜索按钮点击事件
        fabSearch.setOnClickListener(v -> showSearchDialog());
    }
    
    private void showSearchDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("搜索模拟器");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("输入模拟器名称或包名");
        builder.setView(input);
        
        builder.setPositiveButton("搜索", (dialog, which) -> {
            String searchText = input.getText().toString().trim();
            if (!searchText.isEmpty()) {
                searchEmulators(searchText);
            }
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void searchEmulators(String searchText) {
        List<EmulatorListAdapter.EmulatorItem> searchResults = new ArrayList<>();
        
        for (EmulatorListAdapter.EmulatorItem item : allEmulators) {
            if (item.emulatorInfo.name.toLowerCase().contains(searchText.toLowerCase()) ||
                item.emulatorInfo.packageName.toLowerCase().contains(searchText.toLowerCase()) ||
                item.emulatorInfo.console.toLowerCase().contains(searchText.toLowerCase()) ||
                item.emulatorInfo.description.toLowerCase().contains(searchText.toLowerCase())) {
                searchResults.add(item);
            }
        }
        
        adapter.updateList(searchResults);
        statsText.setText(String.format("搜索结果: %d 个模拟器", searchResults.size()));
        
        Toast.makeText(this, "找到 " + searchResults.size() + " 个匹配的模拟器", 
                     Toast.LENGTH_SHORT).show();
    }

    private void loadEmulatorData() {
        // 获取已安装的模拟器
        List<EmulatorDetector.InstalledEmulator> installedEmulators = 
            EmulatorDetector.detectInstalledEmulators(this);
        
        // 提取已安装模拟器的包名
        installedPackages.clear();
        for (EmulatorDetector.InstalledEmulator installed : installedEmulators) {
            installedPackages.add(installed.emulatorInfo.packageName);
        }

        // 获取所有模拟器信息
        Map<String, RetroEmulatorDatabase.EmulatorInfo> allEmulatorsMap = 
            RetroEmulatorDatabase.getAllEmulators();

        // 转换为列表项
        allEmulators.clear();
        for (Map.Entry<String, RetroEmulatorDatabase.EmulatorInfo> entry : allEmulatorsMap.entrySet()) {
            RetroEmulatorDatabase.EmulatorInfo info = entry.getValue();
            boolean isInstalled = installedPackages.contains(info.packageName);
            
            // 如果已安装，获取详细信息
            String versionInfo = "";
            if (isInstalled) {
                for (EmulatorDetector.InstalledEmulator installed : installedEmulators) {
                    if (installed.emulatorInfo.packageName.equals(info.packageName)) {
                        versionInfo = installed.versionName;
                        break;
                    }
                }
            }
            
            allEmulators.add(new EmulatorListAdapter.EmulatorItem(info, isInstalled, versionInfo));
        }

        // 排序：已安装的在前，然后按游戏机类型分组，最后按名称排序
        Collections.sort(allEmulators, new Comparator<EmulatorListAdapter.EmulatorItem>() {
            @Override
            public int compare(EmulatorListAdapter.EmulatorItem a, EmulatorListAdapter.EmulatorItem b) {
                // 1. 已安装的优先
                if (a.isInstalled != b.isInstalled) {
                    return a.isInstalled ? -1 : 1;
                }
                
                // 2. 按游戏机类型排序
                int consoleCompare = a.emulatorInfo.console.compareTo(b.emulatorInfo.console);
                if (consoleCompare != 0) {
                    return consoleCompare;
                }
                
                // 3. 按名称排序
                return a.emulatorInfo.name.compareTo(b.emulatorInfo.name);
            }
        });

        // 创建适配器
        adapter = new EmulatorListAdapter(this, allEmulators, this::onEmulatorItemClick);
        recyclerView.setAdapter(adapter);

        // 更新统计信息
        updateStats();
    }

    private void setupFilters() {
        // 收集所有游戏机类型
        List<String> consoles = new ArrayList<>();
        consoles.add("全部");
        consoles.add("已安装");
        
        for (EmulatorListAdapter.EmulatorItem item : allEmulators) {
            String console = item.emulatorInfo.console;
            if (!consoles.contains(console)) {
                consoles.add(console);
            }
        }

        // 创建筛选Chip
        for (String console : consoles) {
            Chip chip = new Chip(this);
            chip.setText(console);
            chip.setCheckable(true);
            
            if (console.equals(currentFilter)) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // 取消其他chip的选中状态
                    for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                        View child = filterChipGroup.getChildAt(i);
                        if (child instanceof Chip && child != buttonView) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                    currentFilter = console;
                    applyFilter();
                } else if (currentFilter.equals(console)) {
                    // 如果取消选中当前筛选器，则选中"全部"
                    chip.setChecked(true);
                }
            });
            
            filterChipGroup.addView(chip);
        }
    }

    private void applyFilter() {
        List<EmulatorListAdapter.EmulatorItem> filteredList = new ArrayList<>();
        
        for (EmulatorListAdapter.EmulatorItem item : allEmulators) {
            if (currentFilter.equals("全部")) {
                filteredList.add(item);
            } else if (currentFilter.equals("已安装") && item.isInstalled) {
                filteredList.add(item);
            } else if (item.emulatorInfo.console.equals(currentFilter)) {
                filteredList.add(item);
            }
        }
        
        adapter.updateList(filteredList);
        updateStatsForFilter(filteredList);
    }

    private void updateStats() {
        int totalCount = allEmulators.size();
        int installedCount = installedPackages.size();
        
        String stats = String.format("共 %d 个模拟器，已安装 %d 个", totalCount, installedCount);
        statsText.setText(stats);
    }

    private void updateStatsForFilter(List<EmulatorListAdapter.EmulatorItem> filteredList) {
        if (currentFilter.equals("全部")) {
            updateStats();
        } else if (currentFilter.equals("已安装")) {
            statsText.setText(String.format("已安装 %d 个模拟器", filteredList.size()));
        } else {
            int installedInCategory = 0;
            for (EmulatorListAdapter.EmulatorItem item : filteredList) {
                if (item.isInstalled) {
                    installedInCategory++;
                }
            }
            String stats = String.format("%s: 共 %d 个，已安装 %d 个", 
                                       currentFilter, filteredList.size(), installedInCategory);
            statsText.setText(stats);
        }
    }

    private void onEmulatorItemClick(EmulatorListAdapter.EmulatorItem item) {
        if (item.isInstalled) {
            // 已安装：显示选项菜单（启动、卸载、查看详情）
            showInstalledEmulatorOptions(item);
        } else {
            // 未安装：尝试在应用商店中搜索
            searchInPlayStore(item.emulatorInfo.packageName, item.emulatorInfo.name);
        }
    }

    private void showInstalledEmulatorOptions(EmulatorListAdapter.EmulatorItem item) {
        String[] options = {"启动应用", "应用详情", "在应用商店查看", "配置遮罩"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = 
            new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(item.emulatorInfo.name)
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // 启动应用
                           launchEmulator(item.emulatorInfo.packageName);
                           break;
                       case 1: // 应用详情
                           showAppDetails(item.emulatorInfo.packageName);
                           break;
                       case 2: // 在应用商店查看
                           searchInPlayStore(item.emulatorInfo.packageName, item.emulatorInfo.name);
                           break;
                       case 3: // 配置遮罩
                           configureEmulatorMask(item.emulatorInfo.packageName);
                           break;
                   }
               });
        builder.show();
    }

    private void launchEmulator(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
            Toast.makeText(this, "正在启动模拟器...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "无法启动该应用", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAppDetails(String packageName) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    private void searchInPlayStore(String packageName, String appName) {
        try {
            // 尝试直接打开应用页面
            Intent intent = new Intent(Intent.ACTION_VIEW, 
                                     Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (Exception e) {
            try {
                // 如果失败，在浏览器中打开Play Store
                Intent intent = new Intent(Intent.ACTION_VIEW, 
                                         Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                startActivity(intent);
            } catch (Exception e2) {
                Toast.makeText(this, "无法打开应用商店", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void configureEmulatorMask(String packageName) {
        Intent intent = new Intent(this, EmulatorMaskConfigActivity.class);
        intent.putExtra("highlight_package", packageName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新检测已安装的模拟器，可能有变化
        loadEmulatorData();
        applyFilter();
    }
}