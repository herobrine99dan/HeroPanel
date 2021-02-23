package herobrine99dan.heropanel.webserver.features;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Utility {

	public static double round(double value, double precision) {
		return (double) Math.round(value * precision) / precision;
	}

	public static String getPublicIp(String tunnelName) throws IOException, ParseException {
		URL url = new URL("http://localhost:4040/api/tunnels/" + tunnelName);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(sb.toString());
			return (String) object.get("public_url");
		}
	}
}
