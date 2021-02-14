package herobrine99dan.heropanel.webserver.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;

public class ConsoleHandler {
	private final List<String> logsToSend = Collections.synchronizedList(new ArrayList<String>());
	private final List<String> fullLog = Collections.synchronizedList(new ArrayList<String>());

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
		for (int i = 0; i < cloned.size(); i++) {
			String s = cloned.get(i);
			person.put("line" + i, s);
		}
		return person.toString();
	}

	public void addLogToSend(String s) {
		fullLog.add(removeColorCodes(s));
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
}
