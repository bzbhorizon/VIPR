package bzb.se.installation;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			String ip;
			try {
				ip = InetAddress.getLocalHost().getHostAddress();

				if (args[0].equals("1")) {
					if (args.length > 1) {
						if (Screens.isMainScreen(ip, Integer.valueOf(args[1]))) {
							new Thread(new MainScreen(Integer.valueOf(args[1]))).start();
						} else {
							System.out.println("blah");
						}
					} else {
						if (Screens.isMainScreen(ip, Screens.PORT_MAIN)) {
							new Thread(new MainScreen()).start();
						} else {
							System.out.println("blah");
						}
					}
				} else if (args[0].equals("2")) {
					if (args.length > 1) {
						if (Screens.isScreen(ip, Integer.valueOf(args[1]))) {
							if (!Screens.isMainScreen(ip, Integer.valueOf(args[1]))) {
								new Thread(new SecondaryScreen(Integer.valueOf(args[1]))).start();
							} else {
								new Thread(new MainScreen(Integer.valueOf(args[1]))).start();
							}
						} else {
							System.out.println("blah");
						}
					} else {
						if (Screens.isScreen(ip, Screens.PORT_SECONDARY)) {
							new Thread(new SecondaryScreen()).start();
						} else {
							System.out.println("blah");
						}
					}
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
