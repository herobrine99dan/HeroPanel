package herobrine99dan.heropanel.webserver.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import herobrine99dan.heropanel.UniportWebServer;

public class ConsoleHandler {

	private final List<String> logsToSend = Collections.synchronizedList(new ArrayList<String>());
	private final List<String> fullLog = Collections.synchronizedList(new ArrayList<String>());
	private final static boolean colorSupport = false;

	public void schedule(UniportWebServer main) {
		new BukkitRunnable() {
			@Override
			public void run() {
				fullLog.clear();
				try {
					for (String s : tailFile(new File("./logs/latest.log").toPath(), 80)) {
						if (!colorSupport) {
							fullLog.add(removeColorCodes(s));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(main, 199, 1);
	}

	public String removeColorCodes(String test) {
		char[] array = test.toCharArray();
		String result = "";
		int counter = 0;
		for (char character : array) {
			if (character == '§') {
				counter++;
			} else if (counter > 0) {
				counter = 0;
			} else {
				result = result + character;
			}
		}
		return result;
	}

	public String getFullLogToSendInJSONForm() {
		JSONObject person = new JSONObject();
		List<String> cloned = new ArrayList<String>(fullLog);
		Collections.reverse(cloned);
		for (int i = 0; i < cloned.size(); i++) {
			person.put("line" + i, cloned.get(i));
		}
		return person.toString();
	}

	public void addLogToSend(String s) {
		logsToSend.add(removeColorCodes(s));
	}

	public String getLogToSendInJSONForm() {
		JSONObject person = new JSONObject();
		for (int i = 0; i < logsToSend.size(); i++) {
			person.put("line" + i, logsToSend.get(i));
		}
		logsToSend.clear();
		return person.toString();
	}

	final List<String> tailFile(final Path source, final int noOfLines) throws IOException {
		FileBuffer fileBuffer = new FileBuffer(noOfLines);
		try (BufferedReader br = new BufferedReader(new FileReader(source.toFile()))) {
			String st;
			while ((st = br.readLine()) != null) {
				if (!st.contains("/DEBUG")) {
					fileBuffer.collect(st);
				}
			}
		}
		return fileBuffer.getLines();
	}

	private final class FileBuffer {
		private int offset = 0;
		private final int noOfLines;
		private final String[] lines;

		public FileBuffer(int noOfLines) {
			this.noOfLines = noOfLines;
			this.lines = new String[noOfLines];
		}

		public void collect(String line) {
			lines[offset++ % noOfLines] = line;
		}

		public List<String> getLines() {
			return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
					.mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
		}
	}

}
