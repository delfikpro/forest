package forest;

import java.util.Arrays;
import java.util.Collection;

public interface Java extends Executable {

	default void javaPath(String path) {
		setJavaPath(path);
	}

	void setJavaPath(String path);

	String getJavaPath();

	default void mainClass(String mainClass) { setMainClass(mainClass);}

	void setMainClass(String mainClass);

	String getMainClass();


	default void arguments(Object... arguments) {
		getArguments().addAll(Arrays.asList(arguments));
	}

	void setArguments(Collection<Object> arguments);

	Collection<Object> getArguments();


	default void jvmArgs(Object... arguments) {
		getJvmArgs().addAll(Arrays.asList(arguments));
	}

	void setJvmArgs(Collection<Object> arguments);

	Collection<Object> getJvmArgs();


	default void xmx(String xmx) {
		jvmArgs("-Xmx" + xmx);
	}

	default void xms(String xms) {
		jvmArgs("-Xms" + xms);
	}


	default void classpath(Object... arguments) {
		getClasspath().addAll(Arrays.asList(arguments));
	}

	void setClasspath(Collection<Object> arguments);

	Collection<Object> getClasspath();


}
