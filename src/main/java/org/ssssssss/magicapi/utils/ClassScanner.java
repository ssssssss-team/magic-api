package org.ssssssss.magicapi.utils;

import sun.misc.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassScanner {

	public static List<String> scan() throws URISyntaxException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Set<String> classes = new HashSet<>();
		do{
			if(loader instanceof URLClassLoader){
				classes.addAll(scan(((URLClassLoader) loader).getURLs()));
			}
		}while ((loader = loader.getParent()) != null);
		classes.addAll(scan(Launcher.getBootstrapClassPath().getURLs()));
		return new ArrayList<>(classes);
	}

	private static Set<String> scan(URL[] urls) throws URISyntaxException {
		Set<String> classes = new HashSet<>();
		if(urls != null){
			for (URL url : urls) {
				System.out.println(url);
				String protocol = url.getProtocol();
				if("file".equalsIgnoreCase(protocol)){
					String path = url.getPath();
					if (path.toLowerCase().endsWith(".jar")) {
						classes.addAll(scanJarFile(url));
					} else {
						classes.addAll(scanDirectory(new File(url.toURI()), null));
					}
				}else if("jar".equalsIgnoreCase(protocol)){
					classes.addAll(scanJarFile(url));
				}
			}
		}
		return classes;
	}

	private static List<String> scanDirectory(File dir, String packageName) {
		File[] files = dir.listFiles();
		List<String> classes = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				String name = file.getName();
				if (file.isDirectory()) {
					classes.addAll(scanDirectory(file, packageName == null ? name : packageName + "." + name));
				} else if (name.endsWith(".class") && !name.contains("$")) {
					classes.add(filterFullName(packageName + "." + name.substring(0, name.length() - 6)));
				}
			}
		}
		return classes;
	}

	private static String filterFullName(String fullName){
		if(fullName.startsWith("BOOT-INF.classes.")){
			fullName = fullName.substring(17);
		}
		return fullName;
	}

	private static List<String> scanJarFile(URL url) {
		List<String> classes = new ArrayList<>();
		try(ZipInputStream zis = new ZipInputStream(url.openStream())){
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.getName().contains("META-INF")) {
					String className = entry.getName();
					if (className.endsWith(".class") && !className.contains("$")) {
						classes.add(filterFullName(className.substring(0, className.length() - 6).replace("/", ".")));
					}
				}
			}
		}catch (IOException ignored){

		}
		return classes;
	}
}
