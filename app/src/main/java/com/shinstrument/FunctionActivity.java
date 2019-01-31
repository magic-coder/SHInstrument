package com.shinstrument;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceView;

import com.blankj.utilcode.util.BarUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.shinstrument.Alerts.Alert_IP;
import com.shinstrument.Alerts.Alert_Message;
import com.shinstrument.Alerts.Alert_Server;
import com.shinstrument.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.shinstrument.Function.Func_Camera.mvp.view.IPhotoView;
import com.shinstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.shinstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import com.trello.rxlifecycle2.components.RxActivity;

import cbdi.drv.card.ICardInfo;

/**
 * Created by zbsz on 2017/11/27.
 */

public abstract class FunctionActivity extends RxActivity implements IPhotoView, IIDCardView {
    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public PhotoPresenter pp = PhotoPresenter.getInstance();

    public SurfaceView surfaceView;

    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        idp.idCardOpen();
        pp.initCamera();

    }

    @Override
    public void onStart() {
        super.onStart();
        pp.setParameter(surfaceView.getHolder());
    }



    @Override
    public void onRestart() {
        super.onRestart();
        pp.initCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
    }

    @Override
    public void onPause() {
        super.onPause();
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        pp.PhotoPresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        idp.idCardClose();
    }

    @Override
    public void onSetAllMsg(ICardInfo cardInfo, Bitmap bitmap) {

    }

    @Override
    public void onSetText(String Msg) {
        if(alert_message.Showing()){
            ToastUtils.showLong(Msg);
        }
    }
}
