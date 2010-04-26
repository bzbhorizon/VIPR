package bzb.se.installation;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import bzb.se.Paths;

import javazoom.jl.player.Player;

public class SubScreen implements Runnable {

	private ServerSocket ss;

	private AudioPlayer audio;

	public boolean run;

	private Desktop explorer;

	public SubScreen () {
		this(Meta.PORT_SUB);
	}

	public SubScreen(final int port) {
		try {
			ss = new ServerSocket(port);
			audio = new AudioPlayer();

			new Thread(new Runnable() {
				public void run() {
					Meta.readContent();
					explorer = Desktop.getDesktop();
					resetContent();
					try {
						System.out
								.println("Started secondary Google Earth installation display on "
										+ InetAddress.getLocalHost().getHostAddress() + ":" + port);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
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

	private void process(String command) {
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
				for (int x = 0; x < Meta.getContentLocations(alt).length; x++) {
					temp = Math.pow(gx - Meta.getContentLocations(alt)[x][0], 2)
							+ Math.pow(gy - Meta.getContentLocations(alt)[x][1], 2);
					if (temp < distance) {
						nearest = x;
						distance = temp;
					}
				}
				if (!Meta.wanderRestricted()
						&& distance < Meta.getWanderLimitForLevel(alt)) {
					try {
						explorer
								.browse(new URI(new File(Paths.RES_DIR
										+ Meta.getContent(alt, nearest)).toURI()
										.toString()));
						new ProcessBuilder("cmd", "/C",
								"start", new File(Paths.RES_DIR
										+ Meta.getContent(alt, nearest)).toURI()
										.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

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

	public void resetContent() {
		try {
			explorer.browse(new URI(new File(Paths.CONTENT_INDEX_URL).toURI()
					.toString()));
			new ProcessBuilder("cmd", "/C", "start",
					new File(Paths.CONTENT_INDEX_URL).toURI().toString());
			alt = 0;
			audio.reset();
		} catch (Exception e) {
			System.out.println("There should be an index page for the browser at " + new File(Paths.CONTENT_INDEX_URL).toURI()
					.toString());
		}
	}

	public class AudioPlayer implements Runnable {

		private Player player;

		public AudioPlayer() {
			new Thread(this).start();
		}

		public void run() {
			while (true) {
				String audioFile = Paths.AUDIO_DIR + (alt+1) + ".mp3";
				System.out.println("Starting " + audioFile);
				try {
					FileInputStream fis = new FileInputStream(audioFile);
					BufferedInputStream bis = new BufferedInputStream(fis);
					player = new Player(bis);
					player.play();
				} catch (Exception e) {
					System.out.println("Problem playing file " + audioFile);
					System.out.println(e);
					break;
				}
			}
		}

		public void reset() {
			if (player != null) {
				player.close();
				player = null;
			}
		}

	}

}
