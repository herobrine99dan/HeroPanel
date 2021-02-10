package herobrine99dan.heropanel.test;

public class StringTest {

	public static void main(String[] args) {
		String test = "Plugins (10): §aEssentials§f, §aFreeMCServerFixes§f, §aMultiWorld§f, §aPermissionsEx§f, §aPlayerSimulator§f, §aPlugMan§f, §aProtocolSupport§f, §aServerTest§f, §aspark§f, §aViaVersion";
		char[] array = test.toCharArray();
		String result = "";
		int counter = 0;
		for (int i = 0; i < array.length; i++) {
			char character = array[i];
			if (character == '§') {
				counter++;	
			} else if(counter > 0) {
				counter = 0;
			} else {
				result = result + character;
			}
		}
		System.out.println(result);
	}

}
