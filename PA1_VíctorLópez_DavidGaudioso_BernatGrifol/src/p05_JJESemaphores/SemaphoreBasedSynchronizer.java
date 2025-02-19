package p05_JJESemaphores;

import p03_JJECommon.*;
import java.util.concurrent.*;

public class SemaphoreBasedSynchronizer implements Synchronizer {
    private final Semaphore jumpSemaphore = new Semaphore(1);
    private final Semaphore jiveSemaphore = new Semaphore(0);
    private final Semaphore enjoySemaphore = new Semaphore(0);
    private volatile int jumpCount = 0;
    private volatile int lastTicId = -1;
    private volatile int jiveCount = 0;

    @Override
    public void letMeJump(int id) {
        try {
            jumpSemaphore.acquire();
            if (jumpCount == 1 && id == lastTicId) {
                jumpSemaphore.release();
                return;
            }
            lastTicId = id;
            jumpCount++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void jumpDone(int id) {
        if (jumpCount < 2) {
            jumpSemaphore.release();
        } else {
            if (lastTicId == 0) {
                enjoySemaphore.release();
            } else {
                jiveSemaphore.release();
            }
            jumpCount = 0;
        }
    }

    @Override
    public void letMeJive(int id) {
        try {
            jiveSemaphore.acquire();
            jiveCount++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void jiveDone(int id) {
        if (jiveCount < lastTicId) {
            jiveSemaphore.release();
        } else {
            enjoySemaphore.release();
        }
    }

    @Override
    public boolean letMeEnjoy(int id) {
        try {
            enjoySemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return lastTicId % 2 != 0;
    }

    @Override
    public void enjoyDone(int id) {
        jumpSemaphore.release();
        jiveCount = 0;
    }
}