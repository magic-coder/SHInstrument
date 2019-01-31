package com.shinstrument.Function.Func_Camera.mvp.module;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

/**
 * Created by zbsz on 2017/5/19.
 */


public interface IPhotoModule {

    void initCamera();

    void setParameter(SurfaceHolder surfaceHolder);

    void setDisplay(SurfaceHolder surfaceHolder);

    void capture(IOnSetListener iOnSetListener);//拍照按钮点击事件

    void closeCamera();

    void getOneShut(IOnSetListener iOnSetListener);
    interface IOnSetListener {
        void onBtnText(String msg);//按完按钮后的回调接口

        void onGetPhoto(Bitmap bmp);
    }

}