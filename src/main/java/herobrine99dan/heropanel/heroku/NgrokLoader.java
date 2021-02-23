package herobrine99dan.heropanel.heroku;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import herobrine99dan.heropanel.UniportWebServer;

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
			//executeCommand("./ngrok authtoken " + main.getHeroPanelConfig().ngrokKey());
			//executeCommand("./ngrok tcp " + main.getBukkitPort());
			replaceStringsOnFile();
			executeCommand("./ngrok start -config ./ngrokconfig.yml --all");
			//executeCommand("./ngrok http 5678");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void replaceStringsOnFile() throws IOException {
		Path path = Paths.get("ngrokconfig.yml");
		Charset charset = StandardCharsets.UTF_8;

		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("Tokener", main.getHeroPanelConfig().ngrokKey());
		Files.write(path, content.getBytes(charset));
	}

	private void executeCommand(String command) throws IOException, InterruptedException {
		Runtime.getRuntime().exec(command);
	}

}
