package liubaoyua.customtext;

import java.io.DataOutputStream;

import liubaoyua.customtext.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SetText extends Activity {
	
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	
	SharedPreferences globalpref;
	SharedPreferences.Editor globaleditor;
	String Package;
	private Switch swtActive;
	final int[] oristrname = new int[] {
			R.id.origintext0,
			R.id.origintext1, 
			R.id.origintext2, 
			R.id.origintext3,
			R.id.origintext4, 
			R.id.origintext5,
			R.id.origintext6,
			R.id.origintext7, 
			R.id.origintext8,
			R.id.origintext9 };
	final int[] newstrname = new int[] {
			R.id.newtext0,
			R.id.newtext1, 
			R.id.newtext2, 
			R.id.newtext3,
			R.id.newtext4, 
			R.id.newtext5,
			R.id.newtext6,
			R.id.newtext7, 
			R.id.newtext8,
			R.id.newtext9};
	EditText[] oristr = new EditText[oristrname.length];
	EditText[] newstr = new EditText[newstrname.length];
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		swtActive = new Switch(this);
		getActionBar().setCustomView(swtActive);
		getActionBar().setDisplayShowCustomEnabled(true);
		setContentView(R.layout.activity_settings);
		
		Intent intent = getIntent();
		String AppName = intent.getStringExtra("name");
		Package = intent.getStringExtra("package");
		
		preferences = getSharedPreferences(Package, MODE_WORLD_READABLE);
		editor = preferences.edit();
		globalpref = getSharedPreferences("liubaoyua.customtext_preferences", MODE_WORLD_READABLE);
		globaleditor = globalpref.edit();
		
		this.setTitle(AppName);
		if (globalpref.getBoolean(Package, false)) 
			swtActive.setChecked(true);
			else 
			swtActive.setChecked(false);

		for (int i = 0; i < oristrname.length; i++){
			oristr[i] = (EditText)findViewById(oristrname[i]);
			newstr[i] = (EditText)findViewById(newstrname[i]);
		}

		
		for(int i=0;i < oristrname.length; i++){
			oristr[i].setText(preferences.getString("oristr"+i, null));
			newstr[i].setText(preferences.getString("newstr"+i, null));
		}
		
		swtActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					globaleditor.putBoolean(Package, true);
					globaleditor.commit();
				}
				else{
					globaleditor.putBoolean(Package, false);
					globaleditor.commit();
				}
				
			}
		});
		 
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_app, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_save) {
			for(int i=0;i<oristrname.length;i++){
				editor.putString("oristr"+i, oristr[i].getText().toString());
				editor.putString("newstr"+i, newstr[i].getText().toString());
			}
			editor.commit();
			AlertDialog.Builder builder = new AlertDialog.Builder(SetText.this);
			builder.setTitle(R.string.settings_apply_title);
			builder.setMessage(R.string.settings_apply_detail);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Send the broadcast requesting to kill the app
					killPackage(Package);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		} else if (item.getItemId() == R.id.menu_app_launch) {
			Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(Package);
			startActivity(LaunchIntent);
		} else if (item.getItemId() == R.id.menu_app_clean_settings) {
			for(int i=0;i<oristrname.length;i++){
				oristr[i].setText("");
				newstr[i].setText("");
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void killPackage(String packageToKill) {
		// code modified from :
		// http://forum.xda-developers.com/showthread.php?t=2235956&page=6
		try { // get superuser
			Process su = Runtime.getRuntime().exec("su");
			if (su == null)
				return;
			DataOutputStream os = new DataOutputStream(su.getOutputStream());
			os.writeBytes("pkill " + packageToKill + "\n");
			os.writeBytes("exit\n");
			su.waitFor();
			os.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
