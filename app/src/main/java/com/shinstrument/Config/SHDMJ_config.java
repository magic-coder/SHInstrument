package com.shinstrument.Config;

public class SHDMJ_config extends BaseConfig {
    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return false;
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }

    @Override
    public String getServerId() {
        return "http://shjd.snaq.cn:47002/";
    }

    @Override
    public String getUpDataPrefix() {
        return "da_gzmb_updata?";
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
        return "CBDI-ID-P-H";
    }

    @Override
    public String getName() {
        return "库房采集器";
    }

    @Override
    public String getProject() {
        return "SHFB";
    }

    @Override
    public String getPower() {
        return "12-18V 2A";
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
