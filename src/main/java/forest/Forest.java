package forest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
public class Forest {


    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length == 0) {
            System.out.println("Usage: forest <realm>");
            return;
        }

        String realm = args[0];
        String[] ss = realm.replace("-TEST", "-").split("-", 2);
        String realmType = ss[0];
        int realmId = Integer.parseInt(ss[1]);

//        Yaml yaml = new Yaml();
        Map<String, Object> config = new HashMap<>();
        try (InputStream inputStream = new FileInputStream("forest.yml")) {
//            config = yaml.load(inputStream);
        }

        String javaPath = (String) config.get("java-path");
        Map<String, Object> cristalix = (Map<String, Object>) config.get("cristalix");
        String cristalixUrl = (String) cristalix.get("url");
        String cristalixPassword = (String) cristalix.get("password");

        Pattern pattern = Pattern.compile("([A-Za-z0-9_-]+)@([^:]+):(\\d+)(.*)");
        Matcher matcher = pattern.matcher(cristalixUrl);
        if (!matcher.find()) {
            System.out.println("Invalid cristalix url!");
            return;
        }
        String cristalixLogin = matcher.group(1);
        String cristalixHost = matcher.group(2);
        String cristalixPort = matcher.group(3);
        String cristalixPath = matcher.group(4);

        Map<String, Object> setup = (Map<String, Object>) config.get(realmType);
        if (setup == null) {
            System.out.println("Template '" + realmType + "' doesn't exist.");
            return;
        }


        int port = setup.containsKey("port") ? (int) setup.get("port") + realmId : -1;
        String xmx = (String) setup.get("max-memory");
        String xms = (String) setup.get("start-memory");
        String mainClass = (String) setup.get("main-class");
        List<String> discreteBushes = (List<String>) setup.get("discrete-bushes");
        List<String> providedBushes = (List<String>) setup.get("provided-bushes");
        List<String> flowers = (List<String>) setup.get("flowers");
        List<String> cleanupEntries = (List<String>) setup.get("cleanup");

        System.out.println("Starting berry " + realm + " on 0.0.0.0:" + port + "...");

        File bushesDir = new File("bush");
        File flowersDir = new File("flower");
        File workingDir = new File("berry/" + realm);
        if (!workingDir.isDirectory()) workingDir.mkdirs();

        if (discreteBushes != null) for (String bush : discreteBushes) {
            File from = new File(bushesDir, bush);
            File to = new File(workingDir, bush);
            if (!from.exists()) {
                System.out.println("Discrete bush '" + bush + "' doesn't exist.");
                return;
            }
            if (to.exists() && from.lastModified() <= to.lastModified() && from.length() == to.length()) continue;

            System.out.println("Updating discrete bush '" + to.getName() + "'...");
            to.getParentFile().mkdirs();
            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        if (flowers != null) for (String flower : flowers) {
            String[] s = flower.split(" -> ", 2);
            String fromPath = s[0];
            boolean bulk = false;
            if (fromPath.endsWith("*")) {
                fromPath = fromPath.substring(0, fromPath.length() - 1);
                bulk = true;
            }
            String toPath = s.length < 2 ? fromPath : s[1];

            File from = new File(flowersDir, fromPath);
            File to = new File(workingDir, toPath);
            if (bulk) {
                if (!from.isDirectory()) {
                    System.out.println(from.getAbsolutePath() + " is not a directory");
                    return;
                }
                for (File file : from.listFiles()) applyFlower(realm, port, file, new File(to, file.getName()));
            } else applyFlower(realm, port, from, to);
        }

        List<String> classPath = new ArrayList<>();
        for (String bush : providedBushes) {
            File file = new File(bushesDir, bush);
            if (!file.exists()) {
                System.out.println("Provided bush '" + bush + "' doesn't exist.");
                return;
            }
            classPath.add(file.getAbsolutePath());
        }

        ProcessBuilder builder = new ProcessBuilder(
                javaPath,
                "-Xms" + xms,
                "-Xmx" + xmx,
                "-cp",
                String.join(":", classPath),
                mainClass
        ).directory(workingDir);
        Map<String, String> environment = builder.environment();
        environment.put("TOWER_IP", cristalixHost);
        environment.put("TOWER_PATH", cristalixPath);
        environment.put("TOWER_PORT", cristalixPort);
        environment.put("TOWER_LOGIN", cristalixLogin);
        environment.put("TOWER_PASSWORD", cristalixPassword);
        environment.put("REALM_TYPE", realmType);
        environment.put("REALM_ID", realmId + "");

        builder.inheritIO();
        Process start = builder.start();
        start.waitFor();

		System.out.println("Exited with code " + start.exitValue() + ".");
		System.out.println("Removing temporary files...");
		if (cleanupEntries != null) {
			for (String cleanupEntry : cleanupEntries) {
				File file = new File(workingDir, cleanupEntry);
				System.out.println("Removing " + file + "...");
				delete(file);
			}
		}
    }

    private static void delete(File... files) {
		for (File file : files) {
			if (file.isDirectory()) delete(file.listFiles());
			else file.delete();
		}
	}

    private static void applyFlower(String realm, int port, File fromFile, File toFile) throws IOException {
    	if (fromFile.isDirectory()) {
			for (File file : fromFile.listFiles()) {
				applyFlower(realm, port, file, toFile);
			}
		}
        byte[] bytes = Files.readAllBytes(fromFile.toPath());
        String string = new String(bytes)
                .replace("<port>", port + "")
                .replace("<realm>", realm);
        toFile.getParentFile().mkdirs();
        Files.write(toFile.toPath(), string.getBytes());
    }


}
