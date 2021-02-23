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
		authHandler = new AuthenticationHandler(main.getHeroPanelConfig().TOTPKey(),
				main.getHeroPanelConfig().timeZone());
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
				String secret = Long.toString(TOTP.generateCurrentNumber(authHandler.getBase32Secret(),
						main.getHeroPanelConfig().timeZone()));
				System.out.println("Secret Code: " + secret);
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
			if (!main.getHeroPanelConfig().TOTPKey().isEmpty()) {
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n"
						+ "    <h3>Sorry, but you already setupped the HeroPanel!</h3>\n" + "  </body>\n" + "</html>"));
				return;
			}
			if (argument.equals("setup")) {
				if (!isNumber(argumentValue)) {
					event.setMessage("There was an error while setupping HeroPanel! Please setup it again!");
					return;
				}
				String key = TOTP.generateBase32Secret();
				main.getConfig().set("TOTPKey", key);
				main.getConfig().set("timeZone", Integer.parseInt(argumentValue) * 1000);
				main.saveConfig();
				String qrCode = TOTP.qrImageUrl("HeroPanel", key);
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n"
						+ "    <h3>Hello, to setup correctly the HeroPanel please download a 2FA Authenticator, i suggest to use Google Authenticator.</h3>\n"
						+ "    <h3>Then scan this qrcode and here you are!</h3>\n" + "    <img src=\"qrcodelink\">\n"
						+ "  </body>\n" + "</html>").replace("qrcodelink", qrCode));
				main.setupConfigurationOrReload();
				authHandler = new AuthenticationHandler(main.getHeroPanelConfig().TOTPKey(),
						main.getHeroPanelConfig().timeZone());
				return;
			}
			if (argument.equals("")) {
				event.setMessage(("<html>\n" + "  <body>\n" + "    <h1>HeroPanel Setup</h1>\n"
						+ "    <h3>Hello, please setup your timezone here. This Step is an IMPORTANT STEP. If the timezone isn't correct you won't be able to access to the panel."
						+ " <h3> Current MilliSecond Time is: " + System.currentTimeMillis() + " or better: "
						+ Calendar.getInstance().getTime() + "</h3> "
						+ "<h3> What you have to put in this InputBox is just the timezone of your country."
						+ " You must use a number that represent your timezone in seconds. In order to convert hours to seconds just use 'hours*3600', "
						+ "if your timezone changes also minutes, after you have converted hours to seconds, convert minutes to seconds (just do 'minutes*60') and add the result to the timezone converted in seconds format."
						+ " Since the Timezone can add or remove hours, HeroPanel will also handle subtractions of time."
						+ "<form action=\"/panel/setup\">\n" + "  <label for=\"fname\">TimeZone</label><br>\n"
						+ "  <input type=\"text\" id=\"setup\" name=\"setup\" value=\"Europe/Paris\"><br>\n"
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
