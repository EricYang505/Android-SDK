package company.product;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.accela.mobile.*;
import com.accela.mobile.AccelaMobile.AuthorizationStatus;

public class MainActivity extends Activity implements OnClickListener  {
	private TextView tvToken;
	private Button btnSignIn, btnSignOut;
	private ProgressDialog accessTokenProgressDialog = null;
	private AppContext appContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);        
		// Initialize AccelaMobile context
		appContext = (AppContext)this.getApplicationContext();
		appContext.accelaMobile  = appContext.getAccelaMobile(this);
		appContext.accelaMobile.setSessionDelegate(authSessionDelegate);
		// Initialize controls
		initControls();	
	}
	
	// Show progress dialog if resume the current activity from authorization web view..
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {		
		super.onWindowFocusChanged(hasFocus);					
		if ( (hasFocus) && (appContext.accelaMobile != null)
		  && (appContext.accelaMobile.getAuthorizationStatus() == AuthorizationStatus.AUTHORIZED)) {
			accessTokenProgressDialog = ProgressDialog.show(this, null, this.getResources().getString(R.string.msg_login), true, true);
		} 
	}	

	public void onClick(View view) {				
		switch (view.getId()) {		
			case R.id.btnSignIn:
				// Define permission scope.
				String[] permissions4Authorization  = new String[] {"search_records", "get_records", "get_record", "create_record"};
				// Start to process authorization request.	 		
				appContext.accelaMobile.authorize(permissions4Authorization);
				break;
			case R.id.btnSignOut:
				// Log out user's session
				appContext.accelaMobile.logout();				
			}
	}	
	
	private void initControls() {
		// Initialize text views		
		this.tvToken = (TextView) this.findViewById(R.id.tvToken);		
		// Initialize buttons
		this.btnSignIn = (Button) this.findViewById(R.id.btnSignIn);
		this.btnSignOut = (Button) this.findViewById(R.id.btnSignOut);
		this.btnSignIn.setOnClickListener(this);        
		this.btnSignOut.setOnClickListener(this);	
		this.btnSignOut.setEnabled(false);
	}
	
	private void enableBusinessButtons(Boolean sessionValid) {
		if (sessionValid) {			
			this.tvToken.setText(R.string.msg_logged_in);
			this.btnSignIn.setEnabled(false);
			this.btnSignOut.setEnabled(true);
		} else {			
			this.tvToken.setText(R.string.msg_not_logged_in);
			this.btnSignIn.setEnabled(true);
			this.btnSignOut.setEnabled(false);
		}
	}
	
	
	private AMSessionDelegate authSessionDelegate = new AMSessionDelegate() {
		public void amDidLogin() {
			// Dismiss progress dialog.
			if ((accessTokenProgressDialog != null) && (accessTokenProgressDialog.isShowing())) {
				accessTokenProgressDialog.dismiss();	
			}
			// Refresh button status based on session.
		 	boolean sessionValid = (appContext.accelaMobile != null) && (appContext.accelaMobile.isSessionValid());
		 	enableBusinessButtons(sessionValid);			
			// Show message.
			AMLogger.logInfo("Invoked Session Delegate.amDidLogin()...");
		}		
		
		public void amDidLoginFailure(AMError error) {	
			AMLogger.logInfo("Invoked Session Delegate.amDidLoginFailure(): %s", error.toString());					
		}
		
		public void amDidCancelLogin() {
			AMLogger.logInfo("Invoked Session Delegate.amDidCancelLogin()...");				
		}
		
		public void amDidSessionInvalid(AMError error) {
			AMLogger.logInfo("Invoked Session Delegate.amDidSessionInvalid(): %s", error.toString());				
		}	
		
		public void amDidLogout() {
			enableBusinessButtons(false);	
			AMLogger.logInfo("Invoked Session Delegate.amDidLogout()...");	
		}		
	};
}