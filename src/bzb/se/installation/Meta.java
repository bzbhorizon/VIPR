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

	public static final int PORT_HUB = 50000;
	public static final int PORT_SUB = 49999;
	
	private static Document config;
	private static String installationName;
	//private static int[] altitudeLevels;
	private static int levels;
	private static int maxAlt;
	private static int minAlt;
	private static double[] boundCoords = new double[4];
	private static String hubIp;
	private static int hubPort;
	private static ArrayList<ArrayList<String>> subScreens = new ArrayList<ArrayList<String>>();
	
	private static double[] startPoint = new double[3];
	private static boolean wanderRestrict = false;
	private static double[][][] contentLocations; 
	private static String[][] contentURLs;
	
	public static ArrayList<ArrayList<String>> getSubScreens() {
		return subScreens;
	}
	
	/*public static int[] getAltitudeLevels() {
		return altitudeLevels;
	}*/
	
	public static int getLevels() {
		return levels;
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
			/*NodeList levels = config.getElementsByTagName("level");
			altitudeLevels = new int[levels.getLength()];
			for (int i = 0; i < levels.getLength(); i++) {
				Node firstNode = levels.item(i);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element firstElement = (Element) firstNode;
					if (firstElement.hasAttribute("number")) {
						altitudeLevels[i] = Integer.parseInt(firstElement.getTextContent());
					}
				}
			}*/
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
						hubIp = ip;
					} else {
						System.out.println("No IP");
					}
				} else {
					System.out.println("No IP");
				}
				if (firstElement.hasAttribute("port")) {
					int port = Integer.parseInt(firstElement.getAttribute("port"));
					System.out.println("Port: " + port);
					hubPort = port;
				} else {
					System.out.println("No port");
				}
			}
			
			NodeList googleEarths = config.getElementsByTagName("googleEarth");
			Node googleEarth = googleEarths.item(0);
			if (googleEarth.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) googleEarth;
				if (firstElement.hasAttribute("wanderRestrict")) {
					wanderRestrict = Boolean.parseBoolean(firstElement.getAttribute("wanderRestrict").trim());
				}
				if (firstElement.hasAttribute("levels")) {
					levels = Integer.parseInt(firstElement.getAttribute("levels").trim());
				}
				if (firstElement.hasAttribute("maxAlt")) {
					maxAlt = Integer.parseInt(firstElement.getAttribute("maxAlt").trim());
				}
				if (firstElement.hasAttribute("minAlt")) {
					minAlt = Integer.parseInt(firstElement.getAttribute("minAlt").trim());
				}
			}
			
			NodeList starts = config.getElementsByTagName("start");
			Node googleEarthStart = starts.item(0);
			if (googleEarthStart.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) googleEarthStart;
				if (firstElement.hasAttribute("x")) {
					startPoint[0] = Double.parseDouble(firstElement.getAttribute("x").trim());
				}
				if (firstElement.hasAttribute("y")) {
					startPoint[1] = Double.parseDouble(firstElement.getAttribute("y").trim());
				}
				if (firstElement.hasAttribute("angle")) {
					startPoint[2] = Double.parseDouble(firstElement.getAttribute("angle").trim());
				}
			}
			
			NodeList boundary = config.getElementsByTagName("boundary");
			Node bound = boundary.item(0);
			if (bound.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) bound;
				if (firstElement.hasAttribute("xLeft")) {
					boundCoords[BOUNDARY_LEFT] = Double.parseDouble(firstElement.getAttribute("xLeft").trim());
				}
				if (firstElement.hasAttribute("xRight")) {
					boundCoords[BOUNDARY_RIGHT] = Double.parseDouble(firstElement.getAttribute("xRight").trim());
				}
				if (firstElement.hasAttribute("yTop")) {
					boundCoords[BOUNDARY_TOP] = Double.parseDouble(firstElement.getAttribute("yTop").trim());
				}
				if (firstElement.hasAttribute("yBottom")) {
					boundCoords[BOUNDARY_BOTTOM] = Double.parseDouble(firstElement.getAttribute("yBottom").trim());
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
						subScreens.add(thisScreen);
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
	
	private static int[] contentAmounts;
	
	public static void readContent () {
		contentAmounts = new int[getLevels()];
		contentLocations = new double[contentAmounts.length][][];
		contentURLs = new String[contentAmounts.length][];
		
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(Paths.DB_XML_FILE_URL));
			// normalize text representation
			doc.getDocumentElement().normalize();
			
			NodeList markers = doc.getElementsByTagName("marker");
			for (int s = 0; s < markers.getLength(); s++) {
				Node firstNode = markers.item(s);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element firstElement = (Element) firstNode;
					contentAmounts[Integer.parseInt(firstElement.getAttribute("markerElevation").trim())]++;
				}
			}
			
			for (int s = 0; s < contentAmounts.length; s++) {
				contentLocations[s] = new double[contentAmounts[s]][3];
				contentURLs[s] = new String[contentAmounts[s]];
				contentAmounts[s] = 0;
			}
			
			for (int s = 0; s < markers.getLength(); s++) {
				Node firstNode = markers.item(s);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element firstElement = (Element) firstNode;
					contentLocations[Integer.parseInt(firstElement.getAttribute("markerElevation").trim())]
					                 [contentAmounts[Integer.parseInt(firstElement.getAttribute("markerElevation").trim())]]
					                  = new double[]{Double.parseDouble(firstElement.getAttribute("markerLat")), Double.parseDouble(firstElement.getAttribute("markerLng"))};
					contentURLs[Integer.parseInt(firstElement.getAttribute("markerElevation").trim())]
					            [contentAmounts[Integer.parseInt(firstElement.getAttribute("markerElevation").trim())]++]
					             = firstElement.getAttribute("markerRecord") + ".html";
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
	
	public static double[][] getContentLocations (int altLevel) {
		return contentLocations[altLevel];
	}
	
	public static String getContent (int altLevel, int number) {
		return contentURLs[altLevel][number];
	}
	
	public static boolean wanderRestricted () {
		return wanderRestrict;
	}
	
	public static int getAltitudeForLevel (int level) {
		double dif = maxAlt - minAlt;
		int alt = maxAlt - (int)(dif / levels * level);
		//System.out.println("alt " + alt);
		return alt;
	}
	
	public static double[] getStart () {
		return startPoint;
	}
	
	public static int getHorizonAngle (int level) {
		int angle = 20 + (int)(70.0 * Math.pow(level, 2) / Math.pow(levels, 2));
		//System.out.println("angle " + angle);
		return angle;
	}
	
	public static final int BOUNDARY_TOP = 0;
	public static final int BOUNDARY_RIGHT = 1;
	public static final int BOUNDARY_BOTTOM = 2;
	public static final int BOUNDARY_LEFT = 3;
	
	public static double getBoundary (int side) {
		return boundCoords[side]; 
	}
	
	/*public static double getBoundary (int side, int altLevel) {
		switch (side) {
		case BOUNDARY_TOP:
			return getStart()[1] + getWanderLimitForLevel(altLevel); 
		case BOUNDARY_BOTTOM:
			return getStart()[1] - getWanderLimitForLevel(altLevel);
		case BOUNDARY_LEFT:
			return getStart()[0] - getWanderLimitForLevel(altLevel); 
		case BOUNDARY_RIGHT:
			return getStart()[0] + getWanderLimitForLevel(altLevel); 
		default:
			return 0;
		}
	}*/
	
	// to do
	/*public static double getWanderLimitForLevel (int altLevel) {
		return (double)altLevel;
	}*/
	
	public static int getIconScaleForLevel (int level) {
		int altitude = getAltitudeForLevel(level);
		int scale = (int)(altitude / 500.0); // a guess
		return scale;
	}

	public static boolean isHub(String ip, int port) {
		if (ip.equals(hubIp) && port == hubPort) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isSubScreen(String ip, int port) {
		for (ArrayList<String> thisScreen : subScreens) {
			if (thisScreen.get(0).equals(ip) && Integer.parseInt(thisScreen.get(1)) == port) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInConfig(String targetIp, int targetPort) {
		if(isHub(targetIp, targetPort) || isSubScreen(targetIp, targetPort)) {
			return true;
		} else {
			return false;
		}
	}

}
