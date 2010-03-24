package bzb.se.GoogleEarth;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URI;
import java.lang.ProcessBuilder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;

public class CommObjectSecondary implements Runnable {

	private ServerSocket ss;

	private AudioPlayer audio;
	
	public boolean run;
	
	private Desktop explorer;
	
	private final double contentLocations[][][] = new double[][][] {
		new double[][] {
			new double[] {48.161482, 30.169847},
			new double[] {43.622486, 35.695268},
			new double[] {44.061492, 31.441489},
			new double[] {41.480189, 34.444308},
			new double[] {47.286224, 35.179780}
		},
		new double[][] {
			new double[] {44.169586, 33.649314},
			new double[] {44.752070, 33.327688},
			new double[] {44.285909, 33.121396},
			new double[] {44.902668, 33.983368},
			new double[] {45.198297, 32.876403},
			new double[] {43.647702, 33.379970},
			new double[] {45.521535, 32.524551}
		},
		new double[][] {
			new double[] {44.419675, 33.313808},
			new double[] {44.431400, 33.304312},
			new double[] {44.416214, 33.336660},
			new double[] {44.354927, 33.361464},
			new double[] {44.396373, 33.295514},
			new double[] {44.398838, 33.302300},
			new double[] {44.372460, 33.343469},
			new double[] {44.419251, 33.303736},
			new double[] {44.411850, 33.320887},
			//new double[] {44.405685, 33.326352},
			new double[] {44.370979, 33.271873},
			new double[] {44.373461, 33.289011}
		}
	};
	private final String contentURLs[][] = new String[][]{
		new String[] {
			"1-1.html",
			"1-2.html",
			"1-3.html",
			"1-4.html",
			"1-5.html"
		},
		new String[] {
			"2-1.html",
			"2-2.html",
			"2-3.html",
			"2-4.html",
			"2-5.html",
			"2-6.html",
			"2-7.html"
		},
		new String[] {
			"3-1.html",
			"3-2.html",
			"3-3.html",
			"3-4.html",
			"3-5.html",
			"3-6.html",
			"3-7.html",
			"3-8.html",
			"3-9.html",
			//"3-10.html",
			"3-11.html",
			"3-12.html"
		}
	};

	private String currentContent = "";
	
	public CommObjectSecondary () {
		try {
			ss = new ServerSocket(IP.SECONDARY_PORT);
			audio = new AudioPlayer();
			
			new Thread(new Runnable() {
				public void run () {
					explorer = Desktop.getDesktop();
					resetContent();
					System.out.println("Started secondary Google Earth installation display on " + IP.SECONDARY_IP + ":" + IP.SECONDARY_PORT);
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		run = true;
		Socket s;
		try {
			s = ss.accept();
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
			if (ss != null) {
				ss.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double gx = 0, gy = 0;
	private int alt = 0;

	private void process (String command) {
		if (command.startsWith("c")) {
			try {
				int i = command.indexOf(',');
				gx = Double.parseDouble(command.substring(1, i));
				int j = command.indexOf(',', i + 1);
				gy = Double.parseDouble(command.substring(i + 1, j));
				alt = Integer.parseInt(command.substring(j + 1));

				int nearest = -1;
				double distance = Double.MAX_VALUE;
				double temp = 0;
				for (int x = 0; x < contentLocations[alt].length; x++) {
					temp = Math.pow(gx - contentLocations[alt][x][0],2) + Math.pow(gy - contentLocations[alt][x][1],2);
					if (temp < distance) {
						nearest = x;
						distance = temp;
					}
				}
				//if (distance < Config.wanderLimit[alt] / 6) {
					try {
						explorer.browse(new URI("file:///C:/rivers/build/res/" + contentURLs[alt][nearest]));
						ProcessBuilder pb = new ProcessBuilder("cmd","/C","start","C:/rivers/build/res/"  + contentURLs[alt][nearest]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				//}
		
			} catch (Exception e) {
				System.out.println("burp");
			}
		} else if (command.startsWith("e")) {
			resetContent();
		} else if (command.startsWith("z")) {
			alt = Integer.parseInt(command.substring(1, command.length()));
			audio.reset();
		}
	}
	
	public void resetContent () {
		try {
			explorer.browse(new URI("file:///C:/rivers/build/res/index.html"));
			ProcessBuilder pb = new ProcessBuilder("cmd","/C","start","C:/rivers/build/res/index.html");
			alt = 0;
			audio.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class AudioPlayer implements Runnable {

		private Player player;

		//private boolean playing = false;

		private String[] audio = new String[] {
			"build/res/1.mp3",
			"build/res/2.mp3",
			"build/res/3.mp3"
		};

		public AudioPlayer () {
			new Thread(this).start();
		}

		public void run () {
			while (true) {
				System.out.println("Starting " + audio[alt]);
				try {
			        	FileInputStream fis = new FileInputStream(audio[alt]);
				        BufferedInputStream bis = new BufferedInputStream(fis);
				        player = new Player(bis);
					player.play();
			        } catch (Exception e) {
			      		System.out.println("Problem playing file " + audio[alt]);
			     		System.out.println(e);
		        	}
			}
		}
/*
		public void play () {
		//	playing = true;
			new Thread(this).start();
		}

		public void stop () {
			System.out.println("Stopping " + audio[alt]);
			playing = false;
			if (player != null) {
				player.close(); 
				player = null;
			}
		}
*/
		public void reset () {
			//stop();
			//play();
			if (player != null) {
				player.close();
				player = null;
			}
		}

	}

}
