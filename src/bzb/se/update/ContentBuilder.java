package bzb.se.update;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import bzb.se.Paths;
import bzb.se.installation.Meta;
import net.dapper.scrender.Scrender;

/*
 *	Looks up web content and creates screenshots
 */

public class ContentBuilder {

	public static void main(String[] args) {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(Paths.DB_XML_FILE_URL));
			// normalize text representation
			doc.getDocumentElement().normalize();
			
			NodeList markers = doc.getElementsByTagName("marker");
			int total = markers.getLength();
			System.out.println("Total markers: " + total);
			
			Meta.readConfig();
			DataOutputStream bigDos = new DataOutputStream(
					new FileOutputStream(new File(Paths.ICONS_OVERLAY_URL)));
			bigDos
					.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
							"<kml xmlns=\"http://earth.google.com/kml/2.2\">" +
								"<Document>" +
									"<name>" + Meta.getInstallationName() + "</name>"/* +
									"<Style id=\"sn_ylw-pushpin\" />" +
									"<Style id=\"transLine\" >" +
										"<LineStyle>" +
											"<width>0</width>" +
										"</LineStyle>" +
									"</Style>" +
									"<Placemark>" +
										"<name>Boundary</name>" +
										"<styleUrl>#transLine</styleUrl>" +
										"<LineString>" +
											"<extrude>0</extrude>" +
											"<tessellate>0</tessellate>" +
											"<altitudeMode>absolute</altitudeMode>" +
											"<coordinates>43.922795, 33.823958, 0" +
												"43.922795, 32.962490, 0" +
												"44.993794, 32.962490, 0" +
												"44.993794, 33.823958, 0" +
												"43.922795, 33.823958, 0" +
											"</coordinates>" +
										"</LineString>" +
									"</Placemark>"*/);

			for (int s = 0; s < markers.getLength(); s++) {

				Node firstNode = markers.item(s);
				if (firstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element firstElement = (Element) firstNode;
					if (firstElement.hasAttribute("markerSiteURL")) {
						String mediaURL = firstElement.getAttribute(
								"markerSiteURL").trim();
						if (mediaURL.length() > 0) {
							System.out.println("Media URL: " + mediaURL);

							try {
								Scrender scrender = new Scrender();
								scrender.init();
								String id = firstElement.getAttribute(
										"markerRecord").trim();
								scrender.render(mediaURL, new File(
										Paths.CONTENT_DIR + id + ".jpg"));

								File file = new File(Paths.CONTENT_DIR + id
										+ ".html");
								DataOutputStream dos = new DataOutputStream(
										new FileOutputStream(file));
								dos
										.writeUTF("<html><head><link href=\""
												+ Paths.CONTENT_STYLESHEET
												+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\" /></head><body><div id=\"centeredcontent\"><img src=\""
												+ id
												+ ".jpg\" /></div></body></html>");
								dos.close();
								
								bigDos.writeBytes("<Placemark>" +
										"<name>" + firstElement.getAttribute("markerTitle").trim() + "</name>" +
											//"<styleUrl>#sn_ylw-pushpin</styleUrl>" +
											"<Model>" +
												"<altitudeMode>relativeToGround</altitudeMode>" +
												"<Location>" +
													"<longitude>" + firstElement.getAttribute("markerLng").trim() + "</longitude>" +
													"<latitude>" + firstElement.getAttribute("markerLat").trim() + "</latitude>" +
													"<altitude>" + Meta.getAltitudeForLevel(Integer.parseInt(firstElement.getAttribute("markerElevation").trim())) + "</altitude>" +
												"</Location>" +
												"<Orientation>" +
													"<heading>0</heading>" +
													"<tilt>10</tilt>" +
													"<roll>0</roll>" +
												"</Orientation>" +
												"<Scale>" +
													"<x>" + Meta.getIconScaleForLevel(Integer.parseInt(firstElement.getAttribute("markerElevation").trim())) + "</x>" +
													"<y>" + Meta.getIconScaleForLevel(Integer.parseInt(firstElement.getAttribute("markerElevation").trim())) + "</y>" +
													"<z>" + Meta.getIconScaleForLevel(Integer.parseInt(firstElement.getAttribute("markerElevation").trim())) + "</z>" +
												"</Scale>" +
												"<Link>" +
													"<href>icons/waterdrop.dae</href>" +
												"</Link>" +
												"<ResourceMap>" +
													"<Alias>" +
														"<targetHref>texture0.jpg</targetHref>" +
														"<sourceHref>../images/texture0.jpg</sourceHref>" +
													"</Alias>" +
												"</ResourceMap>" +
											"</Model>" +
										"</Placemark>");
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							System.out.println("No media URL");
						}
					} else {
						System.out.println("No media URL");
					}
				}// end of if clause

			}// end of for loop with s var
				
			bigDos.writeBytes("</Document>" +
					"</kml>");
			bigDos.close();

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

}
