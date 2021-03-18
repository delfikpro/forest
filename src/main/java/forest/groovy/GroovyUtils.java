package forest.groovy;

import groovy.lang.Closure;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroovyUtils {

	public static <T> T configure(Closure closure, T object) {
//		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(object);
		closure.call(object);
		return object;
	}


	public static String resolve(Object object) {
		if (object instanceof File) return ((File) object).getAbsolutePath();
		return String.valueOf(object);
	}

	public static List<String> resolveMany(Collection<Object> collection) {
		return collection.stream().map(GroovyUtils::resolve).collect(Collectors.toList());
	}

	public static Map<String, String> resolveMap(Map<?, ?> map) {
		Map<String, String> m = new HashMap<>();
		map.forEach((k, v) -> m.put(resolve(k), resolve(v)));
		return m;
	}

}
