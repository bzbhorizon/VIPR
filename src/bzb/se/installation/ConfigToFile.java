package bzb.se.installation;


public abstract class ConfigToFile {
	
	public static final int HORIZON_ANGLE[] = new int[] {0,15,55};
	
	public static boolean WANDER_RESTRICT = false;
	public static final double WANDER_LIMIT[] = new double[] {4,1,0.1};

	public static final double START_GY = 33.314729;
	public static final double START_GX = 44.422256;
	public static final double START_ANGLE = 1;
	
	public static final double SPEED_BASE = 0.0025;
	public static final double SPEED_MODIFIER[] = new double[] {0.3,0.5,2.2};
	
	public static final double BOUNDARY_TOP[] = new double[] {START_GY + WANDER_LIMIT[0], START_GY + WANDER_LIMIT[1], START_GY + WANDER_LIMIT[2]};
	public static final double BOUNDARY_BOTTOM[] = new double[] {START_GY - WANDER_LIMIT[0], START_GY - WANDER_LIMIT[1], START_GY - WANDER_LIMIT[2]};
	public static final double BOUNDARY_LEFT[] = new double[] {START_GX - WANDER_LIMIT[0], START_GX - WANDER_LIMIT[1], START_GX - WANDER_LIMIT[2]};
	public static final double BOUNDARY_RIGHT[] = new double[] {START_GX + WANDER_LIMIT[0], START_GX + WANDER_LIMIT[1], START_GX + WANDER_LIMIT[2]};
	
	public static final double PAN_INSTANT = 5;
	public static final double PAN_SLOW = 0.75;
}
