/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.netbeans.actions;

import ca.weblite.codename1.netbeans.Installer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
        category = "Bugtracking",
        id = "ca.weblite.codename1.netbeans.actions.CleanAndBuildIOSLocally"
)
@ActionRegistration(
        displayName = "#CTL_CleanAndBuildIOSLocally"
)
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = -190),
    @ActionReference(path = "Loaders/folder/any/Actions", position = 10),
    @ActionReference(path="Projects/org-netbeans-modules-java-j2seproject/Actions",  position=800)
})

@Messages("CTL_CleanAndBuildIOSLocally=Clean and Build iOS Locally")
public final class CleanAndBuildIOSLocally extends AbstractAction implements ContextAwareAction, LookupListener  {

     private final Lookup lkp;
    private final Lookup.Result<Project> result;

    public CleanAndBuildIOSLocally() {
        this(Utilities.actionsGlobalContext());
    }
    public CleanAndBuildIOSLocally(Lookup lookup) {
        super(Bundle.CTL_CleanAndBuildIOSLocally());
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
                    NotifyDescriptor nd = new NotifyDescriptor(
                            "An Xcode Project Already exists in this location.  Would you like to delete it?", 
                            "Create Xcode Project?",
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            null,
                            NotifyDescriptor.YES_OPTION
                    );
                    if ( DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION ){
                        final FileObject buildScript = context.getProjectDirectory()
                                .getFileObject("nbproject")
                                .getFileObject("build-ios-local.xml");

                        ExecutorTask task = ActionUtils.runTarget(buildScript, new String[]{"clean-xcode-project"}, null);
                        

                    }
                } else {
                    final FileObject buildScript = context.getProjectDirectory()
                                .getFileObject("nbproject")
                                .getFileObject("build-ios-local.xml");

                        ExecutorTask task = ActionUtils.runTarget(buildScript, new String[]{"clean-xcode-project"}, null);
                        
                }
                

                        
                        

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new CleanAndBuildIOSLocally(lkp);
    }

   

    

   

    @Override
    public void resultChanged(LookupEvent le) {
        
        Collection<? extends Project> sel = result.allInstances();
        System.out.println(sel);
        if ( sel.size() > 0 ){
            Project p = sel.toArray(new Project[0])[0];
            FileObject projectDir = p.getProjectDirectory();
            FileObject cn1PropertiesFile = projectDir.getFileObject("codenameone_settings.properties");
            if ( cn1PropertiesFile != null ){
                
                super.setEnabled(true);
                return;
            }
        }
        
        super.setEnabled(false);
    }
}
