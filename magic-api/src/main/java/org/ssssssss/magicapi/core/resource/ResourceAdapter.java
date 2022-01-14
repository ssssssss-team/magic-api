package org.ssssssss.magicapi.core.resource;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 资源适配器
 *
 * @author mxd
 */
public class ResourceAdapter {

	public static final String SPRING_BOOT_CLASS_PATH = "BOOT-INF/classes/";
	private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	public static Resource getResource(String location, boolean readonly) throws IOException {
		if (location == null) {
			return null;
		}
		org.springframework.core.io.Resource resource;
		if (location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			resource = resolver.getResource(location);
			if (resource.exists()) {
				return resolveResource(resource, true);
			} else {
				throw new FileNotFoundException(String.format("%s not found", resource.getDescription()));
			}
		} else {
			resource = resolver.getResource(location);
			if (!resource.exists()) {
				resource = resolver.getResource(ResourceUtils.FILE_URL_PREFIX + location);
			}
		}
		return resolveResource(resource, readonly);
	}

	private static Resource resolveResource(org.springframework.core.io.Resource resource, boolean readonly) throws IOException {
		URL url = resource.getURI().toURL();
		if (url.getProtocol().equals(ResourceUtils.URL_PROTOCOL_JAR)) {
			JarURLConnection connection = (JarURLConnection) url.openConnection();
			boolean springBootClassPath = "org.springframework.boot.loader.jar.JarURLConnection".equals(connection.getClass().getName());
			String entryName = (springBootClassPath ? SPRING_BOOT_CLASS_PATH : "") + connection.getEntryName();
			JarFile jarFile = connection.getJarFile();
			List<JarEntry> entries = jarFile.stream().filter(it -> it.getName().startsWith(entryName)).collect(Collectors.toList());
			if (entries.isEmpty()) {
				entries = jarFile.stream().filter(it -> it.getName().startsWith(connection.getEntryName())).collect(Collectors.toList());
				return new JarResource(jarFile, connection.getEntryName(), entries, springBootClassPath);
			}
			return new JarResource(jarFile, entryName, entries, springBootClassPath);
		} else {
			return new FileResource(resource.getFile(), readonly, resource.getFile().getAbsolutePath());
		}
	}

}
