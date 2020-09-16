package forest;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Java implements Command {

	@Builder.Default
	private final String path = "java";

	private final Object initialHeap;
	private final Object maxHeap;
	private final List<String> classPath;
	private final String mainClass;
	private final List<String> arguments;

	@Override
	public String[] getUnixArgs() {
		List<String> args = new ArrayList<>();
		args.add(this.path);
		this.heapSize("-Xms", this.initialHeap, args);
		this.heapSize("-Xmx", this.maxHeap, args);
		args.add("-cp");
		args.add(String.join(System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ";", classPath));
		args.add(mainClass);
		if (this.arguments != null) args.addAll(arguments);
		return args.toArray(new String[0]);
	}

	private void heapSize(String argument, Object value, List<String> buffer) {
		if (value instanceof Number) buffer.add(argument + value.toString() + "M");
		else if (value instanceof String) buffer.add(argument + value);
		else if (value != null) System.out.println("Weird memory object: " + value);
	}

}
