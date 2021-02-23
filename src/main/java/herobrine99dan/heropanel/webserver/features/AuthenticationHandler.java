package herobrine99dan.heropanel.webserver.features;

import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicReference;

public class AuthenticationHandler {
		
	private AtomicReference<String> authenticated = new AtomicReference<String>("");
	private final String realAccount;
	
	public AuthenticationHandler(String realAccount) {
		this.realAccount = realAccount;
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
		return isCodeCorrect(code, ip);
	}
	
	private boolean isCodeCorrect(String account, String ip) throws GeneralSecurityException {
		if(account.isEmpty()) {
			return false;
		}
		if(realAccount.equals(account)) {
			authenticated.set(ip);
			return true;
		}
		return false;
	}

	public String getRealAccount() {
		return realAccount;
	}

}
