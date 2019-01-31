package com.shinstrument.Function.Func_Face.mvp.view;

import android.graphics.Bitmap;

import com.baidu.aip.entity.User;
import com.shinstrument.Function.Func_Face.mvp.Module.FaceImpl;


public interface IFaceView {
    void onText(FaceImpl.resultType resultType, String text);

    void onBitmap(FaceImpl.resultType resultType ,Bitmap bitmap);

    void onUser(FaceImpl.resultType resultType,User user);

}
