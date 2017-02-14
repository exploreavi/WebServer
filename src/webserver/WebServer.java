package webserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServer {

	public static void main(String[] args) {

		// need to take care of users who are member of root group
		if (System.getProperty("user.name").equals("root")) {
			System.err.println("Cannot run as root");
			System.exit(-1);
		}

		// Create worker threads. These would handle the HTTP request.
		ExecutorService es = Executors.newFixedThreadPool(80);

		// Use for synchronization
		Object lock = new Object();
		ServerSocket ss = null;

		try {
			// Configuration is stored in a XML file. Parse it.
			ConfigObject co = new ConfigObject("config.xml");
			
			// Enable file type logging
			Logger l = co.getLogObject();
			
			// let the server begin
			InetAddress lb = InetAddress.getLoopbackAddress();
			ss = new ServerSocket(co.getPort(), 10, lb);
			l.log(Level.INFO, "Server started on port " + co.getPort());
			
			while (true) {
				Socket cs = null;
				cs = ss.accept();
				l.log(Level.INFO, "New Request arrived from port: " + cs.getPort());
				es.execute(new RequestProcessor(cs, l, lock, co));
			}
		} catch (IOException e) {	}
	} // end main
} // end class
