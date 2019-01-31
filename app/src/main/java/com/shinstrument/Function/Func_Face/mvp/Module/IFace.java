package com.shinstrument.Function.Func_Face.mvp.Module;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.manager.FaceSDKManager;

import cbdi.drv.card.ICardInfo;


public interface IFace {

    void FaceInit(Context context, FaceSDKManager.SdkInitListener listener );

    void CameraInit(Context context, PreviewView previewView , TextureView textureView, IFaceListener listener);

    void FaceIdentify();

    void FaceReg(ICardInfo cardInfo,Bitmap bitmap) ;

    void Face_to_IMG(Bitmap bitmap);

    void FaceOnActivityStart();

    void FaceOnActivityStop();

    void FaceOnActivityDestroy();

    void FaceIdentifyReady();

    interface IFaceListener{
        void onText(FaceImpl.resultType resultType, String text);

        void onBitmap(FaceImpl.resultType resultType,Bitmap bitmap);

        void onUser(FaceImpl.resultType resultType,User user);

    }

}
