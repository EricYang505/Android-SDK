package company.product;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import com.accela.mobile.AccelaMobile;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

public class AppContext extends Application {
	public AccelaMobile accelaMobile;	
	public Properties accelaProperties;		
	
	@Override
	public void onCreate() {
		super.onCreate();	
		// Initialize the properties
		try {
			this.getAccelaPropties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public AccelaMobile getAccelaMobile(Activity activity) {
		if (accelaMobile == null) {		
			String appId = accelaProperties.getProperty("accela.app.id");
			String appSecret = accelaProperties.getProperty("accela.app.secret");
			String authServer = accelaProperties.getProperty("accela.auth.host");
			String apiServer = accelaProperties.getProperty("accela.api.host");
			
			// Create an AccelaMobile instance with the App ID and App Secret of the registered app.
			accelaMobile = new AccelaMobile(activity, appId, appSecret,null,authServer,apiServer);
			// Set the environment.
			accelaMobile.setEnvironment(AccelaMobile.Environment.PROD);
			// Set the URL schema.
			// NOTE: The assigned value should be same as the value of "android:scheme" under "AuthorizationActivity" in the project's AndroidManifest.xml.
			accelaMobile.setUrlSchema(accelaProperties.getProperty("schema"));
		}
		
		return accelaMobile;
	}

	/**
	 * Get the Properties instance attached to the current application. 	 
	 *          
	 * @return The current Properties instance.
	 * 
	 */
	public Properties getAccelaPropties() throws IOException {
		if (accelaProperties == null) {
			String file = "accela.properties";
			accelaProperties = new Properties();
			try {
				InputStream fileStream = getAssets().open(file);
				accelaProperties.load(fileStream);
				fileStream.close();
			} catch (FileNotFoundException e) {
				Log.d("DEBUG", "Ignoring missing property file: " + file);
			}		
		}
		return accelaProperties;
	}
	
}
