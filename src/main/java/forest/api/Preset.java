package forest.api;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Collection;
import java.util.function.Consumer;

public interface Preset {

	void using(String path);

	Resource resource(String path);

	void delete(String path);

	Collection<Preset> getPresets();

	void preset(Consumer<PresetContext> action);

	default void preset(@DelegatesTo(value = PresetContext.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		this.preset(Groovy.toConsumer(closure));
	}

	void map(String regexPattern, Preset preset);

}
