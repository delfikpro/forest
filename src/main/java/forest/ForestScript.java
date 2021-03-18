package forest;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.Script;
import lombok.experimental.Delegate;

public abstract class ForestScript extends Script implements ForestContext {

	@Delegate(excludes = JavaContext.class)
	public final ForestContext context = App.getContext();

	public void java(@DelegatesTo (value = Java.class, strategy = Closure.DELEGATE_FIRST) Closure<?> config) {
		this.context.java(config);
	}

}
