package com.shinstrument;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.TexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.shinstrument.Alerts.Alert_IP;
import com.shinstrument.Alerts.Alert_Message;
import com.shinstrument.Alerts.Alert_Server;
import com.shinstrument.Bean.DataFlow.PersonBean;
import com.shinstrument.Bean.DataFlow.ReUploadBean;
import com.shinstrument.Bean.DataFlow.UpCheckRecordData;
import com.shinstrument.Bean.DataFlow.UpOpenDoorData;
import com.shinstrument.Config.BaseConfig;
import com.shinstrument.Config.SHDMJ_config;
import com.shinstrument.EventBus.AlarmEvent;
import com.shinstrument.EventBus.CloseDoorEvent;
import com.shinstrument.EventBus.ExitEvent;
import com.shinstrument.EventBus.NetworkEvent;
import com.shinstrument.EventBus.PassEvent;
import com.shinstrument.EventBus.TemHumEvent;
import com.shinstrument.Function.Func_Face.mvp.Module.FaceImpl;
import com.shinstrument.Function.Func_Face.mvp.presenter.FacePresenter;
import com.shinstrument.Function.Func_Face.mvp.view.IFaceView;
import com.shinstrument.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.shinstrument.Function.Func_IDCard.mvp.view.IIDCardView;
import com.shinstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.shinstrument.Receiver.TimeCheckReceiver;
import com.shinstrument.Service.SwitchService;
import com.shinstrument.State.LockState.Lock;
import com.shinstrument.State.LockState.State_Unlock;
import com.shinstrument.State.OperationState.LockingState;
import com.shinstrument.State.OperationState.OneUnlockState;
import com.shinstrument.State.OperationState.Operation;
import com.shinstrument.State.OperationState.TwoUnlockState;
import com.shinstrument.Tools.MediaHelper;
import com.shinstrument.Tools.ServerConnectionUtil;
import com.shinstrument.greendao.DaoSession;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cbdi.drv.card.ICardInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CBSD_FaceBySHActivity extends RxActivity implements IIDCardView, IFaceView, AddPersonWindow.OptionTypeListener {

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.img_captured1)
    ImageView captured1;

    @BindView(R.id.tv_info)
    TextView tips;

    @BindView(R.id.iv_network)
    ImageView iv_network;

    @BindView(R.id.iv_lock)
    ImageView iv_lock;

    @BindView(R.id.tv_temp)
    TextView tv_temperature;

    @BindView(R.id.tv_humid)
    TextView tv_humidity;

    @BindView(R.id.iv_humid)
    ImageView iv_humidity;

    @BindView(R.id.iv_temp)
    ImageView iv_temperature;

    @BindView(R.id.preview_view)
    TexturePreviewView previewView;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    BaseConfig ins_type = AppInit.getInstrumentConfig();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Disposable disposableTips;

    Intent intent;

    GestureLibrary mGestureLib;

    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);

    AddPersonWindow personWindow;

    Operation operation;

    SPUtils config = SPUtils.getInstance("config");

    String persontype;

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();


    SPUtils personData = SPUtils.getInstance("personData");

    SPUtils firstPer = SPUtils.getInstance("firstPer");

    PersonBean person1 = new PersonBean();

    PersonBean person2 = new PersonBean();

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    Disposable checkChange;

    Bitmap headphoto;

    Bitmap photo;


    @OnClick(R.id.iv_network)
    void show() {
        personWindow = new AddPersonWindow(this);
        personWindow.setOptionTypeListener(this);
        personWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    @OnClick(R.id.iv_lock)
    public void showMessage() {
        if (AppInit.getInstrumentConfig().getClass().getName().equals(SHDMJ_config.class.getName())
                && Lock.getInstance().getLockState().getClass().getName().equals(State_Unlock.class.getName())) {
            SwitchPresenter.getInstance().doorOpen();
        } else {
            alert_message.showMessage();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_main1);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        openService();
        hardwarePrepare();
        prepare();
        setGesture();
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {

    }

    @Override
    public void onSetText(String Msg) {
        if (alert_message.Showing()) {
            ToastUtils.showLong(Msg);
        }
    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
//        if (alert_message.Showing()) {
//            alert_message.setICCardText(cardInfo.cardId());
//        } else {
//            this.cardInfo = cardInfo;
//            tips.setText(cardInfo.name() + "刷卡中，请稍后");
//            if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("1")) {
//
//            } else if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("2")) {
//
//            } else {
//                connectionUtil.post(config.getString("ServerId") + ins_type.getPersonInfoPrefix() + "dataType=queryPersion" + "&daid=" + config.getString("devid") + "&id=" + cardInfo.cardId(), config.getString("ServerId"), new ServerConnectionUtil.Callback() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (response != null) {
//                            if (response.startsWith("true")) {
//                                if (response.split("\\|").length > 1) {
//                                    persontype = response.split("\\|")[1];
//                                    SPUtils.getInstance("personData").put(cardInfo.cardId(), persontype);
//                                    if (persontype.equals("1")) {
//                                        if (getState(LockingState.class)) {
//                                            person1.setCardId(cardInfo.cardId());
//                                            person1.setName(cardInfo.name());
//                                        } else if (getState(OneUnlockState.class)) {
//                                            person2.setCardId(cardInfo.cardId());
//                                            person2.setName(cardInfo.name());
//                                        }
//                                    }
//                                    if (!ins_type.isGetOneShot()) {
//                                        pp.capture();
//                                    } else {
//                                        pp.getOneShut();
//                                    }
//                                    idp.stopReadCard();
//                                }
//                            } else {
//                                persontype = "0";
//                                if (!ins_type.isGetOneShot()) {
//                                    pp.capture();
//                                } else {
//                                    pp.getOneShut();
//                                }
//                                idp.stopReadCard();
//                            }
//                        } else {
//                            tips.setText("人员身份查询：服务器上传出错");
////                            MediaHelper.play(MediaHelper.Text.err_connect);
//                            persontype = "0";
//                            if (!ins_type.isGetOneShot()) {
//                                pp.capture();
//                            } else {
//                                pp.getOneShut();
//                            }
//                            idp.stopReadCard();
//                        }
//                    }
//                });
//            }
//        }
    }

    @Override
    public void onSetAllMsg(final ICardInfo cardInfo, final Bitmap bitmap) {
        headphoto = bitmap;
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.cardId());
        } else {
            tips.setText(cardInfo.name() + "刷卡中，请稍后");
            if ((persontype = personData.getString(cardInfo.cardId())).equals("1")) {
                if (firstPer.getBoolean(cardInfo.cardId(), true)) {
                    fp.FaceReg(cardInfo, bitmap);
                }
            } else if ((persontype = personData.getString(cardInfo.cardId())).equals("2")) {

            } else {
                connectionUtil.post(config.getString("ServerId") + ins_type.getPersonInfoPrefix() + "dataType=queryPersion" + "&daid=" + config.getString("devid") + "&id=" + cardInfo.cardId(), config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                if (response.split("\\|").length > 1) {
                                    persontype = response.split("\\|")[1];
                                    personData.put(cardInfo.cardId(), persontype);
                                    if (firstPer.getBoolean(cardInfo.cardId(), true)) {
                                        fp.FaceReg(cardInfo, bitmap);
                                    }
                                }
                            } else {

                            }
                        } else {

                        }
                    }
                });
            }
        }
    }

    @Override
    public void onBitmap(FaceImpl.resultType resultType, Bitmap bitmap) {
        if (resultType == FaceImpl.resultType.Identify) {
            captured1.setImageBitmap(bitmap);
            photo = bitmap;
        }
    }

    @Override
    public void onText(FaceImpl.resultType resultType, String text) {
        if (resultType == FaceImpl.resultType.Reg) {
            if (text.equals("success")) {
                tips.setText("人员人脸参数已被记录");
                fp.FaceIdentifyReady();
            } else {
                tips.setText("人员人脸参数记录失败");
            }
            fp.FaceIdentify();
        } else if (resultType == FaceImpl.resultType.IMG_MATCH_IMG_Error) {
            fp.FaceIdentify();
        }
    }

    @Override
    public void onUser(FaceImpl.resultType resultType, User user) {
        this.the_user = user;
        if (resultType == FaceImpl.resultType.Identify) {
            if (getState(LockingState.class)) {
                person1.setCardId(user.getUserId());
                person1.setName(user.getUserInfo());
                AfterID(user);
            } else if (getState(OneUnlockState.class)) {
                person2.setCardId(user.getUserId());
                person2.setName(user.getUserInfo());
                if (person1.getCardId().equals(person2.getCardId())) {
                    tips.setText("请不要连续输入同一个管理员的信息");
                    MediaHelper.play(MediaHelper.Text.err_samePerson);
                    return;
                } else {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    AfterID(user);
                }
            } else if (getState(TwoUnlockState.class)) {
                EventBus.getDefault().post(new CloseDoorEvent());
                iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lockup));
                tips.setText("已进入设防状态");
                // MediaHelper.play(MediaHelper.Text.relock_opt);
            }
        } else if (resultType == FaceImpl.resultType.Reg) {
            firstPer.put(user.getUserId(), false);
        }
    }

    User the_user;

    void AfterID(User user) {
        operation.doNext();
        if (getState(OneUnlockState.class)) {
            person1.setPhoto(photo);
            tips.setText("仓管员" + user.getUserInfo() + "刷卡成功");
            MediaHelper.play(MediaHelper.Text.first_opt);
            Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .compose(CBSD_FaceBySHActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            checkChange = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            checkRecord(2);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if ((getState(TwoUnlockState.class))) {
            person2.setPhoto(photo);
            captured1.setImageBitmap(null);
            EventBus.getDefault().post(new PassEvent());
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
            tips.setText("仓管员" + user.getUserInfo() + "刷卡成功");
            openDoorUpData();
        }
    }

    void openDoorUpData() {
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=openDoor" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(),
                new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            tips.setText("开门记录已上传到服务器");
                        } else {
                            tips.setText("开门记录上传失败，已保存到本地");
                            MediaHelper.play(MediaHelper.Text.second_err);
                            mdaoSession.insert(new ReUploadBean(null, "dataType=openDoor", new UpOpenDoorData().toOpenDoorData((byte) 0x01, person1.getCardId(), person1.getName(), person1.getPhoto(), person2.getCardId(), person2.getName(), person2.getPhoto()).toByteArray(), 0));
                        }
                    }
                });
    }

    void checkRecord(final int type) {
        SwitchPresenter.getInstance().OutD9(false);
        Intent checked = new Intent(this, TimeCheckReceiver.class);
        checked.setAction("checked");
        sendBroadcast(checked);


        if (checkChange != null && !checkChange.isDisposed()) {
            operation.setState(new LockingState());
            captured1.setImageBitmap(null);
            tips.setText("巡检数据：巡检成功");
//            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
//                    config.getString("ServerId"),
//                    new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(),
//                    new ServerConnectionUtil.Callback() {
//                        @Override
//                        public void onResponse(String response) {
//                            try{
//                                if (!getState(TwoUnlockState.class)) {
//                                    operation.setState(new LockingState());
//                                }
//                                captured1.setImageBitmap(null);
//                                if (response != null) {
//                                    if (response.startsWith("true")) {
//                                        tips.setText("巡检数据：巡检成功");
//                                        MediaHelper.play(MediaHelper.Text.msg_patrol);
//                                    } else {
//                                        tips.setText("巡检数据：上传失败");
//                                        MediaHelper.play(MediaHelper.Text.err_upload);
//                                    }
//                                } else {
//                                    tips.setText("巡检数据：无法连接到服务器");
//                                    MediaHelper.play(MediaHelper.Text.err_connect);
//                                    mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(), type));
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//
//                        }
//                    });
        } else {
            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
                    config.getString("ServerId"),
                    new UpCheckRecordData().toCheckRecordData(the_user.getUserId(), photo, the_user.getUserInfo()).toByteArray(),
                    new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (!getState(TwoUnlockState.class)) {
                                    operation.setState(new LockingState());
                                }
                                captured1.setImageBitmap(null);
                                if (response != null) {
                                    if (response.startsWith("true")) {
                                        tips.setText("巡检数据：巡检成功");
                                        MediaHelper.play(MediaHelper.Text.msg_patrol);
                                    } else {
                                        tips.setText("巡检数据：上传失败");
                                        MediaHelper.play(MediaHelper.Text.err_upload);
                                    }
                                } else {
                                    tips.setText("巡检数据：无法连接到服务器");
                                    MediaHelper.play(MediaHelper.Text.err_connect);
                                    mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(the_user.getUserId(), photo, the_user.getUserInfo()).toByteArray(), type));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }
    }

    @Override
    public void onOptionType(Button view, int type) {
        personWindow.dismiss();
        if (type == 1) {
            alert_server.show();
        } else if (type == 2) {
            alert_ip.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.non_wifi));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        tips.setText("开门报警已被触发");
        MediaHelper.play(MediaHelper.Text.alarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        fp.FacePresenterSetView(this);
        operation.setState(new LockingState());
        tips.setText(config.getString("devid") + "号机器等待用户操作");
        fp.FaceIdentifyReady();
        fp.FaceIdentify();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            fp.FaceOnActivityStart();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
        fp.FacePresenterSetView(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        fp.FaceOnActivityStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fp.FaceOnActivityDestroy();
        idp.idCardClose();
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        EventBus.getDefault().post(new ExitEvent());
        disposableTips.dispose();
        MediaHelper.mediaRealese();
        EventBus.getDefault().unregister(this);
    }

    void openService() {
        intent = new Intent(this, SwitchService.class);
        startService(intent);
    }

    private void setGesture() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay,
                                           Gesture gesture) {
                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    // 匹配的手势
                    if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                        if (prediction.name.equals("setting")) {
                            NetworkUtils.openWirelessSettings();
                        }
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    private void hardwarePrepare() {
        MediaHelper.mediaOpen();
        idp.idCardOpen();
        fp.CameraInit(this.getApplicationContext(), previewView, textureView);
        AppInit.getMyManager().ethEnabled(true);

    }

    private void prepare() {
        alert_message.messageInit();
        alert_ip.IpviewInit();
        alert_server.serverInit(new Alert_Server.Server_Callback() {
            @Override
            public void setNetworkBmp() {
                iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
            }
        });
        operation = new Operation(new LockingState());
        if (ins_type.isTemHum()) {
            iv_temperature.setVisibility(View.VISIBLE);
            iv_humidity.setVisibility(View.VISIBLE);
        } else {
            iv_temperature.setVisibility(View.INVISIBLE);
            iv_humidity.setVisibility(View.INVISIBLE);
        }
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        tv_time.setText(formatter.format(new Date(System.currentTimeMillis())));
                    }
                });

        disposableTips = RxTextView.textChanges(tips)
                .debounce(20, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws
                            Exception {
                        return Observable.just(config.getString("devid") + "号机器等待用户操作");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tips.setText(s);
                    }
                });
    }

    private Boolean getState(Class stateClass) {
        if (operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }
}
