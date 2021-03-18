package forest;

public interface PortManager {

	int assignPort(String realmName);

	void portRangeStart(int port);

}
