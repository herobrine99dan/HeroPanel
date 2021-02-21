package herobrine99dan.heropanel.webserver.features;

import java.util.Arrays;

import org.bukkit.Bukkit;

import herobrine99dan.heropanel.UniportWebServer;

public class TPSHandler implements Runnable {
	private EstimatedTPSCalculator tpsCalculator;
	private int elapsedTicks = 0;
	private volatile float minecraftTPS, customTPS;

	public void startTask(UniportWebServer main) {
		this.elapsedTicks = 0;
		this.tpsCalculator = new EstimatedTPSCalculator();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this, 120L, 1);
	}

	public float getTPS() {
		return minecraftTPS;
	}

	public void run() {
		minecraftTPS = (float) this.tpsCalculator.calculateCurrentAverageTPS();
		float tps = (float) this.tpsCalculator.calculateCurrentAverageTPS();
		this.tpsCalculator.tick();
		if (tps > 0.0D && tps <= 21.0D) {
			customTPS = tps;
		}
		customTPS = (float) Utility.round(customTPS, 100);
		minecraftTPS = (float) Utility.round(minecraftTPS, 100);
	}

	private class EstimatedTPSCalculator {
		private long lasTimestamp = -1L;
		private int[] tickLengths = new int[20];
		private double tps = 20.0D;

		public EstimatedTPSCalculator() {
			Arrays.fill(this.tickLengths, 50);
		}

		public void tick() {
			long currentTime = System.currentTimeMillis();
			if (this.lasTimestamp != -1L) {
				int elaspedTime = (int) (currentTime - this.lasTimestamp);
				if (elaspedTime == 49 || elaspedTime == 51)
					elaspedTime = 50;
				this.tickLengths[elapsedTicks % 20] = elaspedTime;
			}
			this.lasTimestamp = currentTime;
			if (++elapsedTicks % 20 == 0) {
				double tickSum = 0.0D;
				for (int i : this.tickLengths)
					tickSum += i;
				double tickLength = tickSum / 20.0D;
				this.tps = 50.0D / tickLength * 20.0D;
			}
		}

		public double calculateCurrentAverageTPS() {
			return this.tps;
		}
	}
}
