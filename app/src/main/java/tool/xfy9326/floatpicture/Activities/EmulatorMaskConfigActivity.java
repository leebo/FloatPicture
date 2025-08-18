package tool.xfy9326.floatpicture.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.EmulatorDetector;
import tool.xfy9326.floatpicture.Utils.EmulatorMaskConfig;
import tool.xfy9326.floatpicture.View.EmulatorMaskConfigAdapter;

/**
 * 模拟器遮罩配置界面
 */
public class EmulatorMaskConfigActivity extends AppCompatActivity {

    private SwitchMaterial globalSwitch;
    private RecyclerView recyclerView;
    private EmulatorMaskConfigAdapter adapter;
    private EmulatorMaskConfig emulatorMaskConfig;
    private String highlightPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator_mask_config);

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
            actionBar.setTitle("模拟器自动遮罩配置");
        }

        // 初始化配置管理器
        emulatorMaskConfig = new EmulatorMaskConfig(this);

        // 获取高亮包名参数
        highlightPackage = getIntent().getStringExtra("highlight_package");

        // 初始化UI组件
        initializeViews();
        
        // 加载模拟器列表
        loadEmulatorList();
    }

    private void initializeViews() {
        // 全局开关
        globalSwitch = findViewById(R.id.switch_global_auto_mask);
        globalSwitch.setChecked(emulatorMaskConfig.isAutoMaskEnabled());
        globalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            emulatorMaskConfig.setAutoMaskEnabled(isChecked);
            Toast.makeText(this, isChecked ? "已启用模拟器自动遮罩" : "已禁用模拟器自动遮罩", 
                         Toast.LENGTH_SHORT).show();
            
            // 刷新列表状态
            if (adapter != null) {
                adapter.updateGlobalEnabled(isChecked);
            }
        });

        // 模拟器列表
        recyclerView = findViewById(R.id.recycler_view_emulators);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadEmulatorList() {
        // 检测已安装的模拟器
        List<EmulatorDetector.InstalledEmulator> installedEmulators = 
            EmulatorDetector.detectInstalledEmulators(this);

        if (installedEmulators.isEmpty()) {
            Toast.makeText(this, "未检测到已安装的复古游戏模拟器", Toast.LENGTH_LONG).show();
            return;
        }

        // 为已安装的模拟器创建默认配置
        emulatorMaskConfig.createDefaultConfigsForInstalledEmulators(installedEmulators);

        // 创建并设置适配器
        adapter = new EmulatorMaskConfigAdapter(this, installedEmulators, emulatorMaskConfig);
        recyclerView.setAdapter(adapter);

        // 如果有高亮包名，滚动到对应位置
        if (highlightPackage != null) {
            for (int i = 0; i < installedEmulators.size(); i++) {
                if (installedEmulators.get(i).emulatorInfo.packageName.equals(highlightPackage)) {
                    recyclerView.scrollToPosition(i);
                    break;
                }
            }
        }

        Toast.makeText(this, "检测到 " + installedEmulators.size() + " 个模拟器", 
                     Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理不再存在的模拟器配置
        if (emulatorMaskConfig != null) {
            List<String> installedPackages = EmulatorDetector.getInstalledEmulatorPackages(this);
            emulatorMaskConfig.cleanupRemovedEmulatorConfigs(installedPackages);
        }
    }
}