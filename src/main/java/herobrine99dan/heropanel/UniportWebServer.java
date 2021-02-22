package herobrine99dan.heropanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import herobrine99dan.heropanel.protocol.HTTPServerChannelHandler;
import herobrine99dan.heropanel.webserver.HTTPServerListener;
import herobrine99dan.heropanel.webserver.features.Utility;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.helper.ConfigurationHelper;

public class UniportWebServer extends JavaPlugin implements Listener {

	private HeroPanelConfig config;
	private ReflectionUtility reflection;
	private HTTPServerListener listener;
	private CustomHTTPServer httpServer;
	private HerokuHandler herokuHandler;
	private boolean isUsingCustomHttpserver = false;
	private volatile String publicServerIp = "";

	public void onLoad() {
		this.reflection = new ReflectionUtility();
		try {
			reflection.updateFields();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException
				| IllegalAccessException | NoSuchFieldException e1) {
			e1.printStackTrace();
		}
		setupConfigurationOrReload();
		if (config.portToUse() == -1) {
			try {
				injectAndFixIssues(reflection);
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				e.printStackTrace();
			}
		} else if (config.portToUse() == 0) {
			try {
				BufferedReader brTest = new BufferedReader(new FileReader(new File("./portToBind.txt")));
				final String text = brTest.readLine();
				brTest.close();
				int port = Integer.parseInt(text.replaceFirst("port:", ""));
				loadHTTPServer(5678);
				isUsingCustomHttpserver = true;
				herokuHandler = new HerokuHandler(port, this);
				herokuHandler.start();
				new NgrokLoader(this).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				loadHTTPServer(config.portToUse());
				isUsingCustomHttpserver = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.listener = new HTTPServerListener(this);
	}

	public void loadHTTPServer(int port) throws IOException {
		httpServer = new CustomHTTPServer(port);
		httpServer.start();
	}

	public void onEnable() {
		/*try {
			JSONObject tcpTunnel = Utility.startAnotherNgrokTunnel(Bukkit.getPort(), "tcp");
			publicServerIp = (String) tcpTunnel.getOrDefault("public_url", "");
		} catch (IOException | ParseException e) {
			publicServerIp = "Error: " + e.getMessage();
			e.printStackTrace();
		}*/
		this.getServer().getPluginManager().registerEvents(listener, this);
		listener.cleanConnectionsCache();
		listener.getHeroPanel().setupEverything();
	}

	public void setupConfigurationOrReload() {
		HeroPanelConfig config;
		try {
			config = new ConfigurationHelper<>(this.getDataFolder().toPath(), "config.yml",
					new SnakeYamlConfigurationFactory<>(HeroPanelConfig.class, ConfigurationOptions.defaults()))
							.reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			System.err.println("Your configuration is invalid. Fix it and try again");
			ex.printStackTrace();
			return;
		}
		this.config = config;
	}

	@SuppressWarnings("unchecked")
	public void injectAndFixIssues(ReflectionUtility utils) throws IllegalArgumentException, IllegalAccessException {
		Object serverConnection = utils.getServerConnection();
		try {
			Field g = utils.getServerConnectionClass().getDeclaredField("g");
			g.setAccessible(true);
			List<ChannelFuture> oldList = (List<ChannelFuture>) g.get(serverConnection);
			for (ChannelFuture f : oldList) {
				final Channel channel = f.channel();
				for (Entry<String, ChannelHandler> pipeline : channel.pipeline()) {
					if (pipeline.getKey().equals("UniportWebServerHandler")) {
						this.getLogger().info("UniportWebServer detected a reload!");
						channel.pipeline().remove(pipeline.getKey());
					}
				}
				channel.pipeline().addFirst("UniportWebServerHandler", new HTTPServerChannelHandler());
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void detectAndRemoveOldChannelHandler(ReflectionUtility utils)
			throws IllegalArgumentException, IllegalAccessException {
		Object serverConnection = utils.getServerConnection();
		try {
			Field g = utils.getServerConnectionClass().getDeclaredField("g");
			g.setAccessible(true);
			List<ChannelFuture> oldList = (List<ChannelFuture>) g.get(serverConnection);
			for (ChannelFuture f : oldList) {
				final Channel channel = f.channel();
				for (Entry<String, ChannelHandler> pipeline : channel.pipeline()) {
					if (pipeline.getKey().equals("UniportWebServerHandler")) {
						this.getLogger().info("UniportWebServer detected a reload!");
						channel.pipeline().remove(pipeline.getKey());
					}
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		try {
			detectAndRemoveOldChannelHandler(reflection);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		if (isUsingCustomHttpserver) {
			try {
				this.httpServer.closeServer();
				if (herokuHandler != null) {
					herokuHandler.closeServer();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HeroPanelConfig getHeroPanelConfig() {
		return config;
	}

	public String getPublicServerIp() {
		return publicServerIp;
	}

}
