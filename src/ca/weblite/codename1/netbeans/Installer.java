/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.netbeans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author shannah
 */
public class Installer {
    public static final int version = 8;
    public static final String VERSION_KEY = "cn1-ios-local.version";
    public static final String PROPERTIES_PATH = "nbproject/project.properties";
    private final Project project;
    
    public Installer(Project proj){
        this.project = proj;
    }
    
    public void update() throws IOException{
        EditableProperties props = getEditableProperties(project,  PROPERTIES_PATH);
        String oldVersionStr = props.get(VERSION_KEY);
        int currentVersion = -1;
        if ( oldVersionStr != null ){
            try {
                currentVersion = Integer.parseInt(oldVersionStr);
            } catch ( NumberFormatException ex){
                
            }
        }
        if ( currentVersion < version){
            updateBuildScript();
            props.setProperty(VERSION_KEY, ""+version);
            storeEditableProperties(project, PROPERTIES_PATH, props);
        }
        
    }
    
    private void updateBuildScript() throws IOException{
        URL buildScript = this.getClass().getResource("build-ios-local.xml");
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        File nbproject = new File(projectDir, "nbproject");
        File buildScriptDest = new File(nbproject, "build-ios-local.xml");
        FileUtils.copyURLToFile(buildScript, buildScriptDest);
    }
    
    private  EditableProperties getEditableProperties(final Project prj,final  String propertiesPath)
        throws IOException {
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null;
                        ep = new EditableProperties(false);
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }

    private static void storeEditableProperties(final Project prj, final  String propertiesPath, final EditableProperties ep)
        throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo!=null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }
}
