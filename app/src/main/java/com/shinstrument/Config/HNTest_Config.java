package com.shinstrument.Config;

public class HNTest_Config extends BaseConfig {
    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return false;
    }


//    @Override
//    public String getPersonInfoPrefix() {
//        return "da_gzmb_persionInfo?";
//    }
//
//    @Override
//    public String getServerId() {
//        return "http://124.172.232.87:8802/";
//    }
//
//    @Override
//    public String getUpDataPrefix() {
//        return "da_gzmb_updata?";
//    }


    @Override
    public String getPersonInfoPrefix() {
        return "fbcjy_updata?";
    }

    @Override
    public String getServerId() {
        return "http://hnyzb.wxhxp.cn:1093/cjy/s/";
    }

    @Override
    public String getUpDataPrefix() {
        return "fbcjy_updata?";
    }

    @Override
    public String getDev_prefix() {
        return "800200";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-ID";
    }

    @Override
    public String getName() {
        return "数据采集器";
    }

    @Override
    public String getProject() {
        return "HNJD";
    }

    @Override
    public String getPower() {
        return "12V 2A";
    }

    @Override
    public boolean isCheckTime() {
        return false;
    }

    @Override
    public boolean isGetOneShot() {
        return true;
    }

    @Override
    public boolean disAlarm() {
        return true;
    }

    @Override
    public boolean collectBox() {
        return false;
    }

    @Override
    public boolean noise() {
        return false;
    }
}
