package forest;

import forest.api.ForestException;
import forest.impl.ForestContextImpl;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class App {

	public static final GroovyClassLoader classLoader;

	static {
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(ForestScript.class.getName());
		classLoader = new GroovyClassLoader(App.class.getClassLoader());
	}


	@Getter
	private static ForestContext context;

	@SneakyThrows
	public static void main(String[] args) {

		OptionParser parser = new OptionParser(true);

		OptionSpec<String> presetOption = parser.accepts("preset").withRequiredArg();
		OptionSpec<String> forestDirOption = parser.accepts("forestDir").withRequiredArg().defaultsTo(".");
		OptionSpec<String> realmDirOption = parser.accepts("realmDir").withRequiredArg().defaultsTo("realms");
		OptionSpec<String> configDirOption = parser.accepts("config").withRequiredArg();
		OptionSpec<String> rootOption = parser.accepts("dir").withRequiredArg();

		parser.accepts("help", "Print this message");

		String help = "\nUsage: forest [OPTION]... <REALM>\n\n" +
				" -c, --config <path>         Path to forest config scripts / directories\n" +
				" -f, --forestDir <path>      Home directory for forest (default: .)\n" +
				" -h, --help                  Print this message.\n" +
				" -p, --preset <name>         Specify preset name to use\n" +
				" -r, --realmDir <path>       Directory to store realms data (default: realms)\n" +
				" -d, --dir <path>            Add resource root\n";

		OptionSet options = parser.parse(args);

		List<?> objects = options.nonOptionArguments();

		if (options.has("help") || objects.isEmpty()) {
			System.out.println(help);
			return;
		}

		File forestDir = new File(options.valueOf(forestDirOption));
		List<String> config = options.has(configDirOption) ? options.valuesOf(configDirOption) : Collections.singletonList("presets");

		String realmArg = String.valueOf(objects.get(0));
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

		forestDir.mkdirs();

		List<String> roots = options.valuesOf(rootOption);

		String realmDirPath = options.valueOf(realmDirOption);
		File realmDir = realmDirPath.startsWith("/") ? new File(realmDirPath) : new File(forestDir, realmDirPath);

		File internalsDir = new File(forestDir, ".forest");
		internalsDir.mkdirs();
		context = new ForestContextImpl(realmType, realmId, roots, forestDir, internalsDir, realmDir);

		for (String configPath : config) {
			File file = configPath.startsWith("/") ? new File(configPath) : new File(forestDir, configPath);
			readConfigFrom(file);
		}

		Closure preset = null;
		if (options.has(presetOption)) {
			String presetAddress = options.valueOf(presetOption);
			preset = context.getPresets().get(presetAddress);
			if (preset == null) {
				System.out.println("Preset '" + presetAddress + "' doesn't exist. Available presets: " + String.join(", ", context.getPresets().keySet()));
				return;
			}
		} else {
			for (val entry : context.getMappings().entrySet()) {
				if (Pattern.compile(entry.getValue()).matcher(realmArg).matches()) {
					preset = context.getPresets().get(entry.getKey());
					if (preset == null) System.out.println("Unable to find mapped preset '" + entry.getKey() + "'");
					else {
						System.out.println("Using preset: " + entry.getKey());
						break;
					}
				}
			}
			if (preset == null) {
				System.out.println("Couldn't find preset for realm '" + realmArg + "'.\nPlease specify the --preset flag.");
				return;
			}
		}

		try {
			preset.call();
		} catch (ForestException ex) {
			System.out.println("Error: " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("An error occurred while attempting to execute forest preset:");
			ex.printStackTrace();
		}

	}

	private static void readConfigFrom(File configDir) {
		if (!configDir.isDirectory()) {
			readConfigFile(configDir);
			return;
		}
		File[] files = configDir.listFiles();
		if (files == null || files.length == 0) return;
		Arrays.stream(files).sorted().forEach(file -> {
			if (file.isDirectory()) readConfigFrom(file);
			else readConfigFile(file);
		});
	}

	private static void readConfigFile(File file) {
		try {
			Class<?> calcClass = classLoader.parseClass("@groovy.transform.BaseScript forest.ForestScript FOREST_______\n" + String.join("\n", Files.readAllLines(file.toPath())));
			Script script = (Script) calcClass.newInstance();
			script.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
