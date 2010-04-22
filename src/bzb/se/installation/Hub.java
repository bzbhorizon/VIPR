package bzb.se.installation;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import bzb.se.Paths;

public class Hub implements Runnable {

	private ServerSocket ss;
	
	public boolean run;
	
	private Dispatch googleEarth;
	
	private double gy = Meta.getStart()[1]; //180 -> -180
	private double gx = Meta.getStart()[0]; //90 -> -90
	private int alt = 0;
	private double ang = Meta.getStart()[2];
	
	private static final double PAN_INSTANT = 5;
	private static final double PAN_SLOW = 0.75;
	private static final double SPEED_BASE = 0.0025;

	private long lastUpdate = 0;
	
	public Hub () {
		this(Meta.PORT_MAIN);
	}
	
	public Hub (final int port) {
		try {
			ss = new ServerSocket(port);

			new Thread(new Runnable() {
				public void run () {
					googleEarth = new Dispatch("GoogleEarth.ApplicationGE"); 
					Dispatch.call(googleEarth,"OpenKmlFile",new Variant(new File(Paths.ICONS_OVERLAY_URL).getAbsolutePath()),new Variant(true)); 
					
					resetGoogleEarth();
					try {
						System.out.println("Started main Google Earth installation display on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
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
					Iterator<ArrayList<String>> i = Meta.getSecondaryScreens().iterator();
					while (i.hasNext()) {
						String ip = "";
						int port = -1;
						try {
							ArrayList<String> thisScreen = i.next();
							ip = thisScreen.get(0);
							port = Integer.parseInt(thisScreen.get(1));
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
				
				BufferedInputStream bis = new BufferedInputStream(is);
			    //ByteArrayOutputStream buf;
			    int result = bis.read();
			    StringBuffer sb = new StringBuffer();
			    while(result != -1) {
			    	//buf = new ByteArrayOutputStream();
			    	byte b = (byte)result;
			    	//buf.write(b);
			    	if ((char)b == '/') {
				    	new Thread(new ProcessThread(sb.toString())).start();
				    	sb = new StringBuffer();
			    	} else {
			    		sb.append((char)b);
			    	}
			    	result = bis.read();
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

	private long lastUpdated = 0;
	
	private class ProcessThread implements Runnable {
		
		private String command;
		
		public ProcessThread (String command) {
			this.command = command;
		}
		
		public void run () {
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
				String[] bits = command.split(",");
				try {
					accelData[0] = Double.parseDouble(bits[0]);
					accelData[1] = Double.parseDouble(bits[1]);
					updatePosition();
					updateGoogleEarth(PAN_INSTANT);
					lastUpdated = System.currentTimeMillis();
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			dx = delay * SPEED_BASE * Meta.getAltitudeForLevel(alt)/Meta.getAltitudeForLevel(0) * dx;
			dy = delay * SPEED_BASE * Meta.getAltitudeForLevel(alt)/Meta.getAltitudeForLevel(0) * dy;
			
			if ((gx + dx) > Meta.getBoundary(Meta.BOUNDARY_LEFT, alt) && (gx + dx) < Meta.getBoundary(Meta.BOUNDARY_RIGHT, alt) && (gy + dy) < Meta.getBoundary(Meta.BOUNDARY_TOP, alt) && (gy + dy) > Meta.getBoundary(Meta.BOUNDARY_BOTTOM, alt)) {
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
		updateGoogleEarth(PAN_SLOW);
	}

	public void rotateRight () {
		ang -= 90;
		updateGoogleEarth(PAN_SLOW);
	}

	public void zoomIn () {
		if (alt < Meta.getAltitudeLevels().length - 1) {
			alt++;
			if (!(gx > Meta.getBoundary(Meta.BOUNDARY_LEFT, alt) && gx < Meta.getBoundary(Meta.BOUNDARY_RIGHT, alt) && gy < Meta.getBoundary(Meta.BOUNDARY_TOP, alt) && gy > Meta.getBoundary(Meta.BOUNDARY_BOTTOM, alt))) {
				gy = Meta.getStart()[1];
				gx = Meta.getStart()[0];
			}
			updateGoogleEarth(PAN_SLOW);
		}
	}
	
	public void zoomOut () {
		if (alt > 0) {
			alt--;
			if (!(gx > Meta.getBoundary(Meta.BOUNDARY_LEFT, alt) && gx < Meta.getBoundary(Meta.BOUNDARY_RIGHT, alt) && gy < Meta.getBoundary(Meta.BOUNDARY_TOP, alt) && gy > Meta.getBoundary(Meta.BOUNDARY_BOTTOM, alt))) {
				gy = Meta.getStart()[1];
				gx = Meta.getStart()[0];
			}
			updateGoogleEarth(PAN_SLOW);
		}
	}
	
	public void updateGoogleEarth (double speed) {
		if (googleEarth != null) {
			if (ang < 0) {
				ang += 360;
			} else if (ang >= 360) {
				ang -= 360;
			}
			try {
				Dispatch.call(googleEarth,"SetCameraParams",new Variant(gy),new
					   Variant(gx),new Variant(Meta.getAltitudeForLevel(alt)),new Variant(1),new
					   Variant(300),new Variant(Meta.getHorizonAngle(alt)),new Variant(ang),new Variant(speed));
			} catch (Exception e) {
				System.out.println(gy + " " + gx + " " + Meta.getAltitudeForLevel(alt) + " " + Meta.getHorizonAngle(alt) + " " + ang + " " + speed);
			}
		}
	}
	
	public void resetGoogleEarth () {
		gy = Meta.getStart()[1];
		gx = Meta.getStart()[0];
		alt = 0;
		ang = Meta.getStart()[2];

		updateGoogleEarth(PAN_SLOW);
	}

}