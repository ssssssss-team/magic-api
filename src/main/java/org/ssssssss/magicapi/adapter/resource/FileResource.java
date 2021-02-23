package org.ssssssss.magicapi.adapter.resource;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.utils.IoUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileResource implements Resource {

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
