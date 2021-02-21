package herobrine99dan.heropanel.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class HTTPConnection {

	public boolean isHttp = false;
	public String request = "";
	public String method = "";
	public String host = "";
	public String ngrokIp = "";

	public byte[] postData;

	public void parse(InputStream stream, BufferedReader buffer) throws IOException {
		String line;
		int postDataI = -1;
		while ((line = buffer.readLine()) != null) {
			if (line.isEmpty()) {
				break;
			}
			if (line.endsWith(" HTTP/1.1")) {
				isHttp = true;
				method = line.substring(0, line.indexOf(" "));
				request = line.replace(method + " ", "").replace(" HTTP/1.1", "");
			} else if (line.startsWith("Host: ")) {
				host = line.replace("Host: ", "");
			} else if (line.startsWith("Content-Length:")) {
				postDataI = Integer.valueOf(line.replaceFirst("Content-Length: ", ""));
			} else if (line.startsWith("X-Forwarded-For:")) {
				ngrokIp = line.replace("X-Forwarded-For: ", "");
			}
		}
		if (method.equalsIgnoreCase("POST")) {
			if (postDataI > 0) {
				byte[] byteArray = new byte[postDataI];
				stream.read(byteArray, 0, postDataI);
				this.postData = byteArray;
			}
		}
	}
}
