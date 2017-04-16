package webserver;

import java.io.File;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServer {
  static Logger l;
	public static void main(String[] args) {

		// need to take care of users who are member of root group
		if (System.getProperty("user.name").equals("root")) {
			System.err.println("Cannot run as root");
			System.exit(-1);
		}

		ServerSocket ss = null;

		try {
			// Configuration is stored in a XML file. Parse it.
      if (args.length < 1) {
        System.err.println("Error: Config filename required as argument");
        System.err.println("Usage: {program_name} <configFileName>");
        System.exit(-1);
      }

			ConfigObject co = new ConfigObject(args[0]);
			
			// Enable file type logging
		  l = co.getLogObject();

		  // Create worker threads. These would handle the HTTP request.
		  ExecutorService es = Executors.newFixedThreadPool(co.getThreadCount());
			
			// let the server begin
			InetAddress lb = InetAddress.getByName(co.getServerIP());
			ss = new ServerSocket(co.getPort(), 10, lb);
			l.log(Level.INFO, "Server started on port " + co.getPort());
			
			while (true) {
				Socket cs = null;
				cs = ss.accept();
				l.log(Level.INFO, "New Request arrived from port: " + cs.getPort());
				es.execute(new RequestProcessor(cs, l, co));
			}
		} catch (IOException e) {	
			l.log(Level.INFO, e.getMessage());
		}
	} // end main
} // end class
