package forest;

import lombok.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ForestPortManager implements PortManager {

	private final ForestContext forest;

	private int portRangeStart = 17700;

	@SneakyThrows
	public int assignPort(String realmName) {

		File ports = new File(forest.getInternalsDir(), "ports");

		if (!ports.exists()) {
			ports.createNewFile();
			Files.write(ports.toPath(), Collections.singletonList(realmName));
			return portRangeStart;
		}
		List<String> lines = new ArrayList<>(Files.readAllLines(ports.toPath()));
		val iterator = lines.iterator();
		int port = portRangeStart;
		while (iterator.hasNext()) {
			if (iterator.next().equals(realmName)) return port;
			port++;
		}
		lines.add(realmName);
		Files.write(ports.toPath(), lines);
		return port;

	}

	@Override
	public void portRangeStart(int port) {
		this.portRangeStart = port;
	}

}
