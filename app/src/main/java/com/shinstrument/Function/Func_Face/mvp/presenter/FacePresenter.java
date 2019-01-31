package com.shinstrument.Function.Func_Face.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.manager.FaceSDKManager;
import com.shinstrument.Function.Func_Face.mvp.Module.FaceImpl;
import com.shinstrument.Function.Func_Face.mvp.Module.IFace;
import com.shinstrument.Function.Func_Face.mvp.view.IFaceView;

import cbdi.drv.card.CardInfo;
import cbdi.drv.card.ICardInfo;

public class FacePresenter {
    private IFaceView view;

    private static FacePresenter instance = null;

    private FacePresenter() {
    }

    public static FacePresenter getInstance() {
        if (instance == null)
            instance = new FacePresenter();
        return instance;
    }

    public void FacePresenterSetView(IFaceView view) {
        this.view = view;
    }

    IFace iFace = new FaceImpl();

    public void FaceInit(Context context,FaceSDKManager.SdkInitListener listener){
        iFace.FaceInit(context,listener);
    }

    public void InsRelease(){
        instance = null;
    }

    public void CameraInit(Context context, PreviewView previewView , TextureView textureView){
        iFace.CameraInit(context, previewView, textureView, new IFace.IFaceListener() {

            @Override
            public void onText(FaceImpl.resultType resultType, String text) {
                view.onText(resultType,text);

            }

            @Override
            public void onBitmap(FaceImpl.resultType resultType, Bitmap bitmap) {
                view.onBitmap(resultType,bitmap);
            }

            @Override
            public void onUser(FaceImpl.resultType resultType,User user) {
                view.onUser(resultType,user);
            }
        });
    }

    public void FaceIdentify(){
        iFace.FaceIdentify();
    }

    public void FaceIdentifyReady(){
        iFace.FaceIdentifyReady();
    }


    public void FaceReg(ICardInfo cardInfo,Bitmap bitmap) {
        iFace.FaceReg(cardInfo,bitmap);

    }
    public void Face_to_IMG(Bitmap bitmap){
        iFace.Face_to_IMG(bitmap);
    }

    public void FaceOnActivityStart(){
        iFace.FaceOnActivityStart();
    }

    public void FaceOnActivityStop(){
        iFace.FaceOnActivityStop();
    }

    public void FaceOnActivityDestroy(){
        iFace.FaceOnActivityDestroy();
    }
}
