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

public class Meta {

	public static final int PORT_MAIN = 50000;
	public static final int PORT_SECONDARY = 49999;
	
	private static Document config;
	private static String installationName;
	private static int[] altitudeLevels;
	private static String mainIp;
	private static int mainPort;
	private static ArrayList<ArrayList<String>> secondaryScreens = new ArrayList<ArrayList<String>>();
	
	public static ArrayList<ArrayList<String>> getSecondaryScreens() {
		return secondaryScreens;
	}
	
	public static int[] getAltitudeLevels() {
		return altitudeLevels;
	}

	public static String getInstallationName() {
		return installationName;
	}

	public static void readConfig () {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(
					Paths.INSTALLATION_CONFIG_URL));
			// normalize text representation
			doc.getDocumentElement().normalize();

			config = doc;
			if (config.getDocumentElement().hasAttribute("name")) {
				installationName = config.getDocumentElement().getAttribute("name");
			}
			NodeList levels = config.getElementsByTagName("level");
			altitudeLevels = new int[levels.getLength()];
			for (int i = 0; i < levels.getLength(); i++) {
				Node firstNode = levels.item(i);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element firstElement = (Element) firstNode;
					if (firstElement.hasAttribute("number")) {
						altitudeLevels[i] = Integer.parseInt(firstElement.getTextContent());
					}
				}
			}
			NodeList mainScreens = config.getElementsByTagName("main");
			int total = mainScreens.getLength();
			System.out.println("Total main screens: " + total);
			Node mainScreen = mainScreens.item(0);
			if (mainScreen.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) mainScreen;
				if (firstElement.hasAttribute("ip")) {
					String ip = firstElement.getAttribute("ip").trim();
					if (ip.length() > 0) {
						System.out.println("IP: " + ip);
						mainIp = ip;
					} else {
						System.out.println("No IP");
					}
				} else {
					System.out.println("No IP");
				}
				if (firstElement.hasAttribute("port")) {
					int port = Integer.parseInt(firstElement.getAttribute("port"));
					System.out.println("Port: " + port);
					mainPort = port;
				} else {
					System.out.println("No port");
				}
			}
			NodeList screens = config.getElementsByTagName("secondary");
			for (int s = 0; s < screens.getLength(); s++) {
				Node firstNode = screens.item(s);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element firstElement = (Element) firstNode;
					if (firstElement.hasAttribute("ip")
							&& firstElement.hasAttribute("port")) {
						ArrayList<String> thisScreen = new ArrayList<String>();
						thisScreen.add(firstElement.getAttribute("ip").trim());
						thisScreen.add(firstElement.getAttribute("port").trim());
						secondaryScreens.add(thisScreen);
					}
				}
			}
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
	}
				
	public static int getAltitudeForLevel (int level) {
		return altitudeLevels[level];
	}
	
	public static int getIconScaleForLevel (int level) {
		int altitude = getAltitudeForLevel(level);
		int scale = (int)(altitude / 500.0); // a guess
		return scale;
	}

	public static boolean isMainScreen(String ip, int port) {
		if (ip.equals(mainIp) && port == mainPort) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isSecondaryScreen(String ip, int port) {
		for (ArrayList<String> thisScreen : secondaryScreens) {
			if (thisScreen.get(0).equals(ip) && Integer.parseInt(thisScreen.get(1)) == port) {
				return true;
			}
		}
		return false;
	}

	public static boolean isScreen(String targetIp, int targetPort) {
		if(isMainScreen(targetIp, targetPort) || isSecondaryScreen(targetIp, targetPort)) {
			return true;
		} else {
			return false;
		}
	}

}
