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
						if (Meta.isMainScreen(ip, Integer.valueOf(args[1]))) {
							new Thread(new Hub(Integer.valueOf(args[1]))).start();
						} else {
							System.out.println("blah");
						}
					} else {
						if (Meta.isMainScreen(ip, Meta.PORT_MAIN)) {
							new Thread(new Hub()).start();
						} else {
							System.out.println("blah");
						}
					}
				} else if (args[0].equals("2")) {
					if (args.length > 1) {
						if (Meta.isScreen(ip, Integer.valueOf(args[1]))) {
							if (!Meta.isMainScreen(ip, Integer.valueOf(args[1]))) {
								new Thread(new SubScreen(Integer.valueOf(args[1]))).start();
							} else {
								new Thread(new Hub(Integer.valueOf(args[1]))).start();
							}
						} else {
							System.out.println("blah");
						}
					} else {
						if (Meta.isScreen(ip, Meta.PORT_SECONDARY)) {
							new Thread(new SubScreen()).start();
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
