package herobrine99dan.heropanel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import herobrine99dan.heropanel.protocol.HTTPServerChannelHandler;
import herobrine99dan.heropanel.webserver.HTTPServerListener;
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
	private boolean isUsingCustomHttpserver = false;

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
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HeroPanelConfig getHeroPanelConfig() {
		return config;
	}

}
