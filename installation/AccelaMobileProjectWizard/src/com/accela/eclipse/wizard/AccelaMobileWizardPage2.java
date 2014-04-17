package com.accela.eclipse.wizard;

import java.util.ResourceBundle;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class AccelaMobileWizardPage2 extends WizardNewProjectCreationPage {
	public  ResourceBundle messageResourceBundle;	
	
	/**
	 * Constructor
	 */
	public AccelaMobileWizardPage2(IConfigurationElement wizardPageConfigurationElement) {
		super(wizardPageConfigurationElement.getAttribute("id"));	
		setTitle(wizardPageConfigurationElement.getAttribute("name"));
		setDescription(wizardPageConfigurationElement.getAttribute("description"));		
		this.messageResourceBundle = ResourceBundle.getBundle("./resources/localization");
		setInitialProjectName(wizardPageConfigurationElement.getAttribute("id"));
	}
	
}