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

	// All setter except port and ip could be used to changes config without restarting the server
	public Logger getLogObject() {
		return l;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public int getPort() {
		return Integer.parseInt(this.port);
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	private void initConfig(String configFile) {
		File inputFile = new File(configFile);
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
