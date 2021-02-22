package herobrine99dan.heropanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HerokuHandler extends Thread {

	private ServerSocket server;
	private UniportWebServer main;

	public HerokuHandler(int port, UniportWebServer main) throws IOException {
		this.server = new ServerSocket(port);
		this.main = main;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = server.accept();
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.println("HTTP/1.0 200 OK");
				out.println("Content-Type: text/html; charset=utf-8");
				out.println("Server: HeroPanel");
				// this blank line signals the end of the headers
				out.println("");
				// Send the HTML page
				out.println("<html> <body> <h1> Remember A248 is the best person!</h1> <h2> IP: " + main.getPublicServerIp() + " </h2> </body> </html>");
				out.flush();
				out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void closeServer() throws IOException {
		server.close();
	}

}
