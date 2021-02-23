package herobrine99dan.heropanel.heroku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import herobrine99dan.heropanel.protocol.HTTPConnection;

public class HerokuThreadConnection extends Thread {
	private Socket insocket;
	private String publicIp;

	HerokuThreadConnection(Socket insocket, String publicIp) {
		this.insocket = insocket;
		this.publicIp = publicIp;
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
					this.sendWebsite(insocket.getOutputStream());
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

	private void sendWebsite(OutputStream ctx) throws IOException {
		ctx.write(stringToASCII("HTTP/1.1 200 OK"));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Content-Type: text/html"));
		ctx.write(blankLine);
		ctx.write(stringToASCII("Connection: close"));
		ctx.write(blankLine);
		ctx.write(blankLine);
		ctx.write(stringToASCII("<html> <body> <h1> Remember A248 is the best person!</h1> <h2> IP: " + publicIp
				+ " </h2> </body> </html>"));
		ctx.flush();
		ctx.close();
	}
}
