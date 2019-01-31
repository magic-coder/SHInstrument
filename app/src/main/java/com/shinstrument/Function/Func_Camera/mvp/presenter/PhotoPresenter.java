package com.shinstrument.Function.Func_Camera.mvp.presenter;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;

import com.shinstrument.Function.Func_Camera.mvp.module.IPhotoModule;
import com.shinstrument.Function.Func_Camera.mvp.module.PhotoModuleImpl;
import com.shinstrument.Function.Func_Camera.mvp.view.IPhotoView;


/**
 * Created by zbsz on 2017/6/9.
 */

public class PhotoPresenter {

    private IPhotoView view;

    private static PhotoPresenter instance=null;
    private PhotoPresenter(){}
    public static PhotoPresenter getInstance() {
        if(instance==null)
            instance=new PhotoPresenter();
        return instance;
    }

    public void PhotoPresenterSetView(IPhotoView view) {
        this.view = view;
    }

    IPhotoModule photoModule = new PhotoModuleImpl();

    public void initCamera(){
        photoModule.initCamera();
    }

    public void setParameter(SurfaceHolder surfaceHolder){
        photoModule.setParameter(surfaceHolder);
    }

    public void setDisplay(SurfaceHolder surfaceHolder){
        photoModule.setDisplay(surfaceHolder);
    }

    public void capture(){
        photoModule.capture(new IPhotoModule.IOnSetListener(){
            @Override
            public void onBtnText(String msg) {
                view.onCaremaText(msg);
            }

            @Override
            public void onGetPhoto(Bitmap bmp) {
                view.onGetPhoto(bmp);
            }
        });
    }
    public void close_Camera(){
        photoModule.closeCamera();
    }

    public void getOneShut(){
        photoModule.getOneShut(new IPhotoModule.IOnSetListener(){
            @Override
            public void onBtnText(String msg) {
                view.onCaremaText(msg);
            }

            @Override
            public void onGetPhoto(Bitmap bmp) {
                view.onGetPhoto(bmp);
            }
        });
    }
}