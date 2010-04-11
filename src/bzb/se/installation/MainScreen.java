package bzb.se.installation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URI;

import bzb.se.Paths;

import com4j.ClassFactory;
import com4j.IApplicationGE;
import com4j.AltitudeModeGE;

public class MainScreen implements Runnable {

	private ServerSocket ss;
	
	public boolean run;
	
	private IApplicationGE ge;

	private double gy = Config.START_GY; //180 -> -180
	private double gx = Config.START_GX; //90 -> -90
	private int alt = 0;
	private double ang = Config.START_ANGLE;
	private double vang = Config.HORIZON_ANGLE[0];

	private long lastUpdate = 0;

	private String currentContent = "";
	private String currentControl = "";
	
	public MainScreen () {
		this(Screens.PORT_MAIN);
	}
	
	public MainScreen (final int port) {
		try {
			ss = new ServerSocket(port);

			new Thread(new Runnable() {
				public void run () {
					ge = ClassFactory.createApplicationGE();
					ge.openKmlFile(Paths.ICONS_OVERLAY_URL, 0);
					resetGoogleEarth();
					System.out.println("Started main Google Earth installation display on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
				}
			}).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	ArrayList<OutputStream> dos = new ArrayList<OutputStream>();
	ArrayList<Socket> so = new ArrayList<Socket>();

	public void run() {
		new Thread(
			new Runnable() {
				public void run() {
					ArrayList secondaryScreens = Screens.getSecondaryScreens();
					Iterator i = secondaryScreens.iterator();
					while (i.hasNext()) {
						String ip;
						int port;
						try {
							ArrayList thisScreen = (ArrayList) i.next();
							ip = (String)thisScreen.get(0);
							port = Integer.parseInt((String)thisScreen.get(1));
							so.add(new Socket(ip, port));
							dos.add(so.get(so.size() - 1).getOutputStream());
							System.out.println("Connected to secondary Google Earth installation display on "+ ip + ":" + port);
						} catch (IOException e) {
							//e.printStackTrace();
							System.out.println("Couldn't connect to secondary display on " + ip + ":" + port);
						}
					}
				}
			}
		).start();

		run = true;
		while (run) {
			Socket s;
			try {
				s = ss.accept();
				s.setReceiveBufferSize(1000);
				InputStream is = s.getInputStream();
				int length = 0;
				byte[] b;
				String st;
				while (length != -1) {
					length = is.read();
					if (length == -1) {
						break;
					}
					b = new byte[length];
					is.read(b);
					st = new String(b);
					process(st);
				}
				
				if (is != null) {
					is.close();
				}
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			for (int i = 0; i < dos.size(); i++) {
				if (dos.get(i) != null) {
					dos.get(i).close();
				}
			}
			for (int i = 0; i < so.size(); i++) {
				if (so.get(i) != null) {
					so.get(i).close();
				}
			}
			if (ss != null) {
				ss.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double[] accelData = new double[2];
	private int i = 0;

	private long lastUpdated = 0;

	private void process (String command) {
		if (command.startsWith("z")) {
			if (command.equals("z0")) {
				zoomIn();
			} else if (command.equals("z1")) {
				zoomOut();
			}
			try {
				String message = "z" + alt;
				for (int i = 0; i < dos.size(); i++) {
					dos.get(i).write(message.length());
					dos.get(i).flush();
					dos.get(i).write(message.getBytes());
					dos.get(i).flush();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else if (command.startsWith("c")) {
			try {
				String message = "c" + gx + "," + gy + "," + alt;
				for (int i = 0; i < dos.size(); i++) {
					dos.get(i).write(message.length());
					dos.get(i).flush();
					dos.get(i).write(message.getBytes());
					dos.get(i).flush();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else if (command.startsWith("e")) {
			resetGoogleEarth();
			try {
				String message = "e";
				for (int i = 0; i < dos.size(); i++) {
					dos.get(i).write(message.length());
					dos.get(i).flush();
					dos.get(i).write(message.getBytes());
					dos.get(i).flush();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else if (command.startsWith("r")) {
			if (command.equals("r0")) {
				rotateLeft();
			} else if (command.equals("r1")) {
				rotateRight();
			}
		} else if (command.startsWith("h")) {
		
		} else if (System.currentTimeMillis() - lastUpdated > 1) {
			try {
				i = command.indexOf('.');
				accelData[0] = Double.parseDouble(command.substring(0, i));
				accelData[1] = Double.parseDouble(command.substring(i + 1, command.length()));
				updatePosition();
				updateGoogleEarth(Config.PAN_INSTANT);
				lastUpdated = System.currentTimeMillis();
			} catch (Exception e) {
				System.out.println("burp");
			}
		}
	}
	
	private double getAngle (double dx, double dy) {
		double dang = 0;
		if (dx >= 0 && dy >= 0) {
			dang = Math.toDegrees(Math.atan(dx / dy));
		} else if (dx >= 0 && dy <= 0) {
			dang = 90 + Math.toDegrees(-1 * Math.atan(dy / dx));
		} else if (dx <= 0 && dy <= 0) {
			dang = 270 - Math.toDegrees(Math.atan(dy / dx));
		} else if (dx <= 0 && dy >= 0) {
			dang = 360 + Math.toDegrees(Math.atan(dx / dy));
		}

		dang += ang;

		if (dang >= 360) {
			dang -= 360;
		} else if (dang < 0) {
			dang += 360;
		}

		return dang;
	}

	public void updatePosition () {

		try {
			double dx = accelData[0] / 60;
			double dy = accelData[1] / 60 * -1;
			double dz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			double dang = getAngle(dx,dy);

		if (dang < 90) {
			dx = dz * Math.sin(Math.toRadians(dang));
			dy = Math.sqrt(Math.pow(dz, 2) - Math.pow(dx, 2));
		} else if (dang < 180) {
			dy = -1* dz * Math.sin(Math.toRadians(dang - 90));
			dx = Math.sqrt(Math.pow(dz, 2) - Math.pow(dy, 2));			
		} else if (dang < 270) {
			dx = -1 * dz * Math.sin(Math.toRadians(dang - 180));
			dy = -1 * Math.sqrt(Math.pow(dz, 2) - Math.pow(dx, 2));
		} else {
			dy = dz * Math.sin(Math.toRadians(dang - 270));
			dx = -1 * Math.sqrt(Math.pow(dz, 2) - Math.pow(dy, 2));			
		}

		double delay = 0;
		if (lastUpdate == 0) {
			delay = 100;
		} else {
			delay = System.currentTimeMillis() - lastUpdate;
		}
		if (delay > 2000) {
			delay = 2000;
		}
		lastUpdate = System.currentTimeMillis();

		if (!(dx < 0.15 && dx > -0.15) ||
				!(dy < 0.15 && dy > -0.15)) {
			dx = delay * Config.SPEED_BASE * Config.ALT_LEVEL[alt]/Config.ALT_LEVEL[0] * dx * Config.SPEED_MODIFIER[alt];
			dy = delay * Config.SPEED_BASE * Config.ALT_LEVEL[alt]/Config.ALT_LEVEL[0] * dy * Config.SPEED_MODIFIER[alt];
			
			if ((gx + dx) > Config.BOUNDARY_LEFT[alt] && (gx + dx) < Config.BOUNDARY_RIGHT[alt] && (gy + dy) < Config.BOUNDARY_TOP[alt] && (gy + dy) > Config.BOUNDARY_BOTTOM[alt]) {
				gx += dx;
				if (gx > 180) {
					gx -= 360;
				} else if (gx < -180) {
					gx += 360;
				}
				
				gy += dy;
				if (gy > 90) {
					gy = 180 - gy;
				} else if (gy < -90) {
					gy = -180 - gy;
				}
			}
		}

		} catch (Exception e) {

		}


	}
	
	public void rotateLeft () {
		ang += 90;
		updateGoogleEarth(Config.PAN_SLOW);
	}

	public void rotateRight () {
		ang -= 90;
		updateGoogleEarth(Config.PAN_SLOW);
	}

	public void zoomIn () {
		if (alt < Config.ALT_LEVEL.length - 1) {
			alt++;
			if (!(gx > Config.BOUNDARY_LEFT[alt] && gx < Config.BOUNDARY_RIGHT[alt] && gy < Config.BOUNDARY_TOP[alt] && gy > Config.BOUNDARY_BOTTOM[alt])) {
				gy = Config.START_GY;
				gx = Config.START_GX;
			}
			updateGoogleEarth(Config.PAN_SLOW);
		}
	}
	
	public void zoomOut () {
		if (alt > 0) {
			alt--;
			if (!(gx > Config.BOUNDARY_LEFT[alt] && gx < Config.BOUNDARY_RIGHT[alt] && gy < Config.BOUNDARY_TOP[alt] && gy > Config.BOUNDARY_BOTTOM[alt])) {
				gy = Config.START_GY;
				gx = Config.START_GX;
			}
			updateGoogleEarth(Config.PAN_SLOW);
		}
	}
	
	public void updateGoogleEarth (double speed) {
		if (ge != null) {
			try {
				if (ang < 0) {
					ang += 360;
				} else if (ang >= 360) {
					ang -= 360;
				}
				ge.setCameraParams(gy, gx, 300.0, AltitudeModeGE.RelativeToGroundAltitudeGE, Config.ALT_LEVEL[alt], Config.HORIZON_ANGLE[alt], ang, speed);
			} catch (Exception e) {
				//System.out.println("update " + gy + " " + gx + " " + alt);
			}
		}
	}
	
	public void resetGoogleEarth () {
		gy = Config.START_GY;
		gx = Config.START_GX;
		alt = 0;
		ang = Config.START_ANGLE;
		vang = Config.HORIZON_ANGLE[0];

		updateGoogleEarth(Config.PAN_SLOW);
	}

}