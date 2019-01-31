package com.shinstrument;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;


/**
 * Created by zbsz on 2017/7/16.
 */


public class AddPersonWindow extends PopupWindow implements View.OnClickListener{

    private View mContentView;
    private Activity mActivity;
    OptionTypeListener listener;
    Button btn_server;
    Button btn_staticIP;

    public AddPersonWindow(Activity activity) {
        mActivity = activity;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mContentView = LayoutInflater.from(activity).inflate(R.layout.window_person, null);
        setContentView(mContentView);
        btn_server = (Button) mContentView.findViewById(R.id.btn_ServerInput);
        btn_staticIP = (Button) mContentView.findViewById(R.id.btn_StaticIP);
        btn_server.setOnClickListener(this);
        btn_staticIP.setOnClickListener(this);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setAnimationStyle(R.style.Person_type_Popup);
        setOnDismissListener(new OnDismissListener(){
            @Override
            public void onDismiss() {
                lighton();
            }
        });
    }


    private void lighton() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = 1.0f;
        mActivity.getWindow().setAttributes(lp);
    }

    private void lightoff() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();

        lp.alpha = 0.3f;
        mActivity.getWindow().setAttributes(lp);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        lightoff();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        lightoff();
        super.showAtLocation(parent, gravity, x, y);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ServerInput :
                listener.onOptionType(btn_server,1);
                break;
            case R.id.btn_StaticIP :
                listener.onOptionType(btn_staticIP,2);
                break;
        }
    }

    public interface OptionTypeListener {
        void onOptionType(Button view, int type);
    }

    public void setOptionTypeListener(OptionTypeListener listener){
        this.listener = listener;
    }

}

