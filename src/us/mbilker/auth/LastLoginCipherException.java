package us.mbilker.auth;

/**
 * Occurs when there was an error during the creation of the cipher for
 * the lastlogin file.
 */
public class LastLoginCipherException extends Exception {

	private static final long serialVersionUID = 3263L;

	public LastLoginCipherException(String message, Throwable cause) {
		super(message, cause);
	}

	public LastLoginCipherException(String message) {
		super(message);
	}
}
