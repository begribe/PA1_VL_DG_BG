package p04_JJECompareAndSet;

import p03_JJECommon.*;
import java.util.concurrent.atomic.*;

public class CSBasedSynchronizer implements Synchronizer {

    private static final int CAN_JUMP = 1;
    private static final int CAN_JIVE = 2;
    private static final int CAN_ENJOY = 3;
    private static final int WRITING = 4;

    private AtomicInteger state;
    private volatile int lastTicId = -1;
    private volatile int jumpCount = 0;
    private volatile int jiveCount = 0;

    public CSBasedSynchronizer() {
        this.state = new AtomicInteger(CAN_JUMP);
    }

    @Override
    public void letMeJump(int id) {
        while (true) {
            if (state.compareAndSet(CAN_JUMP, WRITING)) {
                if (jumpCount == 1 && id == lastTicId) {
                    state.set(CAN_JUMP);
                    continue;
                }
                lastTicId = id;
                jumpCount++;
                break;
            }
            Thread.yield();
        }
    }

    @Override
    public void jumpDone(int id) {
        if (jumpCount < 2) {
            state.set(CAN_JUMP);
        } else {
            if (lastTicId == 0) {
                state.set(CAN_ENJOY);
            } else {
                state.set(CAN_JIVE);
            }
            jumpCount = 0;
        }
    }

    @Override
    public void letMeJive(int id) {
        while (!state.compareAndSet(CAN_JIVE, WRITING)) {
            Thread.yield();
        }
        jiveCount++;
    }

    @Override
    public void jiveDone(int id) {
        if (jiveCount < lastTicId) {
            state.set(CAN_JIVE);
        } else {
            state.set(CAN_ENJOY);
        }
    }

    @Override
    public boolean letMeEnjoy(int id) {
        while (!state.compareAndSet(CAN_ENJOY, WRITING)) {
            Thread.yield();
        }
        return lastTicId % 2 != 0;
    }

    @Override
    public void enjoyDone(int id) {
        state.set(CAN_JUMP);
        jiveCount = 0;
    }

}