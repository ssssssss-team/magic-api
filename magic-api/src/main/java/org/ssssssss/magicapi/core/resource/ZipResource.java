package org.ssssssss.magicapi.core.resource;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Zip 存储实现
 *
 * @author mxd
 */
public class ZipResource implements Resource {

	private final Map<String, byte[]> cachedContent;

	private String path = "";

	private Resource parent;

	public ZipResource(InputStream is) throws IOException {
		cachedContent = new TreeMap<>();
		try (ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
			ArchiveEntry entry;
			byte[] buf = new byte[4096];
			int len = -1;
			while ((entry = zis.getNextEntry()) != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				while ((len = zis.read(buf, 0, buf.length)) != -1) {
					os.write(buf, 0, len);
				}
				cachedContent.put(entry.getName(), os.toByteArray());
			}
		}
	}

	ZipResource(String name, Map<String, byte[]> cachedContent, Resource parent) {
		this.path = name;
		this.cachedContent = cachedContent;
		this.parent = parent;
	}

	@Override
	public boolean readonly() {
		return true;
	}

	@Override
	public boolean exists() {
		return this.cachedContent.containsKey(this.path);
	}

	@Override
	public byte[] read() {
		return cachedContent.getOrDefault(this.path, new byte[0]);
	}

	@Override
	public Resource getResource(String name) {
		return new ZipResource(this.path + name, this.cachedContent, this);
	}

	@Override
	public Resource getDirectory(String name) {
		return new ZipResource(this.path + name + "/", this.cachedContent, this);
	}

	@Override
	public boolean isDirectory() {
		return this.path.isEmpty() || this.path.endsWith("/");
	}

	@Override
	public String name() {
		String name = this.path;
		if (isDirectory()) {
			name = this.path.length() > 0 ? this.path.substring(0, name.length() - 1) : "";
		}
		int index = name.lastIndexOf("/");
		return index > -1 ? name.substring(index + 1) : name;
	}

	@Override
	public List<Resource> resources() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Resource parent() {
		return this.parent;
	}

	@Override
	public List<Resource> dirs() {
		int len = this.path.length();
		return this.cachedContent.keySet().stream()
				.filter(it -> it.endsWith("/") && it.startsWith(this.path) && it.indexOf("/", len + 1) == it.length() - 1)
				.map(it -> this.getDirectory(it.substring(len, it.length() - 1)))
				.collect(Collectors.toList());
	}


	@Override
	public List<Resource> files(String suffix) {
		if (isDirectory()) {
			int len = this.path.length();
			return this.cachedContent.keySet().stream()
					.filter(it -> it.startsWith(this.path) && it.endsWith(suffix) && it.indexOf("/", len) == -1)
					.map(it -> this.getResource(it.substring(len)))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public String getAbsolutePath() {
		return this.path;
	}

	@Override
	public String getFilePath() {
		throw new UnsupportedOperationException();
	}
}
