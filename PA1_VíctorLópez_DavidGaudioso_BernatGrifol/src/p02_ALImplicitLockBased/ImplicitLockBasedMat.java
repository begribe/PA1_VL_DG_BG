package p02_ALImplicitLockBased;

import p00_ALCommon.LotteryMat;

public class ImplicitLockBasedMat extends LotteryMat {

    private final Object lock = new Object();
    private String[] chips = new String[NUM_SQUARES];
    private int chipsCount = 0;

    public ImplicitLockBasedMat(int nd) {
        super(nd);
    }

    @Override
    public boolean tryBetting(String raceName, int memberId) {
        synchronized (lock) {
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
        }
    }

    @Override
    public void endBetting() {
        synchronized (lock) {
            // No se necesita hacer nada aquí, el lock se libera en tryBetting
        }
    }

    @Override
    public void startDrawing(int drawerId) {
        synchronized (lock) {
            // Esperar hasta que todos los cuadros estén llenos
            while (chipsCount < NUM_SQUARES) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void endDrawing() {
        synchronized (lock) {
            // Determinar el ganador y reiniciar el mat
            String[] currentChips = getChips();
            int winnerIndex = (int) (Math.random() * NUM_SQUARES);
            String winner = currentChips[winnerIndex];
            lastWinnerRace = winner.split("\\(")[0]; // Extraer la raza del ganador
            restart();
            lock.notifyAll(); // Notificar a los hilos que esperan en startDrawing
        }
    }
}