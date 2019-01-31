package com.shinstrument.State.OperationState;


import com.shinstrument.AppInit;
import com.shinstrument.Config.SHGJ_Config;

/**
 * Created by zbsz on 2017/9/26.
 */

public class TwoUnlockState extends OperationState {


    @Override
    public void onHandle(Operation op) {
        if(AppInit.getInstrumentConfig().getClass().getName().equals(SHGJ_Config.class.getName())){
            op.setState(new OneLockState());
        }else{
            op.setState(new LockingState());
        }

    }

}
