package p01_ALExplicitLockBased;

import p00_ALCommon.LotteryMat;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExplicitLockBasedMat extends LotteryMat {

    private final Lock lock = new ReentrantLock();
    private String[] chips = new String[NUM_SQUARES];
    private int chipsCount = 0;

    public ExplicitLockBasedMat(int nd) {
        super(nd);
    }

    @Override
    public boolean tryBetting(String raceName, int memberId) {
        lock.lock();
        try {
            // Verificar si la raza ya participó en la mano actual o si es la raza ganadora de la mano anterior
            if (participatesInCurrentHand(raceName) || raceName.equals(lastWinnerRace)) {
                return false;
            }
            // Verificar si hay espacio en el mat
            if (chipsCount >= NUM_SQUARES) {
                return false;
            }
            // Colocar el chip en el primer espacio vacío
            for (int i = 0; i < NUM_SQUARES; i++) {
                if (chips[i] == null) {
                    chips[i] = raceName + "(" + memberId + ")";
                    chipsCount++;
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endBetting() {
        lock.lock();
        try {
            // No se necesita hacer nada aquí, el lock se libera en tryBetting
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startDrawing(int drawerId) {
        lock.lock();
        try {
            // Esperar hasta que todos los cuadros estén llenos
            while (chipsCount < NUM_SQUARES) {
                Thread.yield(); // Espera activa
            }
        } finally {
            // El lock se mantiene hasta que se complete el dibujo
        }
    }

    @Override
    public void endDrawing() {
        try {
            // Determinar el ganador y reiniciar el mat
            String[] currentChips = getChips();
            int winnerIndex = (int) (Math.random() * NUM_SQUARES);
            String winner = currentChips[winnerIndex];
            lastWinnerRace = winner.split("\\(")[0]; // Extraer la raza del ganador
            restart();
        } finally {
            lock.unlock();
        }
    }
}