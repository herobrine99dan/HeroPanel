package herobrine99dan.heropanel;

import java.lang.reflect.Field;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;

public class ReflectionUtility {

	private Class<?> serverConnectionClass;
	private Class<?> minecraftServerClass;
	private Object minecraftServerObject;
	private String version;

	public void updateFields() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		this.version = this.getVersion();
		this.serverConnectionClass = this.getNmsClass("ServerConnection");
		this.minecraftServerClass = this.getNmsClass("MinecraftServer");
		this.minecraftServerObject = this.getMinecraftServerObject();
	}

	private Object getMinecraftServerObject() throws IllegalArgumentException, IllegalAccessException {
		for (Field f : minecraftServerClass.getDeclaredFields()) {
			if (f.getType().isAssignableFrom(minecraftServerClass)) {
				f.setAccessible(true);
				return FieldUtils.readStaticField(f);
			}
		}
		return null;
	}

	public Class<?> getServerConnectionClass() {
		return this.serverConnectionClass;
	}

	public Object getServerConnection() throws IllegalArgumentException, IllegalAccessException {
		for (Field f : minecraftServerClass.getDeclaredFields()) {
			if (f.getType().isAssignableFrom(serverConnectionClass)) {
				f.setAccessible(true);
				return f.get(minecraftServerObject);
			}
		}
		return null;
	}

	private Class<?> getNmsClass(String name) throws ClassNotFoundException {
		String className = "net.minecraft.server." + version + "." + name;
		return Class.forName(className);
	}

	private String getVersion() {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		return packageName.substring(packageName.lastIndexOf('.') + 1);
	}

}
