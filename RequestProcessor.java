package webserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {
	private Socket connection;
	private BufferedWriter os;
	private Logger l;
	private Object lock;
	private ConfigObject co;
	private InputStream cis;
	private HttpParser hp;
	
	public RequestProcessor(Socket connection, Logger l,Object lock, ConfigObject co) {
		this.co = co;
		this.connection = connection;
		this.l = l;
		this.lock = lock;
		try {
			this.cis = connection.getInputStream();
		} catch (IOException e) {
			l.log(Level.SEVERE,"Error: Failed to open InputStream for socket");
		}
		
		try {
			this.os = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		} catch (IOException e) {
			l.log(Level.SEVERE,"Error: Failed to open outStream for socket");
		}
	}

	@Override
	public void run() {
		int rc = 200;
		l.log(Level.INFO, "Request taken by thread: " + Thread.currentThread().getName());
		InputStream cis = null;
		try {
			cis = connection.getInputStream();
		} catch (IOException e) { }
		
		while (true) {
			l.log(Level.INFO, "Waiting for request to parse");
			hp = new HttpParser(cis);
			
		try {
			rc = hp.parseRequest();
		} catch (IOException e) {	};
		
		switch (rc) {
		case 0   : // connection closed by client
				break;
		case 200 :
			l.log(Level.INFO,"Processing " + hp.getMethod() + " request");
			if (hp.getMethod().equals("HEAD") == true)
				sendHeadOkResponse();
			else if (hp.getMethod().equals("GET") == true)
				sendGetOkResponse();
			else if (hp.getMethod().equals("POST") == true)
				sendPostOkResponse();
			else if (hp.getMethod().equals("PUT") == true)
				sendPutOkResponse();
			else if (hp.getMethod().equals("DELETE") == true)
				sendDeleteOkResponse();
			break;
		case 400 :
			sendBadRequestResponse();
			break;
		case 501 :
			sendMethodNotImplemented();
			break;
		case 505 :
			sendUnsupportedHttpVersion();
			break;
		default :
			sendServerInternalErrorResponse();
			break;
		}
		
		// client closes the connection
		if (rc == 0) {
			l.log(Level.INFO, "Client closed the connection");
			break;
		}
		
		String connHeader = hp.getHeader("Connection");
		if (connHeader != null && connHeader.equals("close") == true) {
			l.log(Level.INFO, "Client requested for close");
			break;
		}
		else {
			l.log(Level.INFO, "Connection header missing keep-alive");
		}
		} // end while
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end run

	private void sendPostOkResponse() {
		File f = new File(co.getContextRoot()+hp.getRequestURL());
		// client did not provide the filename to create. Create a filename
		if (hp.getRequestURL().equals("/")) 
			f = new File(co.getContextRoot() + "ws" + System.nanoTime());

		boolean rc = false;
		if (f.exists() == false) {
			try {
				rc = f.createNewFile();
				if (rc == true) { 
					l.log(Level.INFO, "file created successfully");
				// Check for numberformatException latter
				int contentLen = Integer.parseInt(hp.getHeader("Content-Length"));
				if ( contentLen > 0) {
					FileWriter fw = new FileWriter(f);
					while(contentLen > 0) {
						fw.write(cis.read());
						contentLen--;
						
					} // endwhile
					fw.flush();
						fw.close();
					} // end content length check
				} // end create file
			} catch (IOException e) {
				sendServerInternalErrorResponse();
			}
		}
		else {
			// I do not want user to use POST to update already existing files
			// Also do not want to user to touch any directory
			sendConflictResponse();
			return;
		}
		
		try {
				os.write("HTTP/1.1 201 Created \r\n");
				os.write("Content-Length: 0\r\n");
				os.write("Connection: Keep-Alive\r\n");
				Date now = new Date();
				os.write("Date: " + now + "\r\n");
				os.write("\r\n\r\n");
				os.flush();
			} catch (IOException e) {
					sendServerInternalErrorResponse();
				}
	} // POST OK

	private void sendConflictResponse() {
		try {
			os.write("HTTP/1.1 409 Conflict \r\n");
			os.write("Connection: Keep-Alive\r\n");
			os.write("\r\n\r\n");
			os.flush();
		} catch (IOException e) {
				sendServerInternalErrorResponse();
			}
	} // Send Conflict

	private void sendPutOkResponse() {
		File f = new File(co.getContextRoot()+hp.getRequestURL());
		if (f.exists() == false) {
			sendFileNotFound();
			return;
		}
		
		if (f.isDirectory() == true) {
			sendForbiddenResponse();
			return;
		}
		// check for number format exception latter.
		if (Integer.parseInt(hp.getHeader("Content-Length")) == 0)
			try {
				new FileOutputStream(f, false).close();
			} catch (FileNotFoundException e1) {
				sendFileNotFound();
			} catch (IOException e1) {
				sendServerInternalErrorResponse();
			}
		
		// replace the existing content with new one.
		if (f.exists() == true) {
			try {
				// truncate
				new FileOutputStream(f, false).close();
				// Check for numberformatException latter
				int contentLen = Integer.parseInt(hp.getHeader("Content-Length"));
				if ( contentLen > 0) {
					FileWriter fw = new FileWriter(f);
					while(contentLen > 0) {
						fw.write(cis.read());
						contentLen--;
					} // endwhile
					fw.flush();
					fw.close();
				}
			} catch (IOException e) {
				sendServerInternalErrorResponse();
			}
		try {
			os.write("HTTP/1.1 200 OK \r\n");
			os.write("Content-Length: 0\r\n");
			os.write("Connection: Keep-Alive\r\n");
			Date now = new Date();
			os.write("Date: " + now + "\r\n");
			os.write("\r\n\r\n");
			os.flush();
		} catch (IOException e) {
				sendServerInternalErrorResponse();
			}
		} // end if exists
	} // end PUT OK

	private void sendDeleteOkResponse() {
		File f = new File(co.getContextRoot()+hp.getRequestURL());
		if (f.exists() == false) {
			sendFileNotFound();
			return;
		}
		
		if (f.isDirectory() == true)
			sendForbiddenResponse();

		boolean rc = false;
		try {
			rc = f.delete();
			if (rc == true) {
				os.write("HTTP/1.1 200 OK \r\n");
				os.write("Content-Length: 0\r\n");
				os.write("Connection: Keep-Alive\r\n");
				Date now = new Date();
				os.write("Date: " + now + "\r\n");
				os.write("\r\n\r\n");
				os.flush();
			} else
				sendServerInternalErrorResponse();
			} catch (IOException e) {
					sendServerInternalErrorResponse();
				}
	} // end DELETE OK

	private void sendForbiddenResponse() {
		try {
				os.write("HTTP/1.1 403 Forbidden \r\n");
				os.write("Content-Length: 0\r\n");
				os.write("Connection: Keep-Alive\r\n");
				Date now = new Date();
				os.write("Date: " + now + "\r\n");
				os.write("\r\n\r\n");
				os.flush();
			} catch (IOException e) {
					sendServerInternalErrorResponse();
				}
	} // end Forbidden

	private void sendFileNotFound() {
		try {
			os.write("HTTP/1.1 404 File Not Found \r\n");
			os.write("Content-Length: 0\r\n");
			os.write("Connection: Keep-Alive\r\n");
			Date now = new Date();
			os.write("Date: " + now + "\r\n");
			os.write("\r\n\r\n");
			os.flush();
			} catch (IOException e) {
				sendServerInternalErrorResponse();
			}
	} // end 404

	private void sendUnsupportedHttpVersion() {
		try {
		os.write("HTTP/1.1 505 HTTP Version Not Supported\r\n");
		os.write("Content-Length: 0\r\n");
		os.write("Connection: Keep-Alive\r\n");
		Date now = new Date();
		os.write("Date: " + now + "\r\n");
		os.write("\r\n\r\n");
		os.flush();
		} catch (IOException e) {
			sendServerInternalErrorResponse();
		}
	} // end 505

	private void sendMethodNotImplemented() {
		try {
		os.write("HTTP/1.1 501 Not Implemented \r\n");
		os.write("Content-Length: 0\r\n");
		os.write("Connection: Keep-Alive\r\n");
		Date now = new Date();
		os.write("Date: " + now + "\r\n");
		os.write("\r\n\r\n");
		os.flush();
		} catch (IOException e) {
			sendServerInternalErrorResponse();
		}
	}

	private void sendBadRequestResponse() {
		try {
		os.write("HTTP/1.1 400 Bad Request\r\n");
		os.write("Content-Length: 0\r\n");
		os.write("Connection: Keep-Alive\r\n");
		Date now = new Date();
		os.write("Date: " + now + "\r\n");
		os.write("\r\n\r\n");
		os.flush();
		} catch (IOException e) {
			sendServerInternalErrorResponse();
		}
	}
	
	private void sendGetOkResponse() {
		try {
		File f = new File(co.getContextRoot()+hp.getRequestURL());
		if (f.exists() == false) {
			sendFileNotFound();
			return;
		}
		
		FileReader fr = null;
		BufferedReader br = null;
		
		if (f.isFile() == true && f.canRead() == true) {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
		}
			
		os.write("HTTP/1.1 200 OK\r\n");
		os.write("Content-Length:" + f.length() +"\r\n");
		os.write("Connection: Keep-Alive\r\n");
		os.write("\r\n\r\n");
		Date now = new Date();
		os.write("Date: " + now + "\r\n");
		os.flush();
		
		String line = br.readLine();
		
		while (line !=null) {
			System.out.println(line);
			os.write(line);
			line = br.readLine();
		} 
		os.flush();
		br.close();
		fr.close();
		
		} catch (IOException e) {	}
	} // end GET OK
	
	private void sendHeadOkResponse() {
		System.out.println("Inside HEAD Response");
		try {
			os.write("HTTP/1.1 200 OK\r\n");
			os.write("Content-Length: 0\r\n");
			os.write("Connection: Keep-Alive\r\n");
			os.write("\r\n\r\n");
			Date now = new Date();
			os.write("Date: " + now + "\r\n");
			os.flush();
			} catch (IOException e) {	}
	} // end HEAD OK
	
	private void sendServerInternalErrorResponse() {
		try {
		os.write("HTTP/1.1 500 Internal Server Error \r\n");
		os.write("Content-Length: 0\r\n");
		os.write("Connection: Keep-Alive\r\n");
		Date now = new Date();
		os.write("Date: " + now + "\r\n");
		os.write("\r\n\r\n");
		os.flush();
		} catch (IOException e) { }
		
	}
} // end class