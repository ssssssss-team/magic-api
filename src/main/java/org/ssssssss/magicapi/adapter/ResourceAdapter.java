package org.ssssssss.magicapi.adapter;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.PathUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public abstract class ResourceAdapter {

	private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private static final String SPRING_BOOT_CLASS_PATH = "BOOT-INF/classes/";

	public static Resource getResource(String location) throws IOException {
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
		return resolveResource(resource, false);
	}

	private static Resource resolveResource(org.springframework.core.io.Resource resource, boolean readonly) throws IOException {
		URL url = resource.getURI().toURL();
		if (url.getProtocol().equals(ResourceUtils.URL_PROTOCOL_JAR)) {
			JarURLConnection connection = (JarURLConnection) url.openConnection();
			boolean springBootClassPath = connection.getClass().getName().equals("org.springframework.boot.loader.jar.JarURLConnection");
			String entryName = (springBootClassPath ? SPRING_BOOT_CLASS_PATH : "") + connection.getEntryName();
			JarFile jarFile = connection.getJarFile();
			List<JarEntry> entries = jarFile.stream().filter(it -> it.getName().startsWith(entryName)).collect(Collectors.toList());
			return new JarResource(jarFile, entryName, entries, springBootClassPath);
		} else {
			return new FileResource(resource.getFile(), readonly);
		}
	}

	private static class FileResource implements Resource {

		private File file;

		private boolean readonly;

		public FileResource(File file, boolean readonly) {
			this.file = file;
			this.readonly = readonly;
		}

		@Override
		public boolean readonly() {
			return this.readonly;
		}

		@Override
		public boolean exists() {
			return this.file.exists();
		}

		@Override
		public boolean delete() {
			return !readonly() && IoUtils.delete(this.file);
		}

		@Override
		public boolean isDirectory() {
			return this.file.isDirectory();
		}

		@Override
		public boolean mkdir() {
			return !readonly() && this.file.mkdirs();
		}

		@Override
		public byte[] read() {
			return IoUtils.bytes(this.file);
		}

		@Override
		public boolean renameTo(Resource resource) {
			if (!this.readonly()) {
				File target = ((FileResource) resource).file;
				if (this.file.renameTo(target)) {
					this.file = target;
					return true;
				}
			}
			return false;
		}

		@Override
		public Resource getResource(String name) {
			return new FileResource(new File(this.file, name), this.readonly);
		}

		@Override
		public String name() {
			return this.file.getName();
		}

		@Override
		public List<Resource> resources() {
			File[] files = this.file.listFiles();
			return files == null ? Collections.emptyList() : Arrays.stream(files).map(it -> new FileResource(it, this.readonly)).collect(Collectors.toList());
		}

		@Override
		public Resource parent() {
			return new FileResource(this.file.getParentFile(), this.readonly);
		}

		@Override
		public List<Resource> dirs() {
			return IoUtils.dirs(this.file).stream().map(it -> new FileResource(it, this.readonly)).collect(Collectors.toList());
		}

		@Override
		public boolean write(byte[] bytes) {
			return !readonly() && IoUtils.write(this.file, bytes);
		}

		@Override
		public boolean write(String content) {
			return !readonly() && IoUtils.write(this.file, content);
		}

		@Override
		public List<Resource> files(String suffix) {
			return IoUtils.files(this.file, suffix).stream().map(it -> new FileResource(it, this.readonly)).collect(Collectors.toList());
		}

		@Override
		public String getAbsolutePath() {
			return this.file.getAbsolutePath();
		}

		@Override
		public String toString() {
			return String.format("file resource [%s]", this.file.getAbsolutePath());
		}
	}

	private static class JarResource implements Resource {

		private JarFile jarFile;

		private ZipEntry entry;

		private List<JarEntry> entries;

		private String entryName;

		private JarResource parent = this;

		private boolean inSpringBoot;

		public JarResource(JarFile jarFile, String entryName, List<JarEntry> entries, boolean inSpringBoot) {
			this.jarFile = jarFile;
			this.entryName = entryName;
			this.inSpringBoot = inSpringBoot;
			this.entry = getEntry(this.entryName);
			this.entries = entries;
		}

		public JarResource(JarFile jarFile, String entryName, List<JarEntry> entries, JarResource parent, boolean inSpringBoot) {
			this(jarFile, entryName, entries, inSpringBoot);
			this.parent = parent;
		}

		@Override
		public boolean readonly() {
			return true;
		}

		@Override
		public byte[] read() {
			try {
				return IoUtils.bytes(this.jarFile.getInputStream(entry));
			} catch (IOException e) {
				return new byte[0];
			}
		}

		@Override
		public boolean isDirectory() {
			return this.entry.isDirectory();
		}

		@Override
		public boolean exists() {
			return this.entry != null;
		}

		protected ZipEntry getEntry(String name) {
			if (inSpringBoot && name.startsWith(SPRING_BOOT_CLASS_PATH)) {
				name = name.substring(SPRING_BOOT_CLASS_PATH.length());
			}
			return this.jarFile.getEntry(name);
		}

		@Override
		public Resource getResource(String name) {
			String entryName = PathUtils.replaceSlash(this.entryName + "/" + name);
			String prefix = PathUtils.replaceSlash(entryName + "/");
			return new JarResource(this.jarFile, entryName, entries.stream()
					.filter(it -> it.getName().startsWith(prefix))
					.collect(Collectors.toList()), this, this.inSpringBoot);
		}

		@Override
		public String name() {
			int index = this.entryName.lastIndexOf("/");
			return index > -1 ? this.entryName.substring(index) : this.entryName;
		}

		@Override
		public Resource parent() {
			return this.parent;
		}

		@Override
		public List<Resource> dirs() {
			return resources().stream().filter(Resource::isDirectory).collect(Collectors.toList());
		}

		@Override
		public List<Resource> files(String suffix) {
			return this.entries.stream().filter(it -> it.getName().endsWith(suffix))
					.map(entry -> new JarResource(jarFile, entry.getName(), Collections.emptyList(), this.inSpringBoot))
					.collect(Collectors.toList());
		}

		@Override
		public List<Resource> resources() {
			String prefix = PathUtils.replaceSlash(this.entryName + "/");
			return entries.stream()
					.filter(it -> it.getName().startsWith(prefix))
					.map(entry -> new JarResource(jarFile, entry.getName(), entries.stream()
							.filter(item -> item.getName().startsWith(PathUtils.replaceSlash(entry.getName() + "/")))
							.collect(Collectors.toList()), this.inSpringBoot)
					)
					.collect(Collectors.toList());
		}

		@Override
		public String getAbsolutePath() {
			return this.jarFile.getName() + "/" + this.entryName;
		}

		@Override
		public String toString() {
			return String.format("class path resource [%s]", this.entryName);
		}
	}
}
