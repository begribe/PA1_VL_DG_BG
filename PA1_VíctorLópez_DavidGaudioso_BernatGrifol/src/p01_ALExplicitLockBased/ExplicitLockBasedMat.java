package p01_ALExplicitLockBased;

import p00_ALCommon.LotteryMat;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExplicitLockBasedMat extends LotteryMat{
	
	// Declare here your explicit lock. And nothing more

	private final Lock lock = new ReentrantLock();
	private String[] mat = new String[4]; // Representa los 4 cuadrados del tablero
	private int currentDrawer = 0; // Sorteador actual
	private String lastWinningRace = null; // Última raza ganadora
	
	public ExplicitLockBasedMat (int numDrawers) {
		super(numDrawers);
	}
	
	/* COMPLETE (implement inherited abstract methods) */


	@Override
	public boolean tryBetting(String raceName, int memberId) {
		lock.lock();
		try {
			// Verificar si ya participa alguien de la misma raza
			if (participatesInCurrentHand(raceName)) {
				return false;
			}
			// Verificar si hay espacio en el tablero
			for (int i = 0; i < mat.length; i++) {
				if (mat[i] == null) {
					// Colocar la ficha en el tablero
					mat[i] = raceName + "(" + memberId + ")";
					putChip(raceName, memberId);
					return true;
				}
			}
			return false; // Tablero lleno
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void endBetting() {
		// No se requiere lógica adicional porque el concursante ya liberó su acceso
	}

	@Override
	public void startDrawing(int drawerId) {
		lock.lock();
		try {
			// Verificar si el sorteador tiene el turno
			while (drawerId != currentDrawer || !isMatFull()) {
				lock.unlock();
				Thread.yield(); // Esperar turno
				lock.lock();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void endDrawing() {
		lock.lock();
		try {
			// Reiniciar el tablero y actualizar el turno del siguiente sorteador
			String[] chips = getChips();
			String winningChip = chips[(int) (Math.random() * 4)]; // Seleccionar ganador
			System.out.println("Winner: " + winningChip);
			lastWinningRace = winningChip.split("\\(")[0];
			restart(); // Vaciar tablero
			currentDrawer = (currentDrawer + 1) % 4; // Orden cíclico
		} finally {
			lock.unlock();
		}
	}

	private boolean isMatFull() {
		for (String square : mat) {
			if (square == null) {
				return false;
			}
		}
		return true;
	}
}