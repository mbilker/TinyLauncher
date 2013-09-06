package us.mbilker.auth;

import javax.crypto.Cipher;

/**
 * This is a thin wrapper around the static variables in javax.crypto.Cipher to
 * ease the use of some methods.
 */
public enum LastLoginCipherMode {

	DECRYPT(Cipher.DECRYPT_MODE),
	ENCRYPT(Cipher.ENCRYPT_MODE),
	UNWRAP(Cipher.UNWRAP_MODE),
	WRAP(Cipher.WRAP_MODE);
	private int mode;

	private LastLoginCipherMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}
}
