package bzb.se.update;

import java.util.*;

class Record implements Comparable {

	private HashMap pairs;

	public static final String UID = "record";

	public Record () {
		pairs = new HashMap();
	}

	public void addData (String key, String data) {
		if (pairs.containsKey(key)) {
			System.out.println("Overwriting");
		}
		pairs.put(key, data);
	}

	public String getData (String key) throws Exception {
		if (pairs.containsKey(key)) {
			return (String) pairs.get(key);
		} else {
			throw new Exception();
		}
	}

	public Set getColumns () {
		return pairs.keySet();
	}

	public int compareTo (Object obj) {
		Record r = (Record) obj;
		try {
			System.out.println(getData(UID).compareTo(r.getData(UID)));
			return getData(UID).compareTo(r.getData(UID));
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean equals (Object obj) {
		Record r = (Record) obj;
		try {
			if (r.getData(UID).equals(getData(UID))) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
