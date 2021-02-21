package herobrine99dan.heropanel.webserver.features;

import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicReference;

public class AuthenticationHandler {
		
	private AtomicReference<String> authenticated = new AtomicReference<String>("");
	private final String base32Secret;
	private AtomicReference<String> lastCodeUsed = new AtomicReference<String>("");
	
	public AuthenticationHandler(String totpKey) {
		this.base32Secret = totpKey;
	}
	
	public boolean isAuthenticated(String ip) {
		if(authenticated.get().isEmpty() || ip.isEmpty()) {
			return false;
		}
		return ip.equals(authenticated.get());
	}
	
	public boolean logoutHandler() {
		if(authenticated.get().isEmpty()) {
			return false;
		}
		authenticated.set("");
		return true;
	}
	
	public boolean loginHandler(String ip, String code) throws GeneralSecurityException {
		if(!authenticated.get().isEmpty()) {
			return false;
		}
		if(!code.matches("[0-9]+")) {
			return false;
		}
		return isCodeCorrect(code, ip);
	}
	
	private boolean isCodeCorrect(String code, String ip) throws GeneralSecurityException {
		if(code.isEmpty()) {
			return false;
		}
		if(base32Secret.isEmpty()) {
			return false;
		}
		String secret = Long.toString(TOTP.generateCurrentNumber(base32Secret));
		if(secret.equals(code) && !lastCodeUsed.get().equals(secret)) {
			authenticated.set(ip);
			lastCodeUsed.set(secret);
			return true;
		}
		return false;
	}

	public String getBase32Secret() {
		return base32Secret;
	}

}
