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

	@ConfComments("Should HeroPanel use the custom TPS Method? (Put true if the minecraft one has issues)")
	@ConfDefault.DefaultBoolean(false)
	boolean customTPSMethod();

	@ConfComments("Should HeroPanel enable color code support in console? (WARNING: this feauture isn't implemented, if you enable this you will get messages with § character)")
	@ConfDefault.DefaultBoolean(false)
	boolean colorSupport();

	@ConfComments("Check delay beetween requests by an ip (if it is lower than minDelayBeetweenRequests it will block the request).")
	@ConfDefault.DefaultLong(500)
	long minDelayBeetweenRequests();

	@ConfComments("When the cache should remove the object? If the time beetween the last request and now is bigger than minDelayForCacheToRemoveObject then remove the object.")
	@ConfDefault.DefaultLong(5000)
	long minDelayForCacheToRemoveObject();

}
