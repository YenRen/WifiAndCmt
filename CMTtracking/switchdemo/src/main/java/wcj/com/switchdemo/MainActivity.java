package wcj.com.switchdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private ToggleButton toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        toggle = (ToggleButton) findViewById(R.id.toggle);

        //指定开关样式
        toggle.setSwitchStyle(R.drawable.bkg_switch, R.drawable.bkg_switch, R.drawable.btn_slip);

        //指定开关的默认状态
        toggle.setSwitchStatus(true);

        //添加开关状态改变监听器
        toggle.setOnSwitchStatusListener(new ToggleButton.OnSwitchStatusListener() {
            @Override
            public void onSwitch(boolean state) {
                if(state){
                    //开启
                    Toast.makeText(getApplicationContext(), "开关开启", Toast.LENGTH_SHORT).show();
                } else {
                    //关闭
                    Toast.makeText(getApplicationContext(), "开关关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
