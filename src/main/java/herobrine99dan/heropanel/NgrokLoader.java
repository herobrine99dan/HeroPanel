package herobrine99dan.heropanel;

import java.io.File;
import java.io.IOException;

public class NgrokLoader extends Thread {

	private UniportWebServer main;

	public NgrokLoader(UniportWebServer uniportWebServer) {
		this.main = uniportWebServer;
	}

	@Override
	public void run() {
		try {
			for(File file : new File("./").listFiles()) {
				System.out.println(file.getName());
			}
			executeCommand("./ngrok authtoken " + main.getHeroPanelConfig().ngrokKey());
			//executeCommand("./ngrok tcp " + main.getBukkitPort());
			executeCommand("./ngrok http 5678");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void executeCommand(String command) throws IOException, InterruptedException {
		Runtime.getRuntime().exec(command);
	}

}
