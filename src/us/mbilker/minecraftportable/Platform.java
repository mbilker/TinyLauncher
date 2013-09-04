package us.mbilker.minecraftportable;

public enum Platform {
	LINUX(1),
	UNIX(1),
	SOLARIS(2),
	SUNOS(2),
	WINDOWS(3),
	MAC(5);
	
	private int platformId;
	
	private Platform(int number) {
		platformId = number;
	}
	
	public int getNumber() {
		return platformId;
	}

}
