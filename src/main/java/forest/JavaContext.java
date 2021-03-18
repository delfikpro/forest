package forest;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

public interface JavaContext {

	void java(@DelegatesTo (value = Java.class, strategy = Closure.DELEGATE_FIRST) Closure<?> config);

}
