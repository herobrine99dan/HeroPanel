package herobrine99dan.heropanel.initializer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeroPanelInitializer {
	private static ScheduledExecutorService scheduler;

	public static void main(String[] args) throws IOException, InterruptedException {
		//We Setup main things
		scheduler = Executors.newSingleThreadScheduledExecutor();
		setupTokens();
		backupFolders();
		automaticBackups();
		//We run the Minecraft Server
		System.out.println("Lunching Minecraft Server...");
		Process process = Runtime.getRuntime().exec("java -Xmx384M -jar server.jar nogui");
		System.out.println(
				"Process with pid: " + process.pid() + " on user " + process.info().user().get() + " was started!");
		//When the server actually stop/crash we print the exit code and we do a backup (for security reasons)
		int exitCode = process.waitFor();
		System.out.println("The Minecraft Server just stopped! Exit code: " + exitCode);
		scheduler.shutdown();
		scheduler.shutdownNow();
		backupFolders();
		System.out.println("The last backup was done!");
	}
	
	public static void setupTokens() {
		
	}

	private static void backupFolders() throws InterruptedException, IOException {
		Runtime.getRuntime().exec("chmod +x sync.sh");
		System.out.println("Exit code of backup system: " + Runtime.getRuntime().exec("./sync.sh").waitFor());
	}

	private static void automaticBackups() {
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					backupFolders();
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, 30, TimeUnit.SECONDS);
	}
}
