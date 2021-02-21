package herobrine99dan.heropanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;

import herobrine99dan.heropanel.protocol.HTTPConnection;
import herobrine99dan.heropanel.protocol.HTTPRequestEvent;

public class ThreadSocket extends Thread {
	private Socket insocket;

	ThreadSocket(Socket insocket) {
		this.insocket = insocket;
		this.start();
	}

	@Override
	public void run() {
		try {
			InputStream is = insocket.getInputStream();
			PrintWriter out = new PrintWriter(insocket.getOutputStream());
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
				HTTPConnection connection = new HTTPConnection();
				connection.parse(is, reader);
				boolean isHTTP = connection.isHttp;
				if (isHTTP) {
					this.sendWebsite(insocket.getOutputStream(), connection);
				}
			}
			out.close();
			insocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] stringToASCII(String s) {
		return s.getBytes(StandardCharsets.US_ASCII);
	}

	private static final byte[] blankLine = new byte[] { 13, 10 };

	private void sendWebsite(OutputStream ctx, HTTPConnection connection) throws IOException {
		HTTPRequestEvent event = new HTTPRequestEvent(connection, insocket);
		Bukkit.getServer().getPluginManager().callEvent(event);
		ctx.write(stringToASCII("HTTP/1.1 " + event.getHttpcode().getResponse()));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Content-Type: " + event.getContentType()));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Connection: close"));
		ctx.write(blankLine);
		ctx.write(blankLine);
		if (event.useByteArray()) {
			ctx.write(event.byteArr);
		} else {
			ctx.write(event.toSend().getBytes());
		}
		ctx.flush();
		ctx.close();
	}
}
