package org.ssssssss.magicapi.core.resource;

import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.PathUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Jar存储实现
 *
 * @author mxd
 */
public class JarResource implements Resource {

	private final JarFile jarFile;

	private final ZipEntry entry;

	private final List<JarEntry> entries;

	private final String entryName;

	private final boolean inSpringBoot;
	private final String rootName;
	private JarResource parent = null;

	public JarResource(JarFile jarFile, String entryName, List<JarEntry> entries, boolean inSpringBoot) {
		this.jarFile = jarFile;
		this.entryName = entryName;
		this.rootName = entryName;
		this.inSpringBoot = inSpringBoot;
		this.entry = getEntry(this.entryName);
		this.entries = entries;
	}

	public JarResource(JarFile jarFile, String entryName, List<JarEntry> entries, JarResource parent, boolean inSpringBoot) {
		this(jarFile, entryName, entries, inSpringBoot);
		this.parent = parent;
	}

	@Override
	public String separator() {
		return "/";
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
		if (inSpringBoot && name.startsWith(ResourceAdapter.SPRING_BOOT_CLASS_PATH)) {
			name = name.substring(ResourceAdapter.SPRING_BOOT_CLASS_PATH.length());
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
		String name = this.entryName;
		if (isDirectory()) {
			name = name.substring(0, name.length() - 1);
		}
		int index = name.lastIndexOf("/");
		return index > -1 ? name.substring(index + 1) : name;
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
				.map(entry -> new JarResource(jarFile, entry.getName(), Collections.emptyList(), this, this.inSpringBoot))
				.collect(Collectors.toList());
	}

	@Override
	public List<Resource> resources() {
		String prefix = PathUtils.replaceSlash(this.entryName + "/");
		return entries.stream()
				.filter(it -> it.getName().startsWith(prefix))
				.map(entry -> new JarResource(jarFile, entry.getName(), entries.stream()
						.filter(item -> item.getName().startsWith(PathUtils.replaceSlash(entry.getName() + "/")))
						.collect(Collectors.toList()), this, this.inSpringBoot)
				)
				.collect(Collectors.toList());
	}

	@Override
	public String getAbsolutePath() {
		return this.jarFile.getName() + "/" + this.entryName;
	}

	@Override
	public String toString() {
		return String.format("jar://%s", this.entryName);
	}

	@Override
	public String getFilePath() {
		JarResource root = this;
		while (root.parent != null) {
			root = root.parent;
		}
		String path = this.entryName.substring(root.rootName.length());
		return path.startsWith("/") ? path.substring(1) : path;
	}
}
