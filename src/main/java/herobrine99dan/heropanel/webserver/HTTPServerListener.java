package herobrine99dan.heropanel.webserver;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import herobrine99dan.heropanel.ReflectionUtility;
import herobrine99dan.heropanel.UniportWebServer;
import herobrine99dan.heropanel.protocol.HTTPRequestEvent;
import herobrine99dan.heropanel.protocol.HTTPResponseCode;
import herobrine99dan.heropanel.webserver.features.HeroPanel;
import herobrine99dan.heropanel.webserver.features.LogFilter;

public class HTTPServerListener implements Listener {

	private ConcurrentHashMap<String, Integer> connections = new ConcurrentHashMap<String, Integer>();
	private final HeroPanel panel;
	private final boolean ngrokCompatibility; // TODO Use ngrokCompatibility and lunch a tunnel with ngrok that
	private final String httpTunnel = "";
	private final long maxRequestsPerSecondByIP;
	private final UniportWebServer main;

	public HTTPServerListener(UniportWebServer main) {
		this.main = main;
		this.panel = new HeroPanel(main);
		ngrokCompatibility = main.getHeroPanelConfig().ngrokCompatibility();
		maxRequestsPerSecondByIP = main.getHeroPanelConfig().maxRequestsPerSecondByIP();
		((Logger) LogManager.getRootLogger()).addFilter(new LogFilter(panel));
	}

	public void cleanConnectionsCache() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (String key : connections.keySet()) {
					connections.remove(key);
				}
			}
		}.runTaskTimerAsynchronously(main, 19, 1);
	}

	public boolean isConnectionComingFromCorrectSource(HTTPRequestEvent event) {
		if (ngrokCompatibility) {
			// If the server should use Ngrok Tunnels, check if they are actually doing
			// this.
			// Even if this isn't a secure method because connection can actually spoof
			// Ngrok
			// Ip and Host (using HTTP Properties on connection), the only one able to
			// exploit this is the one who knows the ngrok url.Since it is randomly
			// generated
			// every time you restart the server, if you don't give the url no one will be
			// able to exploit this because they don't know what url to connect to.
			return !event.getNgrokIp().isEmpty() && event.getHost().equals(httpTunnel);
		}
		// If the server doesn't use Ngrok Tunnels, then it will use the router's
		// connection.
		// In this case, the IP of the connection can't be spoofed, since the connection
		// is
		// received and handled directly by the router and the server.
		return true;
	}

	@EventHandler
	public void onConnection(HTTPRequestEvent event) throws IOException, GeneralSecurityException {
		// Check if the connection comes from the correct source (it is very very very
		// very hard
		// to spoof if the owner doesn't give the http url, else it becomes
		if (!isConnectionComingFromCorrectSource(event)) {
			event.setMessage(
					"Sorry, we aren't able to get your ip through the ngrok api. Please start the ngrok http tunnel and then the tcp tunnel! (Host: "
							+ event.getHost() + ")");
			return;
		}
		// Handle BruteForce Attacks
		String ipAddress = ngrokCompatibility ? event.getNgrokIp() : event.getAddress();
		Integer maxConnections = connections.getOrDefault(ipAddress, 0);
		connections.put(ipAddress, maxConnections + 1);
		if (maxConnections != null) {
			if (maxConnections + 1 > maxRequestsPerSecondByIP) {
				event.setHttpcode(HTTPResponseCode.Code429);
				return;
			}
		}
		if (event.getConnection().request.startsWith("/panel")) {
			panel.handleThePages(event);
			return;
		}
		String req = URLDecoder.decode(event.getConnection().request, StandardCharsets.UTF_8.name());
		boolean download = false;
		if (req.endsWith("?download") || req.endsWith("?download=true") || req.endsWith("?download=1")) {
			req = req.substring(0, req.indexOf('?'));
			download = true;
		}
		if (req.contains("?")) {
			int j = req.indexOf('?');
			req = req.substring(0, j);
		}
		if (req.equals("/"))
			req = "/index.html";
		File f = new File(main.getDataFolder() + req);
		if (f.isDirectory())
			f = new File(f, "index.html");
		if (f.getName().startsWith("heropanel-")) {
			event.setMessage("Sorry, you aren't able to see those files!");
			event.setHttpcode(HTTPResponseCode.Code403);
			return;
		}
		if (f.getName().startsWith("config.yml")) {
			event.setMessage("Sorry, you aren't able to see this file!");
			event.setHttpcode(HTTPResponseCode.Code403);
			return;
		}
		if (f.exists() && !f.isDirectory()) {
			try {
				event.setImage(Files.readAllBytes(f.toPath()));
				if (download) {
					event.setContentType("*/*\nContent-Disposition: attachment");
				} else if (!f.getName().endsWith(".html") && !f.getName().endsWith(".htm")) {
					event.setContentType("*/*");
				}
				if (f.getName().endsWith(".js")) {
					event.setContentType("application/javascript");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HeroPanel getHeroPanel() {
		return panel;
	}

}
