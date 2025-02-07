package p02_ALImplicitLockBased;

import p00_ALCommon.LotteryMat;

public class ImplicitLockBasedMat extends LotteryMat{
	
	// only simple typed variable(s) here
	private String[] mat = new String[NUM_SQUARES]; // Representa los 4 cuadrados del tablero
	private String lastWinningRace = null; // Última raza ganadora
	
	public ImplicitLockBasedMat (int numDrawers) {
		super(numDrawers);
	}
	
	/* COMPLETE (implement inherited abstract methods) 
	 Remeber that synchronized methods are not allowed
	 */

	@Override
	public boolean tryBetting(String raceName, int memberId) {
		synchronized (this) {
			// Verifica si ya participa alguien de la misma raza
			if (participatesInCurrentHand(raceName)) {
				return false;
			}

			// Encuentra un cuadrado vacío y coloca la ficha
			for (int i = 0; i < mat.length; i++) {
				if (mat[i] == null || mat[i].equals("FREE")) {
					mat[i] = raceName + "(" + memberId + ")";
					putChip(raceName, memberId); // Método de la clase base
					return true;
				}
			}

			return false; // Si el tablero está lleno
		}
	}

	@Override
	public void endBetting() {
		// No se necesita acción adicional porque el lock implícito se libera automáticamente
	}

	@Override
	public void startDrawing(int drawerId) {
		synchronized (this) {
			// Bloquea hasta que sea el turno del sorteador y el tablero esté lleno
			while (drawerId != currentDrawerId || emptySquares > 0) {
				try {
					this.wait(); // Libera el lock y espera
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Restaurar estado de interrupción
				}
			}
		}
	}

	@Override
	public void endDrawing() {
		synchronized (this) {
			// Selecciona un ganador aleatorio y reinicia el tablero
			String[] chips = getChips();
			String winningChip = chips[(int) (Math.random() * NUM_SQUARES)];
			System.out.println("Winner: " + winningChip);
			lastWinningRace = winningChip.split("\\(")[0];
			restart(); // Método de la clase base
			this.notifyAll(); // Notifica a todos los hilos que pueden continuar
		}
	}
}