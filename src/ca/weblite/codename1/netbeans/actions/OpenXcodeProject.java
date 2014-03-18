/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.netbeans.actions;

import ca.weblite.codename1.netbeans.Installer;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

@ActionID(
        category = "Build",
        id = "ca.weblite.codename1.netbeans.actions.OpenXcodeProject"
)
@ActionRegistration(
        displayName = "#CTL_OpenXcodeProject"
)
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = -290),
    @ActionReference(path = "Loaders/folder/any/Actions", position = 10),
    @ActionReference(path="Projects/org-netbeans-modules-java-j2seproject/Actions",  position=800)
})

@Messages("CTL_OpenXcodeProject=Open Xcode Project")
public final class OpenXcodeProject extends AbstractAction implements ContextAwareAction, LookupListener {

     private final Lookup lkp;
    private final Lookup.Result<Project> result;

    public OpenXcodeProject() {
        this(Utilities.actionsGlobalContext());
    }

    public OpenXcodeProject(Lookup lookup) {
        super(Bundle.CTL_OpenXcodeProject());
        this.lkp = lookup;
        Project p = lkp.lookup(Project.class);
        super.setEnabled(false);
        if ( p != null ){
            FileObject projectDir = p.getProjectDirectory();
            FileObject cn1PropertiesFile = projectDir.getFileObject("codenameone_settings.properties");
            if ( cn1PropertiesFile != null ){
                System.out.println("Setting enabled");
                super.setEnabled(true);
               
            } 
        }
        Lookup.Template<Project> tpl = new Lookup.Template(Project.class);
        result = this.lkp.lookup(tpl);
        result.addLookupListener(
                WeakListeners.create(LookupListener.class, this, result)
        );
        
        
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         for ( Project p : result.allInstances()){
            ContextAction action = new ContextAction(p);
            action.actionPerformed(e);
        }
    }
    
    private static class ContextAction extends AbstractAction {

        private final Project context;
        
        ContextAction(Project context){
            this.context = context;
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                // TODO use context

                Installer installer = new Installer(context);
                installer.update();

                File buildDir = new File(FileUtil.toFile(context.getProjectDirectory()), "build");
                File iosDir = new File(buildDir, "ios");
                
                
                if ( iosDir.exists() ){
                    
                    final FileObject buildScript = context.getProjectDirectory()
                            .getFileObject("nbproject")
                            .getFileObject("build-ios-local.xml");

                    ExecutorTask task = ActionUtils.runTarget(buildScript, new String[]{"open-xcode-project"}, null);


                    
                }    

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new OpenXcodeProject(lkp);
    }

   

    

   

    @Override
    public void resultChanged(LookupEvent le) {
        try {
            Collection<? extends Project> sel = result.allInstances();
            System.out.println(sel);
            if ( sel.size() > 0 ){
                Project p = sel.toArray(new Project[0])[0];
                FileObject projectDir = p.getProjectDirectory();
                FileObject cn1PropertiesFile = projectDir.getFileObject("codenameone_settings.properties");
                FileObject ios = projectDir.getFileObject("build").getFileObject("ios");
                if ( cn1PropertiesFile != null && ios != null){

                    super.setEnabled(true);
                    return;
                }
            }
        } catch ( Throwable t){}
        
        super.setEnabled(false);
    }
}
