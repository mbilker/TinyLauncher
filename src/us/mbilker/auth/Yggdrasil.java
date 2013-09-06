package us.mbilker.auth;

import java.util.UUID;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Deals with the new authentication system called...Yggdrasil.
 */
@SuppressWarnings({ "unchecked", "unused" })
public class Yggdrasil extends Credentials {

	public static final String AGENT_NAME = "TinyLauncher";
	public static final int AGENT_VERSION = 884;
	public static final String ENDPOINT_AUTHENTICATE = "authenticate";
	public static final String ENDPOINT_REFRESH = "refresh";
	public static final String ENDPOINT_VALIDATE = "validate";
	public static final String MOJANG_SERVER = "https://authserver.mojang.com/";
	private String accessToken;
	private String agentName = AGENT_NAME;
	private int agentVersion = AGENT_VERSION;
	private String clientToken = UUID.randomUUID().toString();
	private String realUsername;
	private String userId;

	public Yggdrasil() {
	}

	public Yggdrasil(Credentials credentials) {
		super(credentials.getUsername(), credentials.getPassword());
	}

	public Yggdrasil(String username, String password) {
		super(username, password);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getAgentName() {
		return agentName;
	}

	public int getAgentVersion() {
		return agentVersion;
	}

	public String getClientToken() {
		return clientToken;
	}

	public String getRealUsername() {
		return realUsername;
	}

	public String getUserId() {
		return userId;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public void setAgentVersion(int agentVersion) {
		this.agentVersion = agentVersion;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	public void setRealUsername(String realUsername) {
		this.realUsername = realUsername;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	private String createAuthenticationJSON() {
		JSONObject parent = new JSONObject();

		JSONObject agent = new JSONObject();
		agent.put("name", getAgentName());
		agent.put("version", getAgentVersion());
		parent.put("agent", agent);

		parent.put("username", getUsername());
		parent.put("password", getPassword());
		parent.put("clientToken", getClientToken());

		return parent.toJSONString();
	}

	private String createRefreshJSON() {
		JSONObject parent = new JSONObject();

		parent.put("accessToken", getAccessToken());
		parent.put("clientToken", getClientToken());

		JSONObject selectedProfile = new JSONObject();
		selectedProfile.put("id", getUserId());
		selectedProfile.put("name", getUsername());
		parent.put("selectedProfile", selectedProfile);

		return parent.toJSONString();
	}

	private String createValidationJSON() {
		JSONObject parent = new JSONObject();

		parent.put("accessToken", getAccessToken());

		return parent.toJSONString();
	}

	private void parseAuthenticationJSON(String response) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject parent = (JSONObject) parser.parse(response);
		setAccessToken((String) parent.get("accessToken"));

		JSONObject selectedProfile = (JSONObject) parent.get("selectedProfile");
		setUserId((String) selectedProfile.get("id"));
		setRealUsername((String) selectedProfile.get("name"));
	}

	private void parseErrorJSON(String response) throws ParseException {
		// Stand tall for the man next door!
	}
	
	private void parseRefreshJSON(String response) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject parent = (JSONObject) parser.parse(response);
		setAccessToken((String) parent.get("accessToken"));

		JSONObject selectedProfile = (JSONObject) parent.get("selectedProfile");
		setUserId((String) selectedProfile.get("id"));
		setRealUsername((String) selectedProfile.get("name"));
	}
}
