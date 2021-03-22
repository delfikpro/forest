package forest;

import groovy.lang.Closure;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface ForestContext extends JavaContext {

	File getForestDir();

	File getInternalsDir();

	File getRealmDir();

	PortManager getPortManager();

	int getAssignedPort();

	void env(Map<String, Object> env);

	File resource(String name);

	String resourcePath(String name);

	void resourceCopy(String srcName);

	void resourceCopy(String srcName, String dstPath);

	String getRealmType();

	int getRealmId();

	String getRealmName();

	Map<String, Closure> getPresets();

	void load(Closure<?> preset);

	void delete(Object... objects);

	void preset(Map<String, Closure<?>> preset);

	To map(String... mappings);

	interface To {
		void to(String preset);
	}

	Map<String, List<String>> getMappings();

	void execute(String... command);

}
