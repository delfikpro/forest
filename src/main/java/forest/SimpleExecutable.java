package forest;

import lombok.Data;

@Data
public class SimpleExecutable implements Executable {

	private final String[] executionCommand;

}
