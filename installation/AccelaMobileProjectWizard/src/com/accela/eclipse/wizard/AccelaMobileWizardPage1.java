package com.accela.eclipse.wizard;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AccelaMobileWizardPage1 extends WizardPage {
	private static String DEFAULT_AUTH_HOST = "https://auth.accela.com";
	private static String DEFAULT_API_HOST = "https://apis.accela.com";	
	public  ResourceBundle localizationResourceBundle;	
	public Text productText, companyText, appIdText, appSecretText, authHostText,apiHostText,packageIdentifier;
	public Combo moduleCombo, androidVersionCombo;
	private String[] androidVersions = {"android-10","android-11","android-12","android-13","android-14","android-15","android-16","android-17","android-18","android-19"};
	private IConfigurationElement wizardPageConfigurationElement;	
	
	/**
	 * Constructor.
	 */
	public AccelaMobileWizardPage1(IConfigurationElement wizardPageConfigurationElement) {
		super(wizardPageConfigurationElement.getAttribute("id"));		
		this.wizardPageConfigurationElement = wizardPageConfigurationElement;		
		// Set page title and description
		setTitle(wizardPageConfigurationElement.getAttribute("name"));
		setDescription(wizardPageConfigurationElement.getAttribute("description"));		
		
		// Get the resource bundle object for next use
		this.localizationResourceBundle = ResourceBundle.getBundle("./resources/localization");		
		
		// Disable the Next & Finish button by default
		setPageComplete(false);
	}

	/**
	 * Create the wizard page.
	 */
	public void createControl(Composite parent) {
		// Create layout for the wizard page
		Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        
        // Create Product Name (field label, text field, and placeholder label)
        Label productLabel = new Label(container, SWT.NULL);
        productLabel.setText(this.getLocalizationString("label_product_name"));
        productText = new Text(container, SWT.BORDER | SWT.SINGLE);
        productText.setText(wizardPageConfigurationElement.getAttribute("id"));  // Default value
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        productText.setLayoutData(gd);
        productText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	valueChanged(productText);            	
            }
        });
        Label placeholder4ProductRow = new Label(container, SWT.NULL);
        placeholder4ProductRow.setSize(50, 0);        
     // Create Company Identifier (field label, text field, and placeholder label)
        Label companyLabel = new Label(container, SWT.NULL);
        companyLabel.setText(this.getLocalizationString("label_company_identifier"));
        companyText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        companyText.setLayoutData(gd);
        companyText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	valueChanged(companyText);
            }
        });
        Label placeholder4CompanyRow = new Label(container, SWT.NULL);
        placeholder4CompanyRow.setSize(50, 0);       
        
     // Create Accela App ID (field label, text field, and placeholder label))
		Label appIdLabel = new Label(container, SWT.NULL);
		appIdLabel.setText(this.getLocalizationString("label_app_id"));
		appIdText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		appIdText.setLayoutData(gd);	
		appIdText.addModifyListener(new ModifyListener() {
	            public void modifyText(ModifyEvent e) {
	            	valueChanged(appIdText);
	            }
	        });       
		Label placeholder4AppidRow = new Label(container, SWT.NULL);
		placeholder4AppidRow.setSize(50, 0);
		
		// Create Accela App Secret (field label, text field, and placeholder label))
		Label appSecretLabel = new Label(container, SWT.NULL);
		appSecretLabel.setText(this.getLocalizationString("label_app_secret"));
		appSecretText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		appSecretText.setLayoutData(gd);	
		appSecretText.addModifyListener(new ModifyListener() {
	            public void modifyText(ModifyEvent e) {
	            	valueChanged(appSecretText);
	            }
	        });       
		Label placeholder4AppSecretRow = new Label(container, SWT.NULL);
		placeholder4AppSecretRow.setSize(50, 0);
		
		// Create Authorization Host(field label, text field, and placeholder label)
		Label authHostLabel = new Label(container, SWT.NULL);
		authHostLabel.setText(this.getLocalizationString("label_auth_host"));
		authHostText = new Text(container, SWT.BORDER | SWT.SINGLE);
		authHostText.setText(DEFAULT_AUTH_HOST);     // Default value
		gd = new GridData(GridData.FILL_HORIZONTAL);
		authHostText.setLayoutData(gd);
		authHostText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	valueChanged(authHostText);
            }
        }); 
		Label placeholder4AuthHostRow = new Label(container, SWT.NULL);
		placeholder4AuthHostRow.setSize(50, 0);
		
		// Create API Host(field label, text field, and placeholder label)
		Label apiHostLabel = new Label(container, SWT.NULL);
		apiHostLabel.setText(this.getLocalizationString("label_api_host"));
		apiHostText = new Text(container, SWT.BORDER | SWT.SINGLE);
		apiHostText.setText(DEFAULT_API_HOST);     // Default value
		gd = new GridData(GridData.FILL_HORIZONTAL);
		apiHostText.setLayoutData(gd);
		apiHostText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	valueChanged(apiHostText);
            }
        }); 
		Label placeholder4ApiHostRow = new Label(container, SWT.NULL);
		placeholder4ApiHostRow.setSize(50, 0);
		
		// Create Package Identifier(field label, read-only text field, and placeholder label)
		Label packageLabel = new Label(container,SWT.NULL);
		packageLabel.setText(this.getLocalizationString("label_package_identifier"));
		packageIdentifier = new Text(container, SWT.BORDER | SWT.SINGLE);
		packageIdentifier.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		packageIdentifier.setLayoutData(gd);
		// Invisible label to occupy width
        Label placeholder4PackageRow = new Label(container, SWT.NULL);
        placeholder4PackageRow.setSize(50, 0);
		
		// Create Module (field label, combo box field, and placeholder label)
		Label moduleLabel = new Label(container, SWT.NULL);
		moduleLabel.setText(this.getLocalizationString("label_module"));
		moduleCombo = new Combo(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		moduleCombo.setLayoutData(gd);	
		String[] moduleNames = {"AMS", "Building","CaseManagement","Enforcement","Licenses","Planning","ServiceRequest"};
		moduleCombo.setItems(moduleNames);
		moduleCombo.setText(moduleNames[0]);  	   // Default selection
		Label placeholder4ModuleRow = new Label(container, SWT.NULL);
		placeholder4ModuleRow.setSize(50, 0);
        
        // Create Android Version (field label, combo box field, and placeholder label)
		Label andriodVersionLabel = new Label(container, SWT.NULL);
		andriodVersionLabel.setText(this.getLocalizationString("label_android_version"));
		androidVersionCombo = new Combo(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		androidVersionCombo.setLayoutData(gd);			
		androidVersionCombo.setItems(androidVersions);
		androidVersionCombo.setText(androidVersions[0]);		   // Default selection
		// Invisible label to occupy width
        Label placeholder4AndroidRow = new Label(container, SWT.NULL);
        placeholder4AndroidRow.setSize(50, 0);
       
        // Initialize the dialog container
        setControl(container);
	}

	/**
	 * Override the method in its parent class WizardPage.
	 */
	public boolean isPageComplete() {
		return (   (!"".equalsIgnoreCase(productText.getText())) && (!"".equalsIgnoreCase(companyText.getText()))
				&& (!"".equalsIgnoreCase(appIdText.getText())) && (!"".equalsIgnoreCase(appSecretText.getText()))
				&& (!"".equalsIgnoreCase(authHostText.getText())) && (!"".equalsIgnoreCase(apiHostText.getText()))
				&& (!"".equalsIgnoreCase(moduleCombo.getText())) && (!"".equalsIgnoreCase(androidVersionCombo.getText()))
				);
    }	

	/**
	 * Update dialog status when user changes values in the wizard page.
	 */
	private void valueChanged(Text textField) {		
		if (textField == productText) {
			String productTextString =  productText.getText();
			if (!productTextString.contains(" ")) {
				setErrorMessage(null);
				packageIdentifier.setText(companyText.getText() + "." + productTextString);					
			} else {
				setErrorMessage(this.getLocalizationString("error_productName_has_space"));			
			}
		} else if (textField == companyText) {
			if (!companyText.getText().contains(" ")) {
				setErrorMessage(null);
				packageIdentifier.setText(companyText.getText() + "." + productText.getText() );								
			} else {
				setErrorMessage(this.getLocalizationString("error_companyIdentifier_has_space"));
			}		
		} else if (textField == appIdText) {			
			if (!appIdText.getText().contains(" ")) {
				setErrorMessage(null);
			} else {
				setErrorMessage(this.getLocalizationString("error_appId_has_space"));
			}
		} else if (textField == appSecretText){			
			if (!appSecretText.getText().contains(" ")) {
				setErrorMessage(null);
			} else {
				setErrorMessage(this.getLocalizationString("error_appSecret_has_space"));			}
											
		} else if (textField == authHostText) {
			if ((authHostText.getText().startsWith("http://")) || (authHostText.getText().startsWith("https://"))) {
				setErrorMessage(null);
			} else {
				setErrorMessage(this.getLocalizationString("error_authHost_invalid"));
			}
		}
		else if (textField == apiHostText) {
			if ((apiHostText.getText().startsWith("http://")) || (apiHostText.getText().startsWith("https://"))) {
				setErrorMessage(null);
			} else {
				setErrorMessage(this.getLocalizationString("error_apiHost_invalid"));
			}
		} else {
			setErrorMessage(null);
		}			
		// Activate Next button when all fields are filled
		if (this.isPageComplete()) 		
		{
			this.setPageComplete(true);
		} else {
			this.setPageComplete(false);
		}
	}	
	
	/**
	 * Get field label or message by key name from localization resource bundle.
	 */
	private String getLocalizationString(String key) {			 
        try {
            return localizationResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return '<' + key + '>';
        }
    }	
}