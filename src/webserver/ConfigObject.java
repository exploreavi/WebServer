package webserver;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// make it a inner class
public class ConfigObject {
	public ConfigObject(String configFileName) {
		initConfig(configFileName);
	}

	private String port;
	private String logPath;
	private String logFile;
	private String logLevel;
	private String contextRoot;
	private Logger l;
  private int threadCount;
  private String serverIP;
  private long httpTimeout;

	// All setter except port and ip could be used to changes config without restarting the server

  public long getHttpTimeout() {
    return this.httpTimeout;
  }

	public Logger getLogObject() {
		return l;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public int getPort() {
		return Integer.parseInt(this.port);
	}

	public String getLogPath() {
		return logPath;
	}

	public String getLogFile() {
		return logFile;
	}

	public String getLogLevel() {
		return logLevel;
	}

  public int getThreadCount() {
      return threadCount;
  }

  public String getServerIP() {
      return serverIP;
  }

	private void initConfig(String configFile) {
		File inputFile = new File(configFile);
    if (!inputFile.exists()) {
      System.err.println("Error: Config file \"" + inputFile + "\"" + " does not exist.!!!");
      System.exit(-2);
    }
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
		}
		Document doc = null;
		try {
			doc = dBuilder.parse(inputFile);
		} catch (SAXException e) {
		} catch (IOException e) {
      e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("config");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				this.port = eElement.getElementsByTagName("port").item(0).getTextContent();

				this.logPath = eElement.getElementsByTagName("logPath").item(0).getTextContent();

				this.contextRoot = eElement.getElementsByTagName("contextRoot").item(0).getTextContent();

				this.logFile = eElement.getElementsByTagName("logFile").item(0).getTextContent();

				this.serverIP = eElement.getElementsByTagName("ipaddr").item(0).getTextContent();

				this.threadCount = Integer.parseInt(eElement.getElementsByTagName("threadCount").item(0).getTextContent());

				this.httpTimeout = Long.parseLong(eElement.getElementsByTagName("httpTimeout").item(0).getTextContent());

			} // end if
		} // end for

		this.l = Logger.getLogger("WebServer");
		this.l.setLevel(Level.ALL);
		FileHandler textLog = null;
		try {
			textLog = new FileHandler(this.logPath + this.logFile);
			textLog.setFormatter(new SimpleFormatter());

		} catch (SecurityException e) {
		} catch (IOException e) {
		}
		this.l.addHandler(textLog);
	}
}
