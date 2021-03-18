package forest.api;

import groovy.lang.Closure;

import java.util.function.Consumer;

public class Groovy {

	public static <T> Consumer<T> toConsumer(Closure<?> closure) {

		return parameter -> {
			closure.setDelegate(parameter);
			closure.setResolveStrategy(Closure.DELEGATE_FIRST);
			closure.run();
		};

	}

}
