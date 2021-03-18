package forest.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ForestException extends RuntimeException {

	public ForestException(String s) {
		super(s);
	}

	public ForestException(String s, Throwable throwable) {
		super(s, throwable);
	}

}
