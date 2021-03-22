package forest.impl;

import forest.*;
import forest.api.ForestException;
import forest.groovy.GroovyUtils;
import groovy.lang.Closure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public class ForestContextImpl implements ForestContext {

	private final String realmType;
	private final int realmId;
	private final PortManager portManager = new ForestPortManager(this);
	private final Map<String, Object> env = new HashMap<>();
	private final Map<String, Closure> presets = new HashMap<>();
	private final Map<String, List<String>> mappings = new HashMap<>();
	private final List<String> contentRoots;

	private final File forestDir;
	private final File internalsDir;
	private final File realmDir;

	public int getAssignedPort() {
		return portManager.assignPort(this.getRealmName());
	}

	public void java(Closure config) {
		execute(GroovyUtils.configure(config, new JavaImpl()));
	}

	public void execute(Executable executable) {

		String[] command = executable.getExecutionCommand();

		if (System.getenv("FOREST_DEBUG") != null) {
			System.out.println("Executing > " + String.join(" ", command));
			return;
		}

		try {
			ProcessBuilder builder = new ProcessBuilder(command).directory(realmDir);
			env.forEach((k, v) -> builder.environment().put(String.valueOf(k), String.valueOf(v)));

			builder.inheritIO();
			Process start = builder.start();
			start.waitFor();
		} catch (Throwable throwable) {
			System.out.println("An error occurred while running command: " + String.join(" ", command));
			throwable.printStackTrace();
			System.exit(0);
		}
	}

	public void env(Map<String, Object> env) {
		this.env.putAll(env);
	}

	public File resource(String name) {

		if (contentRoots.isEmpty()) throw new IllegalStateException("No content roots specified!");
		for (String contentRoot : contentRoots) {
			File file = new File(contentRoot, name);
			if (file.exists()) return file.getAbsoluteFile();
		}
		throw new ForestException("Unable to find resource '" + name + "'!");
	}

	public String resourcePath(String name) {
		return resource(name).getAbsolutePath();
	}

	public void resourceCopy(String source) {
		resourceCopy(source, "");
	}

	public void resourceCopy(String source, String destination) {
		File resource = resource(source);
		File dst = new File(realmDir, destination);
		copy(resource, dst);
	}

	public void copy(File src, File dst) {
		if (src.isDirectory()) {
			for (File file : src.listFiles()) {
				copy(file, new File(dst, file.getName()));
			}
		} else try {
			if (dst.isDirectory()) {
				dst = new File(dst, src.getName());
			}
			dst.getParentFile().mkdirs();
			Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getRealmName() {
		return realmType + (realmId < 0 ? "-TEST-" : "-") + realmId;
	}

	public void load(Closure<?> preset) {
		preset.call();
	}

	public void delete(Object... objects) {
		for (Object object : objects) {
			if (object instanceof File) {
				File file = (File) object;
				if (file.isDirectory()) {
					delete((Object[]) file.listFiles());
				}
				file.delete();
			} else {
				String path = ("/" + object.toString().replace("\\", "/"));
				if (path.contains("/../")) throw new ForestException("Refusing to delete from suspicious path: " + object);
				while (path.startsWith("/")) path = path.substring(1);
				delete(new File(realmDir, path));
			}
		}
	}

	public void preset(Map<String, Closure<?>> preset) {
		presets.putAll(preset);
	}

	@Override
	public To map(String... mappings) {
		return preset -> this.mappings.computeIfAbsent(preset, s -> new ArrayList<>()).addAll(Arrays.asList(mappings));
	}

	@Override
	public void execute(String... command) {
		if (command.length == 0) return;
		if (command.length == 1) {
			Pattern pattern = Pattern.compile("((?:(?:[^ \"\\\\]|\\\\.)*\"(?:[^\"\\\\]|\\\\.)*\"(?:[^ \"\\\\]|\\\\.)*)+|\\S+)");
			Matcher matcher = pattern.matcher(command[0]);
			List<String> args = new ArrayList<>();
			while (matcher.find()) {
				args.add(matcher.group(1));
			}
			execute(new SimpleExecutable(args.toArray(new String[0])));
		} else {
			execute(new SimpleExecutable(command));
		}
	}

}
