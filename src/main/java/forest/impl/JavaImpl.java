package forest.impl;

import forest.Executable;
import forest.Java;
import forest.api.ForestException;
import forest.groovy.GroovyUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class JavaImpl implements Java {

	private String javaPath = "java";
	private String mainClass;
	private Collection<Object> arguments = new ArrayList<>();
	private Collection<Object> jvmArgs = new ArrayList<>();
	private Collection<Object> classpath = new ArrayList<>();

	@Override
	public String[] getExecutionCommand() {
		List<String> args = new ArrayList<>();
		args.add(this.javaPath);

		if (mainClass == null) throw new ForestException("Java: No main class specified");

		if (this.jvmArgs != null) args.addAll(GroovyUtils.resolveMany(jvmArgs));

		if (!classpath.isEmpty()) {
			args.add("-cp");
			args.add(String.join(":", GroovyUtils.resolveMany(classpath)));
		}

		args.add(mainClass);

		args.addAll(GroovyUtils.resolveMany(arguments));

		return args.toArray(new String[0]);
	}

}
