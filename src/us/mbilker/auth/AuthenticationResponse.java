package us.mbilker.auth;

/**
 * Responses from the Authentication Server.
 */
public enum AuthenticationResponse {

	SUCCESS(""),
	BAD_RESPONSE("Bad response"),
	BAD_LOGIN("Bad login"),
	USER_NOT_PREMIUM("User not premium"),
	USER_EMAIL("Account migrated, use e-mail as username."),
	UNKNOWN("");
	private String message;

	private AuthenticationResponse(String message) {
		this.message = message ;
	}

	public String getMessage() {
		return message;
	}

	public static AuthenticationResponse getResponse(String message) {
		for (AuthenticationResponse response : values()) {
			if (response.getMessage().equals(message)) {
				return response;
			}
		}

		return UNKNOWN;
	}
}
