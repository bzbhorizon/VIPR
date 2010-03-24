package bzb.se.GoogleEarth;


public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args[0] != null) {
			if (args[0].equals("1")) {
				new Thread(new CommObject()).start();
			} else if (args[0].equals("2")) {
				new Thread(new CommObjectSecondary()).start();
			}
		}
	}

}
