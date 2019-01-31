package com.shinstrument;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.baidu.aip.manager.FaceSDKManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.shinstrument.Config.GDMB_Config;
import com.shinstrument.Config.HuBeiWeiHua_Config;
import com.shinstrument.Config.SHDMJ_config;
import com.shinstrument.Config.SHGJ_Config;
import com.shinstrument.Config.SH_Config;
import com.shinstrument.Function.Func_Face.mvp.presenter.FacePresenter;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by zbsz on 2017/12/8.
 */

public class SplashActivity extends Activity {
    private static final String PREFS_NAME = "config";

    SPUtils SP_Config = SPUtils.getInstance(PREFS_NAME);

    private Handler handler = new Handler(Looper.getMainLooper());

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showLong(text);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_splash);
        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            copyToClipboard(AppInit.getContext(), FileIOUtils.readFile2String(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        FacePresenter.getInstance().FaceInit(this, new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                toast("sdk init start");
            }

            @Override
            public void initSuccess() {
                toast("sdk init success");
                if (SP_Config.getBoolean("firstStart", true)) {
                    ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
                    SplashActivity.this.finish();
                }else {
                    if(SP_Config.getString("devid").substring(6,7).equals(String.valueOf(1))){
                        SP_Config.put("devid",SP_Config.getString("devid").substring(0,6)+"3"+SP_Config.getString("devid").substring(7,10));
                        ToastUtils.showLong("设备号已成功转换");
                    }
                    if("http://115.159.241.118:8009/".equals(SP_Config.getString("ServerId"))){
                        SP_Config.put("ServerId","https://gdmb.wxhxp.cn:8009/");
                    }
                    Observable.timer(5,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    if(AppInit.getInstrumentConfig().getClass().getName().equals(HuBeiWeiHua_Config.class.getName())){
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_HuBeiWeiHuaActivity");
                                    }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SH_Config.class.getName())){
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_ShangHaiActivity");
                                    }else if(AppInit.getInstrumentConfig().getClass().getName().equals(GDMB_Config.class.getName())){
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_FaceBySHActivity");
                                    }else if(AppInit.getInstrumentConfig().getClass().getName().equals(SHGJ_Config.class.getName())){
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_SHGJActivity");
                                    }else{
                                        //ActivityUtils.startActivity(getPackageName(), getPackageName() + ".MainActivity");
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".CBSD_CommonActivity");
                                    }
                                    SplashActivity.this.finish();
                                }
                            });

                }
            }

            @Override
            public void initFail(int errorCode, String msg) {
                toast("sdk init fail:" + msg);
            }
        });


    }
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FacePresenter.getInstance().InsRelease();
    }
}
