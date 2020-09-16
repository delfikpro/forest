package forest;

import groovy.lang.*;
import groovy.util.Expando;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings ({"rawtypes", "unchecked"})
public class App {

	public static final Binding binding = new Binding();
	public static final Expando presets = new Expando();
	public static final Expando mappings = new Expando();
	public static final GroovyClassLoader classLoader = new GroovyClassLoader(App.class.getClassLoader());
	public static final Expando environment = new Expando();
	public static final List<File> contentRoots = new ArrayList<>();
	public static int portRangeStart = 6000;
	public static String realmArg;
	public static File workingDir = new File(".");

	public static void main(String[] args) {

		OptionParser parser = new OptionParser();

		OptionSpec<String> presetOption = parser.accepts("preset").withRequiredArg();
		OptionSpec<String> configOption = parser.accepts("configDir").withRequiredArg();
		OptionSpec<String> dirOption = parser.accepts("workingDir").withRequiredArg();
		OptionSet options = parser.parse(args);

		if (options.has(dirOption)) workingDir = new File(options.valueOf(dirOption));

		List<?> objects = options.nonOptionArguments();
		if (objects.isEmpty()) {
			System.out.println("You must specify the realm address, for example:\n    forest BW-1");
			return;
		}
		realmArg = String.valueOf(objects.get(0));
		String realmString;
		int realmId;
		if (realmArg.contains("-TEST")) {
			realmString = realmArg.replace("-TEST", "");
			realmId = -1;
		} else {
			realmString = realmArg;
			realmId = 1;
		}
		String[] split = realmString.split("-");
		if (split.length != 2) {
			System.out.println("Invalid realm address: " + realmArg);
			return;
		}
		String realmType = split[0];
		try {
			realmId *= Integer.parseInt(split[1]);
		} catch (NumberFormatException ex) {
			System.out.println("Invalid realm id: " + split[1]);
		}

		workingDir = new File(workingDir, realmArg);
		workingDir.mkdirs();

		binding.setVariable("realmType", realmType);
		binding.setVariable("realmId", realmId);
		binding.setVariable("presets", presets);
		binding.setVariable("mappings", mappings);
		binding.setVariable("env", environment);
		binding.setVariable("contentRoots", contentRoots);
		binding.setVariable("java", new Bindings.java());
		binding.setVariable("resourcePath", new Bindings.resourcePath());
		binding.setVariable("resourceCopy", new Bindings.resourceCopy());
		binding.setVariable("requirePort", new Bindings.requirePort());
		binding.setVariable("portRangeStart", new Bindings.portRangeStart());

		File configDir = options.has(configOption) ? new File(options.valueOf(configOption)) : new File("config");

		readConfigDir(configDir);

		Closure preset = null;
		if (options.has(presetOption)) {
			String presetAddress = options.valueOf(presetOption);
			preset = (Closure) presets.getProperty(presetAddress);
			if (preset == null) {
				System.out.println("Preset '" + presetAddress + "' doesn't exist. Available presets: " + String.join(", ", presets.getProperties().keySet()));
				return;
			}
		}
		else {
			for (val entryObj : mappings.getProperties().entrySet()) {
				val entry = (Map.Entry) entryObj;
				if (realmArg.replaceAll(entry.getKey().toString(), "").isEmpty()) {
					preset = (Closure) entry.getValue();
					break;
				}
			}
			if (preset == null) {
				System.out.println("Couldn't find preset for realm '" + realmArg + "'.\nPlease specify 'forest --preset <...>'");
				return;
			}
		}

		try {
			preset.call();
		} catch (Exception ex) {
			System.out.println("An error occurred while attempting to execute forest preset:");
			ex.printStackTrace();
		}

	}

	public static void runCommand(Command command) {
		String[] unixArgs = command.getUnixArgs();
//		System.out.println(String.join(" ", unixArgs));
		try {
			ProcessBuilder builder = new ProcessBuilder(unixArgs).directory(workingDir);
			environment.getProperties().forEach((k, v) -> builder.environment().put(String.valueOf(k), String.valueOf(v)));

			builder.inheritIO();
			Process start = builder.start();
			start.waitFor();
		} catch (Throwable throwable) {
			System.out.println("An error occurred while running command: " + String.join(" ", unixArgs));
			throwable.printStackTrace();
			System.exit(0);
		}
	}

	@SneakyThrows
	public static int requirePort() {
		File ports = new File(workingDir.getParentFile(), "ports");
		if (!ports.exists()) {
			ports.createNewFile();
			Files.write(ports.toPath(), Arrays.asList(realmArg));
			return portRangeStart;
		}
		List<String> lines = new ArrayList<>(Files.readAllLines(ports.toPath()));
		val iterator = lines.iterator();
		int port = portRangeStart;
		while (iterator.hasNext()) {
			if (iterator.next().equals(realmArg)) return port;
			port++;
		}
		lines.add(realmArg);
		Files.write(ports.toPath(), lines);
		return port;
	}

	private static void readConfigDir(File configDir) {
		if (!configDir.isDirectory()) {
			readConfigFile(configDir);
			return;
		}
		File[] files = configDir.listFiles();
		if (files == null || files.length == 0) return;
		Arrays.stream(files).sorted().forEach(file -> {
			if (file.isDirectory()) readConfigDir(file);
			else readConfigFile(file);
		});
	}

	private static void readConfigFile(File file) {
		try {
			Class<?> calcClass = classLoader.parseClass(file);
			Script script = (Script) calcClass.newInstance();
			script.setBinding(binding);
			script.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getResource(String relativePath) {
		if (contentRoots.isEmpty()) throw new IllegalStateException("No content roots specified!");
		for (File contentRoot : contentRoots) {
			File file = new File(contentRoot, relativePath);
			if (file.exists()) return file;
		}
		throw new NoSuchElementException("Unable to find resource '" + relativePath + "'!");
	}

}
