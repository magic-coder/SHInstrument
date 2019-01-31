package com.shinstrument.State.OperationState;

public class OneLockState extends OperationState {
    @Override
    public void onHandle(Operation op) {
        op.setState(new LockingState());
    }
}
