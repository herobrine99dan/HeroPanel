package herobrine99dan.heropanel.webserver.features;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import herobrine99dan.heropanel.UniportWebServer;
import herobrine99dan.heropanel.protocol.HTTPRequestEvent;

public class HeroPanel {

	private AuthenticationHandler authHandler;
	private final ConsoleHandler consoleHandler;
	private final TPSHandler tpsHandler;
	private final UniportWebServer main;
	private static final Logger LOGGER = Logger.getLogger(HeroPanel.class.getName());
	private final ResourcesHandler resourcesHandler;

	public HeroPanel(UniportWebServer main) {
		this.main = main;
		authHandler = new AuthenticationHandler(main.getHeroPanelConfig().account());
		consoleHandler = new ConsoleHandler();
		LOGGER.fine("AuthenticationHanler and ConsoleHandler were loaded!");
		tpsHandler = new TPSHandler();
		LOGGER.fine("TPSHandler was constructed correctly!");
		this.resourcesHandler = new ResourcesHandler(this);
	}

	public void setupEverything() {
		tpsHandler.startTask(main);
		resourcesHandler.setup();
	}

	public void handleThePages(HTTPRequestEvent event) throws IOException, GeneralSecurityException {
		String req = "/panel/";
		try {
			req = URLDecoder.decode(event.getConnection().request, StandardCharsets.UTF_8.name()).replaceFirst("/panel",
					"");
		} catch (IllegalArgumentException ex) { // Sometimes for some bad characters URLDecoder.decode() gives an error
			LOGGER.log(Level.FINE, "There was an error while decoding url: " + event.getConnection().request, ex);
			return;
		}
		String ip = event.getAddress();
		if (event.getHost().contains("ngrok.io")) {
			ip = event.getNgrokIp();
		}
		if (req.equals(" ") || req.equals("/") || req.equals("/index.html") || req.equals("")) {
			if (authHandler.isAuthenticated(ip)) {
				event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/dashboard.html'\" />\n" + "");
			} else {
				event.setImage(Files.readAllBytes(new File(main.getDataFolder(), "heropanel-login.html").toPath()));
			}
		}
		String argument = "";
		String argumentValue = "";
		if (req.contains("?")) {
			int j = req.indexOf('?');
			argument = req.substring(j + 1);
			int importantCharPosition = argument.indexOf("=");
			argumentValue = argument.substring(importantCharPosition + 1);
			if (argument.contains("=")) {
				argument = argument.substring(0, importantCharPosition);
			}
			req = req.substring(0, j);
		}
		if (argument.equals("code") && !authHandler.isAuthenticated(ip)) {
			boolean login = authHandler.loginHandler(ip, argumentValue);
			if (login) {
				event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/dashboard.html'\" />\n" + "");
			} else {
				event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/'\" />\n" + "");
			}
			return;
		}
		if (argument.equals("quit") && authHandler.isAuthenticated(ip)) {
			authHandler.logoutHandler();
			event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/'\" />\n" + "");
			return;
		}
		if (argument.equals("logout") && authHandler.isAuthenticated(ip)) {
			authHandler.logoutHandler();
			event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/'\" />\n" + "");
			return;
		}
		if (req.equals("/dashboard.html") && authHandler.isAuthenticated(ip)) {
			event.setImage(Files.readAllBytes(new File(main.getDataFolder(), "heropanel-dashboard.html").toPath()));
			return;
		} else if (req.startsWith("/dashboard.html") && !authHandler.isAuthenticated(ip)) {
			event.setMessage("<meta http-equiv=\"refresh\" content=\"0; URL='/panel/'\" />\n" + "");
			return;
		}
		if (req.equals("/server") && authHandler.isAuthenticated(ip)) {
			event.setMessage(this.getDashBoardApi());
			return;
		}
		if (req.equals("/setup") && !authHandler.isAuthenticated(ip)) {
			if (!main.getHeroPanelConfig().account().isEmpty()) {
				System.out.println("Account: " + main.getHeroPanelConfig().account());
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n"
						+ "    <h3>Sorry, but you already setupped the HeroPanel!</h3>\n" + "  </body>\n" + "</html>"));
				return;
			}
			if (argument.equals("setup")) {
				main.getConfig().set("account", argumentValue);
				main.saveConfig();
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n"
						+ "    <h3>HeroPanel was setupped correctly!</h3>\n" + "  </body>\n" + "</html>"));
				main.setupConfigurationOrReload();
				authHandler = new AuthenticationHandler(main.getHeroPanelConfig().account());
				return;
			}
			if (argument.equals("")) {
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n" + "<h3> </h3>"
						+ "<form action=\"/panel/setup\">\n" + "  <label for=\"fname\">User and Password</label><br>\n"
						+ "  <input type=\"text\" id=\"setup\" name=\"setup\" value=\"user:password\"><br>\n"
						+ "  <input type=\"submit\" value=\"Submit\">\n" + "</form>" + "  </body>\n" + "</html>"));
				return;
			}

			return;
		}
		if (req.equals("/server/command") && authHandler.isAuthenticated(ip)) {
			final String argumentFinal = argument;
			final String argumentFinalValue = argumentValue;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (argumentFinal.equals("sendcmd")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), argumentFinalValue);
					}
				}
			}.runTask(main);
			event.setMessage("Done!");
			return;
		}
		if (req.equals("/server/consolelog") && authHandler.isAuthenticated(ip)) {
			event.setMessage(this.getConsoleHandler().getLogToSendInJSONForm());
			return;
		}
		if (req.equals("/server/fullconsolelog") && authHandler.isAuthenticated(ip)) {
			event.setMessage(this.getConsoleHandler().getFullLogToSendInJSONForm());
			return;
		}
	}

	private boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static final String dashboardjson = "{\"Status\":1,\"GameType\":\"#gamemode\",\"GameId\":\"#gameid\",\"Version\":\"#version\",\"Map\":\"#map\",\"MaxPlayers\":#maxplayer,\"NumPlayers\":#numplayers,\"Motd\":\"#motd\",\"Tps\":#tps,\"StartTime\":#starttime,\"Memory\":{\"Total\":#memtotal,\"Used\":#memused,\"Free\":#memfree,\"ActualFree\":0,\"ActualUsed\":0},\"CPU\": #cpu}";

	public String getDashBoardApi() {
		String json = dashboardjson;
		long RAM_USED = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long cpu = Math.round(
				ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getProcessCpuLoad()
						* 100);
		float tps = this.tpsHandler.getTPS();
		if (tps > 22) {
			tps = tps / 2;
		}
		String ip = main.getPublicServerIp().replaceFirst("tcp://", "");
		return json.replace("#numplayers", Bukkit.getOnlinePlayers().size() + "")
				.replace("#maxplayer", Bukkit.getMaxPlayers() + "").replace("#memused", RAM_USED + "")
				.replace("#memtotal", Runtime.getRuntime().totalMemory() + "").replace("#cpu", cpu + "")
				.replace("#tps", tps + "")
				.replace("#version", Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf("-")))
				.replace("#map", Bukkit.getWorlds().get(0).getName() + "").replace("#gameid", ip)
				.replace("#gamemode", Bukkit.getDefaultGameMode().name() + "").replace("#motd", Bukkit.getMotd() + "")
				.replace("#starttime", Bukkit.getIdleTimeout() + "").replace("#status", 1 + "")
				.replace("#memfree", Runtime.getRuntime().freeMemory() + "");
	}

	public ConsoleHandler getConsoleHandler() {
		return consoleHandler;
	}
}
