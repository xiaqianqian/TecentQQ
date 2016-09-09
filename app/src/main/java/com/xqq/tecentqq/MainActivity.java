package com.xqq.tecentqq;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.xqq.tecentqq.drag.DragLayout;
import com.xqq.tecentqq.drag.MyRelativeLayout;

import java.util.Random;

public class MainActivity extends Activity {

    private ListView lvMain;
    private ListView lvLeft;
    private DragLayout dl;
    private ImageView ivHeader;
    private MyRelativeLayout rlMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initView();

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(MainActivity.this, GooActivity.class));
            }
        });
    }

    /**
     * 初始化界面
     */
    private void initView() {
        findViewById();
        rlMain.setDragLayout(dl);

        initData();

        ArrayAdapter<String> mainAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, initData());
        lvMain.setAdapter(mainAdapter);

        ArrayAdapter<String> leftAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, initData()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = (TextView) view;
                mText.setTextColor(Color.WHITE);
                return view;
            }
        };
        lvLeft.setAdapter(leftAdapter);

        dl.setOnDragStateChangeListener(new DragLayout.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                Toast.makeText(getApplicationContext(), "onOpen", Toast.LENGTH_SHORT).show();
                //将左面板上的listView 定到某一条
                lvLeft.smoothScrollToPosition(new Random().nextInt(10));
            }

            @Override
            public void onClose() {
                Toast.makeText(getApplicationContext(), "onClose", Toast.LENGTH_SHORT).show();
                //主面板上的ivHeader  左右震动
                ObjectAnimator mAnim = ObjectAnimator.ofFloat(ivHeader, "translationX", 15f);
                mAnim.setDuration(500);
                mAnim.setInterpolator(new CycleInterpolator(4.0f));//震动4圈
                mAnim.start();
            }

            @Override
            public void onDraging(float percent) {
                ViewHelper.setAlpha(ivHeader, 1 - percent);//从看得见到看不见
            }
        });
    }

    /**
     * 找到界面上的控件
     */
    private void findViewById() {
        lvMain = (ListView) findViewById(R.id.lv_main);
        lvLeft = (ListView) findViewById(R.id.lv_left);
        dl = (DragLayout) findViewById(R.id.dl);
        ivHeader = (ImageView) findViewById(R.id.iv_header);
        rlMain = (MyRelativeLayout)findViewById(R.id.rl_main);
    }

    private String[] initData() {
        String[] contentsMain = {
                "1", "2", "3", "4", "1", "2", "3", "4", "1", "2", "3", "4"
        };
        return contentsMain;
    }
}
