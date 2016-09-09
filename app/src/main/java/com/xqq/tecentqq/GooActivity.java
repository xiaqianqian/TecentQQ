package com.xqq.tecentqq;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.xqq.tecentqq.reminder.GooView;
import com.xqq.tecentqq.utils.Utils;

/**
 * Created by xqq on 2015/11/14.
 */
public class GooActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        GooView gooView = new GooView(GooActivity.this);
        gooView.setOnStateChangeListener(new GooView.OnStateChangeListener() {
            @Override
            public void onDisappear() {
                Utils.showToast(GooActivity.this, "消失了");
            }

            @Override
            public void onReset(boolean isOutOfRange) {
                Utils.showToast(GooActivity.this, "恢复了： " + isOutOfRange);
            }
        });
        setContentView(gooView);
    }
}
