package herobrine99dan.heropanel;

import java.io.IOException;
import java.net.ServerSocket;

public class CustomHTTPServer extends Thread {

	private ServerSocket server;

	public CustomHTTPServer(int port) throws IOException {
		this.server = new ServerSocket(port);
	}

	@Override
	public void run() {
		while (true) {
			if (!server.isClosed()) {
				try {
					new ThreadSocket(server.accept());
				} catch (IOException e) {
					break; //ServerSocket is closed
				}
			} else {
				break;
			}
		}
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.server = null;
	}

	/**
	 * Close the server safely.
	 * @throws IOException 
	 */
	public void closeServer() throws IOException {
		if (server != null) {
			if (!server.isClosed()) {
				server.close();
			}
		}
	}

	public ServerSocket getServer() {
		return server;
	}
}
