package bzb.se.installation;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Meta.readConfig();
		
		if (args.length > 0) {
			String ip;
			try {
				ip = InetAddress.getLocalHost().getHostAddress();

				if (args[0].equals("1")) {
					if (args.length > 1) {
						if (Meta.isHub(ip, Integer.valueOf(args[1]))) {
							new Thread(new Hub(Integer.valueOf(args[1]))).start();
						} else {
							System.out.println("Incorrect port specified: trying to start a hub on " + ip + " using default hub port " + Meta.PORT_HUB);
							if (Meta.isHub(ip, Meta.PORT_HUB)) {
								new Thread(new Hub()).start();
							} else {
								System.out.println("Trying to start a hub on " + ip + ":" + args[1] + " but no such hub declared in config file");
							}
						}
					} else {
						System.out.println("No port specified: trying to start a hub on " + ip + " using default hub port " + Meta.PORT_HUB);
						if (Meta.isHub(ip, Meta.PORT_HUB)) {
							new Thread(new Hub()).start();
						} else {
							System.out.println("Trying to start a hub on " + ip + ":" + Meta.PORT_HUB + " but no such hub declared in config file");
						}
					}
				} else if (args[0].equals("2")) {
					if (args.length > 1) {
						if (Meta.isInConfig(ip, Integer.valueOf(args[1]))) {
							if (!Meta.isHub(ip, Integer.valueOf(args[1]))) {
								new Thread(new SubScreen(Integer.valueOf(args[1]))).start();
							} else {
								System.out.println("This device is declared as a hub in the config file: trying to start a hub on " + ip + " using default hub port " + args[1]);
								new Thread(new Hub(Integer.valueOf(args[1]))).start();
							}
						} else {
							System.out.println("Trying to start a subscreen on " + ip + ":" + args[1] + " but no such subscreen (or hub) declared in config file");
						}
					} else {
						System.out.println("No port specified: trying to start a subscreen on " + ip + " using default subscreen port " + Meta.PORT_SUB);
						if (Meta.isInConfig(ip, Meta.PORT_SUB)) {
							new Thread(new SubScreen()).start();
						} else {
							System.out.println("Trying to start a subscreen on " + ip + ":" + Meta.PORT_SUB + " but no such subscreen declared in config file");
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
