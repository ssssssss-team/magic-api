package org.ssssssss.magicapi.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.core.exception.MagicAPIException;

import java.io.File;
import java.io.IOException;

/**
 * git仓库
 *
 * @author soriee
 * @date 2022/2/20 22:48
 */
public class GitRepo {
    private static final Logger logger = LoggerFactory.getLogger(GitRepo.class);
    /**
     * 文件路径地址
     */
    private String rootPath;
    private String gitFilePath;
    private MagicGitProperties properties;
    private Git git;

    public GitRepo(String rootPath, MagicGitProperties properties) {
        this.rootPath = rootPath;
        this.gitFilePath = rootPath + File.separator + ".git";
        this.properties = properties;
    }

    private void valid() {
        File repoDir = new File(rootPath);
        File gitFile = new File(gitFilePath);
        // 如果文件夹不存在 则创建文件夹
        if (!repoDir.exists()) {
            repoDir.mkdirs();
        }
        if (!gitFile.exists() && repoDir.list().length > 0) {
            throw new MagicAPIException("初次项目启动时，请保持文件夹为空。");
        }
    }

    /**
     * 设置ssh秘钥或者账号密码
     *
     * @param transportCommand
     * @return
     * @author soriee
     * @date 2022/2/28 20:06
     */
    private void setSshOrCredentials(TransportCommand transportCommand) {
        if (this.getProperties().getPrivateKey() != null) {
            // ssh
            final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(GitRepo.this.getProperties().getPrivateKey());
                    return defaultJSch;
                }
            };
            transportCommand.setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                }
            });
        } else if (StringUtils.isNotBlank(properties.getUsername())
                && StringUtils.isNotBlank(properties.getPassword())) {
            // 账号密码
            transportCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    properties.getUsername(),
                    properties.getPassword()));
        }
    }

    /**
     * 项目设置仓库
     *
     * @return
     * @author soriee
     * @date 2022/2/24 20:43
     */
    public void setupRepo() throws IOException, GitAPIException {
        this.valid();
        File gitFile = new File(gitFilePath);
        try {
            if (gitFile.exists()) {
                // 项目存在，则打开为仓库, 并且强制更新一次
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repository = builder.create(gitFile);
                git = new Git(repository);
                // 更新两次，避免删除文件未更新
                this.update(false);
                this.update(true);
            } else {
                CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(properties.getUrl())
                        .setDirectory(new File(rootPath))
                        .setBranch(properties.getBranch());
                this.setSshOrCredentials(cloneCommand);
                git = cloneCommand.call();
            }
        } catch (IOException | GitAPIException e) {
            logger.error("初始化git仓库失败", e);
            throw e;
        }
    }

    /**
     * 更新
     * 1.git add .
     * 2.git commit -m "同步数据"
     * 3.git pull
     * 4.git push
     * @param update
     * @return
     * @author soriee
     * @date 2022/2/20 22:54
     */
    public boolean update(boolean update) {
        try {
            git.add().setUpdate(update).addFilepattern(".").call();
            git.commit().setMessage("同步数据").call();
            PullCommand pull = git.pull();
            this.setSshOrCredentials(pull);
            PullResult pullResult = pull.call();
            if (!pullResult.isSuccessful()) {
                throw new MagicAPIException("git更新失败, 请重试或尝试手动更新");
            }
            PushCommand pushCommand = git.push();
            this.setSshOrCredentials(pushCommand);
            pushCommand.call();
        } catch (GitAPIException e) {
            logger.error("git更新失败", e);
            throw new MagicAPIException("git更新失败, 请重试或尝试手动更新");
        }
        return true;
    }

    public MagicGitProperties getProperties() {
        return properties;
    }
}
