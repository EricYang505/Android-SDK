package com.accela.eclipse.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;

public class AccelaMobileWizard extends Wizard implements INewWizard, IExecutableExtension {
	private IProject project;
	private AccelaMobileWizardPage1 page1;
	private AccelaMobileWizardPage2 page2;
	private IConfigurationElement wizardPageConfigurationElement;
	private String templateAppName;
	private String productValue,companyValue,packageValue,appIdValue,appSecretValue,authHostValue,apiHostValue,androidVersionValue,moduleValue,porjectValue;

	/**
	 * Constructor.
	 */
	public AccelaMobileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page1 = new AccelaMobileWizardPage1(wizardPageConfigurationElement);
		page2 = new AccelaMobileWizardPage2(wizardPageConfigurationElement);
	
		addPage(page1);				
		addPage(page2);		
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard.
	 */
	public boolean performFinish() {
		
		// Get the values entered in wizard pages
		productValue = page1.productText.getText();
		companyValue = page1.companyText.getText();
		packageValue = page1.packageIdentifier.getText();
		appIdValue = page1.appIdText.getText();
		appSecretValue = page1.appSecretText.getText();
		authHostValue = page1.authHostText.getText();
		apiHostValue = page1.apiHostText.getText();
		androidVersionValue = page1.androidVersionCombo.getText();
		moduleValue  = page1.moduleCombo.getText();
		porjectValue = page2.getProjectName();
		
		// Get the project handle		
		project = page2.getProjectHandle();		
		
		URI projectURI = (!page2.useDefaults()) ? page2.getLocationURI() : null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription desc = workspace.newProjectDescription(project.getName());
		desc.setLocationURI(projectURI);
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					createProject(desc, project, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		
		return true;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Do nothing for now
	}
	
	/**
	 * Sets the initialization data for the wizard.
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.wizardPageConfigurationElement = config;
		templateAppName = wizardPageConfigurationElement.getAttribute("id");
	}
	
	/**
	 * Create template project structure.
	 */
	private void createProject(IProjectDescription description, IProject proj, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		monitor.beginTask("Creating project...", 2);
		// Create project folder.		
		proj.create(description,monitor);				
		// Copy files from template project included in the plug-in.
		String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			copyResourcesToDirectory(new JarFile(jarPath), "templates/" + templateAppName, project);
			updatePackageInJavaSrouce();  
			createAccelaProperties();	
			updateProperties();	     
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		monitor.setTaskName("Refreshing project...");		
		// Refresh the project resource in Eclipse.
		proj.open(IResource.BACKGROUND_REFRESH, null);				
		proj.close(null);		
		// Clean build the project(important).
		proj.open(null);		
		proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);		
		
		monitor.worked(1);
	}
	
	/**
	 * Copy the template project included in the plug-in Jar package to the newly created project's folder.
	 */
	private void copyResourcesToDirectory(JarFile fromJar, String jarDir, IContainer destFolder) throws IOException {
  		// Create sub directories under the root directory
		for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();			
			if (entry.getName().startsWith(jarDir + "/"))
			{
		      if (entry.isDirectory()) {  // Directory entry
		    	  File dest = new File(destFolder.getLocation() + "/" + entry.getName().substring(jarDir.length() + 1));
		    	  if (!dest.exists()) {
		    		  dest.mkdirs();
		    	  }		
		      } else {  // File entry
		        File dest = new File(destFolder.getLocation() + "/" + entry.getName().substring(jarDir.length() + 1));
		        FileOutputStream out = new FileOutputStream(dest);
		        InputStream in = fromJar.getInputStream(entry);	
		        int fileSize = in.available();
		        try {
		          byte[] buffer = new byte[fileSize];	
		          int s = 0;
		          while ((s = in.read(buffer)) > 0) {
		            out.write(buffer, 0, s);
		          }
		        } catch (IOException e) {
		        	MessageDialog.openError(getShell(), "File Error", "Failed to copy templage project file" + entry.getName() + " due to exception:\n" + e.getMessage());
		        } finally {
		          try {
		            in.close();
		          } catch (IOException ignored) {}
		          try {
		            out.close();
		          } catch (IOException ignored) {}
		        }
		      }
		    }
		}
		// Create the gen empty folder
		File genDir = new File(destFolder.getLocation() + "/gen");
		genDir.mkdirs();  	  	
	}	
	
	/**
	 * Create accela.properties file in the newly created project's folder.
	 */
	private void createAccelaProperties() throws IOException {
		String confContentStr = "# This file is automatically generated by AccelaMobile Project Wizard tool." + "\n" +  
								"# It stores the parameter values which you entered in AccelaMobile Project Wizard page when creating this project." + "\n" + 
								"# If you would like to update this file, please make sure the new parameter values are valid." + "\n" + 
								"product.name=" + productValue + "\n" + 
								"company.identifier=" + companyValue + "\n" + 
								"accela.app.id=" + appIdValue + "\n" + 
								"accela.app.secret=" + appSecretValue + "\n" + 
								"accela.auth.host=" + authHostValue + "\n" + 
								"accela.api.host=" + apiHostValue + "\n" + 
								"module=" + moduleValue + "\n" +
								"schema=am" + productValue.toLowerCase(); 
		
		File dest = new File(project.getLocation() + "/assets/accela.properties");
		// Create the file with the predefined text content.
		FileOutputStream out = new FileOutputStream(dest);
        InputStream in = new ByteArrayInputStream(confContentStr.getBytes("UTF-8"));	
        int fileSize = in.available();
        try {
          byte[] buffer = new byte[fileSize];	
          int s = 0;
          while ((s = in.read(buffer)) > 0) {
            out.write(buffer, 0, s);
          }
        } catch (IOException e) {
        	MessageDialog.openError(getShell(), "File Error", "Failed to create accela.properties file due to exception:\n" + e.getMessage());
	    } finally {
          try {
            in.close();
          } catch (IOException ignored) {}
          try {
            out.close();
          } catch (IOException ignored) {}
        }
	}
				
	/**
	 * Update the package in Java source files.
	 */
	private void updatePackageInJavaSrouce() throws IOException {
		// *********** Update package names in src directory *******************
		String originalSrcPath = "/src/company/product";		
		String newSrcPath = "/src/"  + Path.SEPARATOR + packageValue.replace(".", "/");
		// Clone the original src director with the new package name.
	    File originalSrcDir = new File(project.getLocation().toString() + originalSrcPath);	
	    File newSrcDir = new File(project.getLocation().toString() + newSrcPath);	  
	    File parentPath = newSrcDir.getParentFile();
	    if (!parentPath.exists()) parentPath.mkdirs();	    
	    originalSrcDir.renameTo(newSrcDir.getAbsoluteFile());	   		
	    // Update package names in src directory.
	    updatePackageNamesInPath(newSrcDir);
	    // Update package names in AndroidManifest.xml file.
	    File manifestFile = new File(project.getLocation().toString() + "/AndroidManifest.xml");
	    updatePackageNamesInPath(manifestFile);
	    // Remove the original src root directory.
	    deleteDirectory(new File(project.getLocation().toString() + "/src/company"));	
	}
	
	/**
	 * Update some property files such as AndroidManifest.xml, .project, strings.xml
	 */
	private void updateProperties() throws IOException {
		// Update the SDK version in project.properties file.
        File projectPropertyFile = new File(project.getLocation().toString() + Path.SEPARATOR + "project.properties");
        String oldTargetStr = "target=android-10";
        String newTargetStr = "target=" + androidVersionValue;
        replaceStringInFile(projectPropertyFile, oldTargetStr, newTargetStr);      
		// Update Android Min SDK version in AndroidManifest.xml file.
		File androidManifestFile = new File(project.getLocation().toString() + Path.SEPARATOR + "AndroidManifest.xml");
		String oldVersionStr = "uses-sdk android:minSdkVersion=\"10\"";
        String newVersionStr = "uses-sdk android:minSdkVersion=\"" + androidVersionValue.substring("android-".length()) + "\"";
        replaceStringInFile(androidManifestFile, oldVersionStr, newVersionStr);			
		// Update schema name in AndroidManifest.xml file.
		String oldSchemaName = "android:scheme=\"am" + templateAppName.toLowerCase() + "\"";
		String newSchemaName = "android:scheme=\"am" + productValue.toLowerCase() + "\"";			
	    replaceStringInFile(androidManifestFile, oldSchemaName, newSchemaName);
	    // Update project name in .project file.
	    File projectFile = new File(project.getLocation().toString() + Path.SEPARATOR + ".project");
        replaceStringInFile(projectFile, templateAppName, porjectValue);
        // Update project name in string.xml.
	    File stringsXmlFile = new File(project.getLocation().toString() + "/res/values/strings.xml");
        replaceStringInFile(stringsXmlFile, templateAppName, porjectValue);
	}
	
	/**
	 * Delete a directory together with its sub folders and files.
	 */
	private void deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    path.delete();
	  }
	
	/**
	 * Update package names in a path (directory or file).
	 */
	private void updatePackageNamesInPath(File path) throws IOException {
		String oldPackageName = "company.product";
		String newPackageName = packageValue;
	    if(path.isDirectory()) {  // For directory
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	        	 updatePackageNamesInPath(files[i]);	       
	         }
	         else {
	        	 replaceStringInFile(files[i], oldPackageName, newPackageName);  	 
	         }
	      }
	    } else {  // For file
	    	replaceStringInFile(path, oldPackageName, newPackageName);  
	    }	   
	  }
	
	/**
	 * Replace package names in a file's content.
	 */
	private void replaceStringInFile(File file, String keyword, String newString) throws IOException {
		
		// Read lines from the file and updated package placeholder.
   	 	BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
   	 	StringBuffer sb = new StringBuffer();
        String line = null;	             	           
        while((line = reader.readLine()) != null)
        {
       	 if (line.contains(keyword))
       	 {
       		 sb.append(line.replace(keyword, newString) + "\n");
       	 } else {
       		 sb.append(line + "\n");
       	 }
        }
        reader.close();	             
        // Write the updated content back to the file
        FileWriter writer = new FileWriter(file.getAbsoluteFile());	           
        writer.write(sb.toString());
        writer.close();	      
	}	
	
}

