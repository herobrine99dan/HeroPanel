package herobrine99dan.heropanel.heroku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import herobrine99dan.heropanel.UniportWebServer;
import herobrine99dan.heropanel.protocol.HTTPConnection;

public class HerokuHandler extends Thread {

	private ServerSocket server;
	private UniportWebServer main;

	public HerokuHandler(int port, UniportWebServer main) throws IOException {
		this.server = new ServerSocket(port);
		System.out.println("Port: " + port);
		this.main = main;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket insocket = server.accept();
				InputStream is = insocket.getInputStream();
				PrintWriter out = new PrintWriter(insocket.getOutputStream());
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
					HTTPConnection connection = new HTTPConnection(false);
					connection.parse(is, reader);
					boolean isHTTP = connection.isHttp;
					if (isHTTP) {
						this.sendWebsite(insocket.getOutputStream());
					}
				}
				out.close();
				insocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] stringToASCII(String s) {
		return s.getBytes(StandardCharsets.US_ASCII);
	}

	private static final byte[] blankLine = new byte[] { 13, 10 };

	private void sendWebsite(OutputStream ctx) throws IOException {
		ctx.write(stringToASCII("HTTP/1.1 200 OK"));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Content-Type: text/html"));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Connection: close"));
		ctx.write(blankLine);
		ctx.write(blankLine);
		final String heroPanelIp = "<a href=\"baseurl/panel\">Go-In-HeroPanel!</a>.\n".replace("baseurl",
				main.getPublicHeroPanelIp());
		ctx.write(stringToASCII("<html> <body> <h1> Remember A248 is the best person!</h1> <h2> ServerIP: "
				+ main.getPublicServerIp().replaceFirst("tcp://", "") + " </h2> <h2> " + heroPanelIp
				+ "</h2> </body> </html>"));
		ctx.flush();
		ctx.close();
	}

	public void closeServer() throws IOException {
		server.close();
	}

}
