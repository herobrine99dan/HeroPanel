package herobrine99dan.heropanel;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;

@ConfHeader("Configure HeroPanel settings.")
public interface HeroPanelConfig {

	@ConfComments("Remember to setup the TOTP Key, go in console or join in your minecraft server and run /heropanel setup")
	@ConfDefault.DefaultString("SETUP-ME")
	String TOTPKey();

	@ConfComments("Should HeroPanel use Ngrok? (Put true if you are using Heroku or your phone to host)")
	@ConfDefault.DefaultBoolean(false)
	boolean ngrokCompatibility();

	@ConfComments("Should HeroPanel enable color code support in console? (WARNING: this feauture isn't implemented, if you enable this you will get messages with § character)")
	@ConfDefault.DefaultBoolean(false)
	boolean colorSupport();
	
	@ConfComments("What port should HeroPanel use? (use -1 to install the panel of the minecraft port)")
	@ConfDefault.DefaultInteger(-1)
	int portToUse();

	@ConfComments("Max Requests allowed for an IP in one second.")
	@ConfDefault.DefaultInteger(20)
	long maxRequestsPerSecondByIP();

}
