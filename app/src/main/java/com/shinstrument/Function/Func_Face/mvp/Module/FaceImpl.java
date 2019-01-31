package com.shinstrument.Function.Func_Face.mvp.Module;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.blankj.utilcode.util.ToastUtils;
import com.shinstrument.Tools.MediaHelper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cbdi.drv.card.ICardInfo;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FaceImpl implements IFace {

    private final static double livnessScore = 0.2;

    public enum action {
        No_ACTION, Reg_ACTION, Identify_ACTION, IMG_MATCH_IMG
    }

    public enum resultType {
        Reg, Identify, IMG_MATCH_IMG_False, IMG_MATCH_IMG_True, IMG_MATCH_IMG_Error,
    }

    static private FaceDetectManager faceDetectManager ;

    PreviewView mPreviewView;

    TextureView mTextureView;

    static CameraImageSource cameraImageSource ;

    IFaceListener listener;

    static action action = FaceImpl.action.No_ACTION;
    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    @Override
    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
         new AntileakHandler(context,listener).sendEmptyMessage(0x123);//防止内存泄露
//        PreferencesUtil.initPrefs(context);
//        // 使用人脸1：n时使用
//        DBManager.getInstance().init(context);
//        livnessTypeTip();
//        FaceSDKManager.getInstance().init(context);
//        FaceSDKManager.getInstance().setSdkInitListener(listener);

    }


    @Override
    public void CameraInit(Context context, PreviewView previewView, TextureView textureView, IFaceListener listener) {
        mPreviewView = previewView;
        mTextureView = textureView;
        this.listener = listener;
        faceDetectManager = new FaceDetectManager(context);
        // 从系统相机获取图片帧。
        cameraImageSource = new CameraImageSource(context);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);
        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        // 设置预览
        cameraImageSource.setPreviewView(mPreviewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        faceDetectManager.setUseDetect(true);
        mTextureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        mTextureView.setKeepScreenOn(true);
        boolean isPortrait = context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            mPreviewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            mPreviewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }
        setCameraType(cameraImageSource);
        addListener();
    }

    Bitmap InputBitmap;

    ICardInfo InputCardInfo;

    @Override
    public void Face_to_IMG(Bitmap bitmap) {
        action = FaceImpl.action.IMG_MATCH_IMG;
        this.InputBitmap = bitmap;
    }

    @Override
    public void FaceOnActivityStop() {
        faceDetectManager.stop();
    }

    @Override
    public void FaceOnActivityStart() {
        faceDetectManager.start();
    }

    @Override
    public void FaceOnActivityDestroy() {
        //mContext = null;
    }

    @Override
    public void FaceIdentify() {
        action = FaceImpl.action.Identify_ACTION;
    }

    @Override
    public void FaceIdentifyReady() {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // android.os.Process.setThreadPriority (-4);
                FaceApi.getInstance().loadFacesFromDB("1");
                int count = FaceApi.getInstance().getGroup2Facesets().get("1").size();
                Log.e("人脸库内人脸数目:", String.valueOf(count));
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    @Override
    public void FaceReg(ICardInfo cardInfo, Bitmap bitmap) {
        action = FaceImpl.action.Reg_ACTION;
        this.InputBitmap = bitmap;
        this.InputCardInfo = cardInfo;
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
        // cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);

        // TODO 选择使用usb摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        // 如果不设置，人脸框会镜像，显示不准
        mPreviewView.getTextureView().setScaleX(-1);

        // TODO 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//        previewView.getTextureView().setScaleX(-1);
    }

    private void addListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, final FaceInfo[] infos, final ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断

                switch (FaceImpl.action) {
                    case Identify_ACTION:
                        if (retCode == FaceTracker.ErrCode.OK.ordinal() && infos != null) {
                            es.submit(new Runnable() {
                                @Override
                                public void run() {
                                    asyncIdentity(frame, infos);
                                }
                            });
                        }
                        break;
                    default:
                        checkFace(retCode, infos, frame);
                }

                showFrame(frame, infos);
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private void toast(final String msg) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showLong(msg);
            }
        });

    }

    private static class AntileakHandler extends Handler {
        WeakReference<Context> reference;
        WeakReference<FaceSDKManager.SdkInitListener> weaklistener;
        public AntileakHandler(Context activity,FaceSDKManager.SdkInitListener sdkInitListener) {
            reference = new WeakReference<Context>(activity);
            weaklistener = new WeakReference<FaceSDKManager.SdkInitListener>(sdkInitListener);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    Context activity = reference.get();
                    if (activity != null) {
                        PreferencesUtil.initPrefs(activity);
                        // 使用人脸1：n时使用
                        DBManager.getInstance().init(activity);
                        livnessTypeTip();
                        FaceSDKManager.getInstance().init(activity);
                        FaceSDKManager.getInstance().setSdkInitListener(weaklistener.get());
                        break;
                    }
                default:
                    break;
            }
        }
    }

    private void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if (retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);
            //toast(tip);
        } else {
            String tip = checkFaceCode(retCode);
            //toast(tip);
        }
    }

    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {

        String tip = "";
        if (faceInfo.mConf < 0.6) {
            tip = "人脸置信度太低";
            return tip;
        }

        float[] headPose = faceInfo.headPose;
        if (Math.abs(headPose[0]) > 20 || Math.abs(headPose[1]) > 20 || Math.abs(headPose[2]) > 20) {
            tip = "人脸置角度太大，请正对屏幕";
            return tip;
        }

        int width = imageFrame.getWidth();
        int height = imageFrame.getHeight();
        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.mWidth / (float) height;
        Log.i("liveness_ratio", "ratio=" + ratio);
        if (ratio > 0.6) {
            tip = "人脸离屏幕太近，请调整与屏幕的距离";
            return tip;
        } else if (ratio < 0.2) {
            tip = "人脸离屏幕太远，请调整与屏幕的距离";
            return tip;
        } else if (faceInfo.mCenter_x > width * 3 / 4) {
            tip = "人脸在屏幕中太靠右";
            return tip;
        } else if (faceInfo.mCenter_x < width / 4) {
            tip = "人脸在屏幕中太靠左";
            return tip;
        } else if (faceInfo.mCenter_y > height * 3 / 4) {
            tip = "人脸在屏幕中太靠下";
            return tip;
        } else if (faceInfo.mCenter_x < height / 4) {
            tip = "人脸在屏幕中太靠上";
            return tip;
        }
        switch (action) {
            case No_ACTION:
                break;
            case Reg_ACTION:
                if ((rgbLiveness(imageFrame, faceInfo)) > livnessScore) {
                    action = FaceImpl.action.No_ACTION;
                    if (Img_match_Img(faceInfo, imageFrame)) {
                        register(faceInfo, imageFrame, InputCardInfo);
                    }
                } else {
                    toast(String.valueOf("活体检测分数过低"));
                }
                break;
            case IMG_MATCH_IMG:
                if ((rgbLiveness(imageFrame, faceInfo)) > livnessScore) {
                    action = FaceImpl.action.No_ACTION;
                    Img_match_Img(faceInfo, imageFrame);
                } else {
                    toast(String.valueOf("活体检测分数过低"));
                }
                break;
            case Identify_ACTION:
                break;
            default:
                break;
        }
        //saveFace(faceInfo, imageFrame);
        return tip;
    }

    private String checkFaceCode(int errCode) {
        String tip = "";
        if (errCode == FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal()) {
//            tip = "未检测到人脸";
        } else if (errCode == FaceTracker.ErrCode.IMG_BLURED.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal()) {
            tip = "请静止平视屏幕";
        } else if (errCode == FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal()) {
            tip = "光线太暗，请到更明亮的地方";
        } else if (errCode == FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()) {
            tip = "未检测到人脸";
        }
        return tip;
    }


    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {
        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
        return rgbScore;
    }

    RectF rectF = new RectF();
    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
        paint.setStrokeWidth(5);
    }

    private void showFrame(ImageFrame imageFrame, FaceInfo[] faceInfos) {
        Canvas canvas = mTextureView.lockCanvas();
        if (canvas == null) {
            mTextureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mTextureView.unlockCanvasAndPost(canvas);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];

        rectF.set(getFaceRect(faceInfo, imageFrame));

        // 检测图片的坐标和显示的坐标不一样，需要转换。
        mPreviewView.mapFromOriginalRect(rectF);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);
            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
        } else {
            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
        canvas.drawRect(rectF, paint);
        mTextureView.unlockCanvasAndPost(canvas);
    }


    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);

        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 4 / 3;
        //
        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height / 2;
        //
        //            rect.top = top;
        //            rect.left = left;
        //            rect.right = left + width;
        //            rect.bottom = top + height;

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 5 / 3;
        int width = (right - left);
        int height = (bottom - top);

        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height * 2 / 3;
        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height / 2);

        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);
        return rect;
    }

    public static final String TYPE_LIVENSS = "TYPE_LIVENSS";
    public static final int TYPE_RGB_LIVENSS = 2;

    private static void livnessTypeTip() {
        PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_LIVENSS);
    }

    private boolean Img_match_Img(FaceInfo faceInfo, ImageFrame imageFrame) {
        final Disposable outOfTime = Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.onText(resultType.IMG_MATCH_IMG_Error, "人员登记已超出时间，请重试");
                    }
                });
        final byte[] bytes1 = new byte[2048];
        final byte[] bytes2 = new byte[2048];
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        int ret1 = FaceApi.getInstance().getFeature(bitmap, bytes1, 50);
        if (ret1 == 512) {
            int ret2 = FaceApi.getInstance().getFeature(InputBitmap, bytes2, 50);
            if (ret2 == 512) {
                if (FaceApi.getInstance().match(bytes1, bytes2) > 50) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(resultType.IMG_MATCH_IMG_True, bitmap);
                            listener.onText(resultType.IMG_MATCH_IMG_True, "人证比对通过");
                            outOfTime.dispose();
                        }
                    });
                    return true;
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(resultType.IMG_MATCH_IMG_False, bitmap);
                            listener.onText(resultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
                            outOfTime.dispose();

                        }
                    });
                }
            }
        } else if (ret1 == -100) {
            toast("未完成人脸比对，可能原因，图片为空");
            listener.onText(resultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();
        } else if (ret1 == -102) {
            toast("未完成人脸比对，可能原因，图片未检测到人脸");
            listener.onText(resultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();

        } else {
            toast("未完成人脸比对，可能原因，"
                    + "人脸太小（小于sdk初始化设置的最小检测人脸）"
                    + "人脸不是朝上，sdk不能检测出人脸");
            listener.onText(resultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();
        }
        return false;
    }

    private void register(final FaceInfo faceInfo, final ImageFrame imageFrame, final ICardInfo cardInfo) {
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
        // String uid = 修改为自己用户系统中用户的id;
        final User user = new User();
        user.setUserId(cardInfo.cardId());
        user.setUserInfo(cardInfo.name());
        user.setGroupId("1");
        es.submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                byte[] bytes = new byte[2048];
                int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
                if (ret == FaceDetector.NO_FACE_DETECTED) {
                    toast("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
                } else if (ret != -1) {
                    Feature feature = new Feature();
                    feature.setGroupId("1");
                    feature.setUserId(cardInfo.cardId());
                    feature.setFeature(bytes);
                    feature.setImageName(cardInfo.cardId());
                    user.getFeatureList().add(feature);
                    if (FaceApi.getInstance().userAdd(user)) {
                        MediaHelper.play(MediaHelper.Text.Reg_success);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                identityStatus = FEATURE_DATAS_UNREADY;
                                listener.onUser(resultType.Reg, user);
                                listener.onText(resultType.Reg, "success");
                            }
                        });
                    } else {
                        MediaHelper.play(MediaHelper.Text.Reg_failed);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onText(resultType.Reg, "failed");
                            }
                        });
                    }
                } else {
                    toast("抽取特征失败");
                }
            }
        });
    }


    private ExecutorService es = Executors.newSingleThreadExecutor();

    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            return;
        }
        if (rgbLiveness(imageFrame, faceInfos[0]) > livnessScore) {
            identity(imageFrame, faceInfos[0]);
        } else {
            // toast("rgb活体分数过低");
        }
    }

    private void identity(final ImageFrame imageFrame, final FaceInfo faceInfo) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }
        identityStatus = IDENTITYING;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        final IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, "1");
        //displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());
        if (identifyRet.getScore() < 80) {
            identityStatus = IDENTITY_IDLE;
            return;
        } else {
            final User user = FaceApi.getInstance().getUserInfo("1", identifyRet.getUserId());
            if (user == null) {
                identityStatus = IDENTITY_IDLE;
                return;
            } else {
                Observable.timer(5, TimeUnit.SECONDS).observeOn(Schedulers.from(es))
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                identityStatus = IDENTITY_IDLE;
                            }
                        });
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onBitmap(resultType.Identify, bitmap);
                        listener.onUser(resultType.Identify, user);
                    }
                });

            }
        }

        //displayTip("特征抽取对比耗时:" + (System.currentTimeMillis() - starttime), featureDurationTv) ;
    }

//    private String userIdOfMaxScore = "";
//    private float maxScore = 0;
//
//    private void displayUserOfMaxScore(final String userId, final float score) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (score < 80) {
//                    return;
//                }
////                if (userIdOfMaxScore.equals(userId)) {
////                    if (score < maxScore) {
////                    } else {
////                        maxScore = score;
////                    }
////                } else {
////                    userIdOfMaxScore = userId;
////                    maxScore = score;
////                }
//                final User user = FaceApi.getInstance().getUserInfo("1", userId);
//                if (user == null) {
//                    return;
//                } else {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            listener.onText(resultType.Identify, user.getUserInfo());
//                        }
//                    });
//                }
////                List<Feature> featureList = user.getFeatureList();
////                if (featureList != null && featureList.size() > 0) {
////                    // featureTv.setText(new String(featureList.get(0).getFeature()));
////                    File faceDir = FileUitls.getFaceDirectory();
////                    if (faceDir != null && faceDir.exists()) {
////                        File file = new File(faceDir, featureList.get(0).getImageName());
////                        if (file != null && file.exists()) {
////                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
////                            listener.onBitmap(resultType.Identify, bitmap);
////                        }
////                    }
////                }
////                List<Feature>  featureList = DBManager.getInstance().queryFeatureByUeserId(userId);
////                if (featureList != null && featureList.size() > 0) {
////                    File faceDir = FileUitls.getFaceDirectory();
////                    if (faceDir != null && faceDir.exists()) {
////                        File file = new File(faceDir, featureList.get(0).getImageName());
////                        if (file != null && file.exists()) {
////                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
////                            testView.setImageBitmap(bitmap);
////                        }
////                    }
////                }
//            }
//        });
//    }
}