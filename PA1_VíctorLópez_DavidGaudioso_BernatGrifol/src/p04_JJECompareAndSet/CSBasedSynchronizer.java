package p04_JJECompareAndSet;

import p03_JJECommon.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.atomic.AtomicInteger;

public class CSBasedSynchronizer implements Synchronizer {
    private static final int JUMP = 1;
    private static final int JIVE = 2;
    private static final int JOY = 3;
    private static final int ENJOY = 4;

    private final AtomicInteger state = new AtomicInteger(JUMP);
    private volatile int jumpCount = 0;
    private volatile int firstJumpId = -1;
    private volatile int secondJumpId = -1;
    private volatile int requiredJives = 0;
    private volatile int jivesCompleted = 0;

    @Override
    public void letMeJump(int id) {
        while (state.get() != JUMP) Thread.yield();
    }

    @Override
    public void jumpDone(int id) {
        if (jumpCount == 0) {
            firstJumpId = id;
            jumpCount = 1;
        } else {
            if (id == firstJumpId) {
                System.err.println("ERROR: JUMP ID repetition");
                System.exit(1);
            }
            secondJumpId = id;
            requiredJives = secondJumpId;
            jivesCompleted = 0;
            state.compareAndSet(JUMP, JIVE);
            jumpCount = 0;
        }
    }

    @Override
    public void letMeJive(int id) {
        while (state.get() != JIVE) Thread.yield();
    }

    @Override
    public void jiveDone(int id) {
        jivesCompleted++;
        if (jivesCompleted == requiredJives) {
            boolean even = (requiredJives % 2) == 0;
            state.compareAndSet(JIVE, even ? JOY : ENJOY);
        }
    }

    @Override
    public boolean letMeEnjoy(int id) {
        int currentState;
        do {
            currentState = state.get();
        } while (currentState != JOY && currentState != ENJOY);
        return currentState == ENJOY;
    }

    @Override
    public void enjoyDone(int id) {
        state.compareAndSet(state.get(), JUMP);
    }
}