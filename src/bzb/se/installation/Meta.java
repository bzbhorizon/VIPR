package bzb.se.installation;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import bzb.se.Paths;

public abstract class Meta {

	public static final int PORT_MAIN = 50000;
	public static final int PORT_SECONDARY = 49999;
	
	private static Document readInstallationConfig() {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(
					Paths.INSTALLATION_CONFIG_URL));
			// normalize text representation
			doc.getDocumentElement().normalize();

			return doc;
		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());
		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static String getInstallationName () {
		Document doc = readInstallationConfig();
		if (doc.getDocumentElement().hasAttribute("name")) {
			return doc.getDocumentElement().getAttribute("name");
		} else {
			return null;
		}
	}
	
	public static int getNumberOfAltitudeLevels () {
		Document doc = readInstallationConfig();
		NodeList levels = doc.getElementsByTagName("level");
		return levels.getLength();
	}
	
	public static int getAltitudeForLevel (int level) {
		Document doc = readInstallationConfig();
		NodeList levels = doc.getElementsByTagName("level");
		level = levels.getLength() - level; // Darryn numbers from 4 (lowest) to 1 (highest); I number from 0 (lowest) upwards
		for (int s = 0; s < levels.getLength(); s++) {
			Node firstNode = levels.item(s);
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) firstNode;
				if (firstElement.hasAttribute("number") && Integer.parseInt(firstElement.getAttribute("number")) == level) {
					return Integer.parseInt(firstElement.getTextContent());
				}
			}
		}
		return 0;
	}
	
	public static int getIconScaleForLevel (int level) {
		int altitude = getAltitudeForLevel(level);
		int scale = (int)(Math.sqrt(altitude) / 20.0); // a guess
		return scale;
	}

	private static Node getMainScreen() {
		Document doc = readInstallationConfig();
		NodeList mainScreens = doc.getElementsByTagName("main");
		int total = mainScreens.getLength();
		System.out.println("Total main screens: " + total);
		return mainScreens.item(0);
	}

	public static String getMainScreenIP() {
		Node mainScreen = getMainScreen();
		if (mainScreen.getNodeType() == Node.ELEMENT_NODE) {
			Element firstElement = (Element) mainScreen;
			if (firstElement.hasAttribute("ip")) {
				String ip = firstElement.getAttribute("ip").trim();
				if (ip.length() > 0) {
					System.out.println("IP: " + ip);
					return ip;
				} else {
					System.out.println("No IP");
				}
			} else {
				System.out.println("No IP");
			}
		}
		return null;
	}

	public static int getMainScreenPort() {
		Node mainScreen = getMainScreen();
		if (mainScreen.getNodeType() == Node.ELEMENT_NODE) {
			Element firstElement = (Element) mainScreen;
			if (firstElement.hasAttribute("port")) {
				int port = Integer.parseInt(firstElement.getAttribute("port"));
				System.out.println("Port: " + port);
				return port;
			} else {
				System.out.println("No port");
			}
		}
		return -1;
	}

	public static boolean isMainScreen(String ip, int port) {
		if (ip.equals(getMainScreenIP()) && port == getMainScreenPort()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isScreen(String targetIp, int targetPort) {
		Document doc = readInstallationConfig();
		NodeList mainScreens = doc.getElementsByTagName("main");
		for (int s = 0; s < mainScreens.getLength(); s++) {
			Node firstNode = mainScreens.item(s);
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) firstNode;
				if (firstElement.hasAttribute("ip")
						&& firstElement.hasAttribute("port")) {
					String ip = firstElement.getAttribute("ip").trim();
					int port = Integer.parseInt(firstElement
							.getAttribute("port"));
					if (targetIp.equals(ip) && targetPort == port) {
						return true;
					}
				}
			}
		}

		NodeList secondaryScreens = doc.getElementsByTagName("secondary");
		for (int s = 0; s < secondaryScreens.getLength(); s++) {
			Node firstNode = secondaryScreens.item(s);
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) firstNode;
				if (firstElement.hasAttribute("ip")
						&& firstElement.hasAttribute("port")) {
					String ip = firstElement.getAttribute("ip").trim();
					int port = Integer.parseInt(firstElement
							.getAttribute("port"));
					if (targetIp.equals(ip) && targetPort == port) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public static ArrayList getSecondaryScreens () {
		ArrayList screens = new ArrayList();
		
		Document doc = readInstallationConfig();
		NodeList secondaryScreens = doc.getElementsByTagName("secondary");
		for (int s = 0; s < secondaryScreens.getLength(); s++) {
			Node firstNode = secondaryScreens.item(s);
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) firstNode;
				if (firstElement.hasAttribute("ip")
						&& firstElement.hasAttribute("port")) {
					ArrayList thisScreen = new ArrayList();
					thisScreen.add(firstElement.getAttribute("ip").trim());
					thisScreen.add(firstElement.getAttribute("port").trim());
					screens.add(thisScreen);
				}
			}
		}
		
		return screens;
	}

}
