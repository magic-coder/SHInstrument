package com.shinstrument.State.OperationState;



/**
 * Created by zbsz on 2017/9/26.
 */

public class OneUnlockState extends OperationState {



    @Override
    public void onHandle(Operation op) {
        op.setState(new TwoUnlockState());
    }

}
