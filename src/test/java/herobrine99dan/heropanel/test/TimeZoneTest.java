package herobrine99dan.heropanel.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeZoneTest {

	public static void main(String[] args) {
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zonedDateTime = ldt.atZone(ZoneId.of("Europe/Paris"));
		System.out.println(zonedDateTime.toInstant().toEpochMilli() / 100 + "  " + System.currentTimeMillis() / 100);
	}
}
