package herobrine99dan.heropanel;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;

@ConfHeader("Configure HeroPanel settings.")
public interface HeroPanelConfig {
	@ConfComments("ngrokKey")
	@ConfDefault.DefaultString("SETUP-ME")
	String ngrokKey();
	
	@ConfComments("account")
	@ConfDefault.DefaultString("")
	String account();

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
