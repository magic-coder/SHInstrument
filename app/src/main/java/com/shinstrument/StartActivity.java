package com.shinstrument;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.shinstrument.Config.GDMB_Config;
import com.shinstrument.Config.HuBeiWeiHua_Config;
import com.shinstrument.Config.SHDMJ_config;
import com.shinstrument.Config.SHGJ_Config;
import com.shinstrument.Config.SH_Config;
import com.shinstrument.Tools.AssetsUtils;
import com.shinstrument.Tools.MediaHelper;

import java.util.regex.Pattern;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private String regEx = "^\\d{4}$";

    private SPUtils config = SPUtils.getInstance("config");

    Pattern pattern = Pattern.compile(regEx);

    @BindView(R.id.dev_prefix)
    TextView dev_prefix;

    @BindView(R.id.devid_input)
    EditText dev_suffix;

    @OnClick(R.id.next)
    void next() {
        if (pattern.matcher(dev_suffix.getText().toString()).matches()) {
            config.put("firstStart", false);
            config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
            config.put("devid", AppInit.getInstrumentConfig().getDev_prefix() + dev_suffix.getText().toString());
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
            StartActivity.this.finish();
            ToastUtils.showLong("设备ID设置成功");
            //copyFilesToSdCard();
            AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
        } else {
            ToastUtils.showLong("设备ID输入错误，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
        dev_prefix.setText(AppInit.getInstrumentConfig().getDev_prefix());
        MediaHelper.maxVoice();
    }

   /* String SDCardPath = Environment.getExternalStorageDirectory() +"/";
    private void copyFilesToSdCard() {
        copyFileOrDir(""); // copy all files in assets folder in my project
    }

    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Lg.i("tag", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = SDCardPath+ path;
                Lg.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Lg.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Lg.e("tag", "I/O Exception");
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Lg.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName =SDCardPath+filename.substring(0, filename.length()-4);
            else
                newFileName =SDCardPath+filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Lg.e("tag", "Exception in copyFile() of "+newFileName);
            Lg.e("tag", "Exception in copyFile() "+e.toString());
        }

    }*/
}
