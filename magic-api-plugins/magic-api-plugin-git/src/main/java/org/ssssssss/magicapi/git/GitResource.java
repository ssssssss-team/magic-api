package org.ssssssss.magicapi.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.ssssssss.magicapi.core.resource.FileResource;
import org.ssssssss.magicapi.core.resource.Resource;
import org.ssssssss.magicapi.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件存储实现
 *
 * @author mxd
 */
public class GitResource extends FileResource {
	private final GitRepo gitRepo;

	public static GitResource of(org.ssssssss.magicapi.core.config.Resource config, MagicGitProperties properties) throws IOException, GitAPIException {
		File file = new File(config.getLocation());
		GitRepo gitRepo = new GitRepo(file.getAbsolutePath(), properties);
		GitResource gitResource = new GitResource(config.isReadonly(), file,
				file.getAbsolutePath(), gitRepo);
		// 初始化
		gitResource.setup();
		return gitResource;
	}


	public GitResource(boolean readonly, File file, String rootPath, GitRepo gitRepo) {
		super(file, readonly, rootPath);
		this.gitRepo = gitRepo;
	}
	/**
	 * 进行初始化操作, 仅仅在项目启动时进行初始化
	 * @author soriee
	 * @date 2022/2/20 22:30
	 * @return
	 */
	private void setup() throws IOException, GitAPIException {
		synchronized(GitResource.class) {
			gitRepo.setupRepo();
		}
	}
	private boolean update(boolean update) {
		return gitRepo.update(update);
	}


	@Override
	public boolean delete() {
		return super.delete() && this.update(true);
	}

	@Override
	public boolean mkdir() {
		return super.mkdir() && this.update(false);
	}
	@Override
	public Resource getResource(String name) {
		return new GitResource(super.readonly(), new File(super.file, name), super.rootPath, this.gitRepo);
	}

	@Override
	public Resource getDirectory(String name) {
		return getResource(name);
	}
	@Override
	public boolean write(byte[] bytes) {
		return super.write(bytes)  && this.update(false);
	}

	@Override
	public boolean write(String content) {
		return super.write(content) && this.update(false);
	}

	@Override
	public List<Resource> resources() {
		File[] files = this.file.listFiles();
		return files == null ? Collections.emptyList() : Arrays.stream(files).map(it -> new GitResource(this.readonly(),
				it, this.rootPath, this.gitRepo)).collect(Collectors.toList());
	}
	@Override
	public Resource parent() {
		return this.rootPath.equals(this.file.getAbsolutePath()) ? null : new GitResource(this.readonly(),
				this.file.getParentFile(), this.rootPath, this.gitRepo);
	}
	@Override
	public List<Resource> dirs() {
		return IoUtils.dirs(this.file).stream().map(it -> new GitResource(this.readonly(),
				it, this.rootPath, this.gitRepo)).collect(Collectors.toList());
	}
	@Override
	public List<Resource> files(String suffix) {
		return IoUtils.files(this.file, suffix).stream().map(it -> new GitResource(this.readonly(),
				it, this.rootPath, this.gitRepo)).collect(Collectors.toList());
	}
	@Override
	public boolean renameTo(Resource resource) {
		if (!this.readonly()) {
			File target = ((GitResource) resource).file;
			if (this.file.renameTo(target)) {
				this.file = target;
				// 更新两次，新增文件和删除文件都要更新
				this.update(false);
				this.update(true);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.gitRepo.getProperties().getUrl();
	}
}
