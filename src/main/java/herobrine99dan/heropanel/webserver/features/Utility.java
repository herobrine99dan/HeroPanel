package herobrine99dan.heropanel.webserver.features;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Utility {
	
	public static double round(double value, double precision) {
		return (double) Math.round(value * precision) / precision;
	}

	/**
	 * Start another tunnel using the Ngrok's api
	 * 
	 * @param portNgrok;     the port that Ngrok will forward (You can make more
	 *                       tunnels that points to the same ip address)
	 * @param protocolNgrok; the protocol that Ngrok should use (for free accounts
	 *                       you can choose beetween http (https) and tcp)
	 * @throws IOException
	 * @throws ParseException
	 */
	public static JSONObject startAnotherNgrokTunnel(int portNgrok, String protocolNgrok)
			throws IOException, ParseException {
		URL url = new URL("http://localhost:4040/api/tunnels");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		String jsonInputString = "{\n" + " \"addr\": \"superPort\",\n" + " \"proto\": \"protocolNgrok\",\n"
				+ " \"name\": \"serverip\"\n" + "}".replaceFirst("superPort", Integer.toString(portNgrok))
						.replaceFirst("protocolNgrok", protocolNgrok);
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}
		int code = con.getResponseCode();
		JSONObject object = new JSONObject();
		JSONParser parser = new JSONParser();
		object.put("httpcode", code);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			JSONObject returned = (JSONObject) parser.parse(response.toString());
			object.put("public_url", returned.get("public_url"));
			object.put("proto", returned.get("proto"));
		}
		return object;
	}
}
