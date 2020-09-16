package forest;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

@SuppressWarnings ("rawtypes")
public class Bindings {

	public static class java {

		public void call(Closure closure) {
			Java.JavaBuilder builder = Java.builder();
			closure.setDelegate(builder);
			closure.call();
			Java java = builder.build();
			App.runCommand(java);
		}

	}

	public static class resourcePath {

		public String call(String relativePath) {
			File resource = App.getResource(relativePath);

			return resource.getAbsolutePath();
//			return relativePath.toUpperCase();
		}

	}

	public static class resourceCopy {

		public void call(String source) {
			this.call(source, source);
		}
		public void call(String source, String destination) {
			File resource = App.getResource(source);
			File dst = new File(App.workingDir, destination);
			copy(resource, dst);
		}

		public void copy(File src, File dst) {
			if (src.isDirectory()) {
				for (File file : src.listFiles()) {
					copy(file, new File(dst, file.getName()));
				}
			} else try {
				dst.getParentFile().mkdirs();
				Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
