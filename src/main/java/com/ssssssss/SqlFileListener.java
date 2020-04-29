package com.ssssssss;

import com.ssssssss.mapping.SqlMappingManager;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;

@Component
public class SqlFileListener implements FileAlterationListener {

    @Autowired
    private SqlMappingManager manager;

    @Value("${ssssssss.directory:/ssssssss}")
    private String directory;

    @PostConstruct
    private void init(){
        new Thread(()->{
            try {
                File file = new File(SqlFileListener.class.getResource(this.directory).getFile());
                SqlFileListener.this.directory = file.getPath();
                start(file.listFiles());
                FileAlterationObserver observer = new FileAlterationObserver(file);
                observer.addListener(SqlFileListener.this);
                new FileAlterationMonitor(50,observer).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    private void start(File[] files){
        if(files != null){
            for (File file : files) {
                if(file.isFile()){
                    manager.register(this.directory,file);
                }else{
                    start(file.listFiles());
                }
            }
        }
    }

    @Override
    public void onDirectoryCreate(File directory) {

    }

    @Override
    public void onDirectoryChange(File directory) {

    }

    @Override
    public void onDirectoryDelete(File directory) {

    }

    @Override
    public void onFileCreate(File file) {
        manager.register(this.directory,file);
    }

    @Override
    public void onFileChange(File file) {
        manager.register(this.directory,file);
    }

    @Override
    public void onFileDelete(File file) {
        manager.unregister(this.directory,file);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
}
