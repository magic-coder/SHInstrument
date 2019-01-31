package com.shinstrument;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.shinstrument.Bean.DataFlow.ReUploadBean;
import com.shinstrument.Bean.DataFlow.UpCheckRecordData;
import com.shinstrument.EventBus.ADEvent;
import com.shinstrument.EventBus.CloseDoorEvent;
import com.shinstrument.EventBus.ExitEvent;
import com.shinstrument.EventBus.PassEvent;
import com.shinstrument.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.shinstrument.Receiver.TimeCheckReceiver;
import com.shinstrument.Service.SwitchService;
import com.shinstrument.State.OperationState.LockingState;
import com.shinstrument.State.OperationState.OneUnlockState;
import com.shinstrument.State.OperationState.TwoUnlockState;
import com.shinstrument.Tools.MediaHelper;
import com.shinstrument.Tools.ServerConnectionUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import butterknife.BindView;
import butterknife.ButterKnife;
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

public class CBSD_HuBeiWeiHuaActivity extends CBSD_FunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Disposable disposableTips;

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    Intent intent;
    GestureLibrary mGestureLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_byhubei);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (ins_type.isTemHum()) {
            iv_temperature.setVisibility(View.VISIBLE);
            iv_humidity.setVisibility(View.VISIBLE);
        } else {
            iv_temperature.setVisibility(View.INVISIBLE);
            iv_humidity.setVisibility(View.INVISIBLE);
        }
        openService();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

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
        setGesture();
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

    @Override
    public void onOptionType(Button view, int type) {
        personWindow.dismiss();
        if (type == 1) {
            alert_server.show();
        } else if (type == 2) {
            alert_ip.show();
        }
    }

    void openService() {
        intent = new Intent(this, SwitchService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        tips.setText(config.getString("devid") + "号机器等待用户操作");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new ExitEvent());
        //stopService(intent);
        disposableTips.dispose();
        EventBus.getDefault().unregister(this);
    }

    @BindView(R.id.tv_ad01)
    TextView tv_ad01;
    @BindView(R.id.tv_ad02)
    TextView tv_ad02;
    @BindView(R.id.tv_ad03)
    TextView tv_ad03;
    @BindView(R.id.tv_ad04)
    TextView tv_ad04;
    @BindView(R.id.tv_ad05)
    TextView tv_ad05;
    @BindView(R.id.tv_ad06)
    TextView tv_ad06;
    @BindView(R.id.tv_ad07)
    TextView tv_ad07;
    @BindView(R.id.tv_ad08)
    TextView tv_ad08;
    @BindView(R.id.iv_warning)
    ImageView iv_warning;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAIEvent(ADEvent event) {
        if (ins_type.collectBox()) {
            if (event.getData() != null) {
                switch (Integer.parseInt(event.getMessage())) {
                    case 8:
                        tv_ad08.setText(event.getData().getAI(7).getName() + ":" + event.getData().getAI(7).getVal() + event.getData().getAI(7).getUnit());
                    case 7:
                        tv_ad07.setText(event.getData().getAI(6).getName() + ":" + event.getData().getAI(6).getVal() + event.getData().getAI(6).getUnit());
                    case 6:
                        tv_ad06.setText(event.getData().getAI(5).getName() + ":" + event.getData().getAI(5).getVal() + event.getData().getAI(5).getUnit());
                    case 5:
                        tv_ad05.setText(event.getData().getAI(4).getName() + ":" + event.getData().getAI(4).getVal() + event.getData().getAI(4).getUnit());
                    case 4:
                        tv_ad04.setText(event.getData().getAI(3).getName() + ":" + event.getData().getAI(3).getVal() + event.getData().getAI(3).getUnit());
                    case 3:
                        tv_ad03.setText(event.getData().getAI(2).getName() + ":" + event.getData().getAI(2).getVal() + event.getData().getAI(2).getUnit());
                    case 2:
                        tv_ad02.setText(event.getData().getAI(1).getName() + ":" + event.getData().getAI(1).getVal() + event.getData().getAI(1).getUnit());
                    case 1:
                        tv_ad01.setText(event.getData().getAI(0).getName() + ":" + event.getData().getAI(0).getVal() + event.getData().getAI(0).getUnit());
                    default:
                        iv_warning.setVisibility(View.GONE);
                        break;
                }
            } else {
                iv_warning.setVisibility(View.VISIBLE);
                tv_ad01.setText(event.getMessage());
                tv_ad02.setText(null);
                tv_ad03.setText(null);
                tv_ad04.setText(null);
                tv_ad05.setText(null);
                tv_ad06.setText(null);
                tv_ad07.setText(null);
                tv_ad08.setText(null);
            }
        }
    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }



//    @Override
//    public void onsetCardInfo(final CardInfoRk123x cardInfo) {

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        if (alert_message.Showing()) {
           alert_message.setICCardText(cardInfo.cardId());
        } else {
            this.cardInfo = cardInfo;
            tips.setText(cardInfo.name() + "刷卡中，请稍后");
            if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("1")) {
                if (getState(LockingState.class)) {
                    person1.setCardId(cardInfo.cardId());
                    person1.setName(cardInfo.name());
                } else if (getState(OneUnlockState.class)) {
                    person2.setCardId(cardInfo.cardId());
                    person2.setName(cardInfo.name());
                    if (person1.getCardId().equals(person2.getCardId())) {
                        tips.setText("请不要连续输入同一个管理员的信息");
                        MediaHelper.play(MediaHelper.Text.err_samePerson);
                        return;
                    } else {
                        if(!ins_type.isFace()){
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                        }

                    }
                } else if (getState(TwoUnlockState.class)) {
                    EventBus.getDefault().post(new CloseDoorEvent());
                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lockup));
                    tips.setText("已进入设防状态");
                    // MediaHelper.play(MediaHelper.Text.relock_opt);
                }
                if (!ins_type.isGetOneShot()) {
                    pp.capture();
                } else {
                    pp.getOneShut();
                }
                idp.stopReadCard();
            } else if ((persontype = SPUtils.getInstance("personData").getString(cardInfo.cardId())).equals("2")) {
                if (!ins_type.isGetOneShot()) {
                    pp.capture();
                } else {
                    pp.getOneShut();
                }
                idp.stopReadCard();
            } else {
                connectionUtil.post(config.getString("ServerId") + ins_type.getPersonInfoPrefix() + "dataType=queryPersion" + "&daid=" + config.getString("devid") + "&id=" + cardInfo.cardId(), config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                if (response.split("\\|").length > 1) {
                                    persontype = response.split("\\|")[1];
                                    SPUtils.getInstance("personData").put(cardInfo.cardId(), persontype);
                                    if (persontype.equals("1")) {
                                        if (getState(LockingState.class)) {
                                            person1.setCardId(cardInfo.cardId());
                                            person1.setName(cardInfo.name());
                                        } else if (getState(OneUnlockState.class)) {
                                            person2.setCardId(cardInfo.cardId());
                                            person2.setName(cardInfo.name());
                                        }
                                    }
                                    if (!ins_type.isGetOneShot()) {
                                        pp.capture();
                                    } else {
                                        pp.getOneShut();
                                    }
                                    idp.stopReadCard();
                                }
                            } else {
                                persontype = "0";
                                if (!ins_type.isGetOneShot()) {
                                    pp.capture();
                                } else {
                                    pp.getOneShut();
                                }
                                idp.stopReadCard();
                            }
                        } else {
                            tips.setText("人员身份查询：服务器上传出错");
//                            MediaHelper.play(MediaHelper.Text.err_connect);
                            persontype = "0";
                            if (!ins_type.isGetOneShot()) {
                                pp.capture();
                            } else {
                                pp.getOneShut();
                            }
                            idp.stopReadCard();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onGetPhoto(Bitmap bmp) {
        photo = compressImage(bmp);
        if (persontype.equals("1")) {
            // if (!persontype.equals("5")) {
            if (getState(LockingState.class) || getState(OneUnlockState.class)) {
                if (ins_type.isFace()) {
                    face_upData();
                } else {
                    noface_upData();
                }
            } else if (getState(TwoUnlockState.class)) {
                operation.doNext();
                pp.setDisplay(surfaceView.getHolder());
                idp.readCard();
            }
        } else if (persontype.equals("2")) {
            if (checkChange != null) {
                checkChange.dispose();
            }
            checkRecord(2);
        } else if (persontype.equals("3")) {
            if (checkChange != null) {
                checkChange.dispose();
            }
            checkRecord(3);
        } else if (persontype.equals("0")) {
            unknownPersonData();
        }
    }

    void noface_upData() {
        operation.doNext();
        if (getState(OneUnlockState.class)) {
            person1.setPhoto(photo);
            captured1.setImageBitmap(photo);
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            MediaHelper.play(MediaHelper.Text.first_opt);
            pp.setDisplay(surfaceView.getHolder());
            idp.readCard();
            Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .compose(CBSD_HuBeiWeiHuaActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
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
            tips.setText("仓管员" + cardInfo.name() + "刷卡成功");
            MediaHelper.play(MediaHelper.Text.second_opt);
            noface_openDoorUpData();
        }
    }

    void face_upData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=faceRecognition" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true") && (int) Double.parseDouble(response.substring(5, response.length())) > 60) {
                                operation.doNext();
                                if (getState(OneUnlockState.class)) {
                                    person1.setPhoto(photo);
                                    person1.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(photo);
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person1.getFaceReconition());
                                    MediaHelper.play(MediaHelper.Text.first_opt);
                                    pp.setDisplay(surfaceView.getHolder());
                                    idp.readCard();
                                    Observable.timer(30, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                            .compose(CBSD_HuBeiWeiHuaActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
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
                                    person2.setFaceReconition((int) Double.parseDouble(response.substring(5, response.length())));
                                    captured1.setImageBitmap(null);
                                    EventBus.getDefault().post(new PassEvent());
                                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_unlock));
                                    tips.setText("仓管员" + cardInfo.name() + "刷卡成功,相似度为" + person2.getFaceReconition());
                                    MediaHelper.play(MediaHelper.Text.second_opt);
                                    face_openDoorUpData();
                                }
                            } else {
                                tips.setText("仓管员数据：人脸比对失败，请重试");
                                pp.setDisplay(surfaceView.getHolder());
                                idp.readCard();
                            }
                        } else {
                            tips.setText("仓管员数据：无法连接服务器");
                            MediaHelper.play(MediaHelper.Text.err_connect_ns);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
                        }
                    }
                });
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void checkRecord(final int type) {
        SwitchPresenter.getInstance().OutD9(false);
        Intent checked = new Intent(this, TimeCheckReceiver.class);
        checked.setAction("checked");
        sendBroadcast(checked);

        if (checkChange != null && !checkChange.isDisposed()) {
            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
                    config.getString("ServerId"),
                    new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(),
                    new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            if (!getState(TwoUnlockState.class)) {
                                operation.setState(new LockingState());
                            }
                            captured1.setImageBitmap(null);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
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
                                mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(person1.getCardId(),/*cardInfo.cardId(),*/ person1.getPhoto(), person1.getName()).toByteArray(), type));
                            }
                        }
                    });
        } else {
            connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=checkRecord" + "&daid=" + config.getString("devid") + "&checkType=" + type,
                    config.getString("ServerId"),
                    new UpCheckRecordData().toCheckRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(),
                    new ServerConnectionUtil.Callback() {
                        @Override
                        public void onResponse(String response) {
                            if (!getState(TwoUnlockState.class)) {
                                operation.setState(new LockingState());
                            }
                            captured1.setImageBitmap(null);
                            pp.setDisplay(surfaceView.getHolder());
                            idp.readCard();
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
                                mdaoSession.insert(new ReUploadBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), type));
                            }
                        }
                    });

        }
    }

    void unknownPersonData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + ins_type.getUpDataPrefix() + "dataType=persionRecord" + "&daid=" + config.getString("devid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), new ServerConnectionUtil.Callback() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                tips.setText("来访人员信息已上传");
                                MediaHelper.play(MediaHelper.Text.msg_visit);
                            } else {
                                tips.setText("设备尚未登记,请前往系统进行登记操作");
                                MediaHelper.play(MediaHelper.Text.no_registration);
                            }
                        } else {
                            tips.setText("来访人员信息：无法连接到服务器");
                            MediaHelper.play(MediaHelper.Text.err_connect);
                            mdaoSession.insert(new ReUploadBean(null, "dataType=persionRecord", upPersonRecordData.toPersonRecordData(cardInfo.cardId(), photo, cardInfo.name()).toByteArray(), 0));
                        }
                        pp.setDisplay(surfaceView.getHolder());
                        idp.readCard();
                    }
                });
    }
}

