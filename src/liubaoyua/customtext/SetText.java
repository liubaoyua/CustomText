package liubaoyua.customtext;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import liubaoyua.customtext.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;


public class SetText extends Activity {
	
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	SharedPreferences globalpref;
	SharedPreferences.Editor globaleditor;
	String Package;
	int page=0;
	int maxpage;
	private Switch swtActive;
	TextView pageview;
	static int EditTextNum = 10;
	// the number of oristrname and newstrname shoube equal to EditTextNum
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
	EditText[] OriStrEdittext = new EditText[EditTextNum];
	EditText[] NewStrEdittext = new EditText[EditTextNum];
	Map<String, String> data = new HashMap<String, String>();

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
		maxpage=preferences.getInt("maxpage", 0);
		ApplicationInfo app;
		try {
			app = getPackageManager().getApplicationInfo(Package,0);
		} catch (NameNotFoundException e) {
			// Close the dialog gracefully, package might have been uninstalled
			finish();
			return;
		}
		//Set app icon and app name
		getActionBar().setIcon(app.loadIcon(getPackageManager()));
		this.setTitle(AppName);


		
		for (int i = 0; i < EditTextNum; i++){
			OriStrEdittext[i] = (EditText)findViewById(oristrname[i]);
			NewStrEdittext[i] = (EditText)findViewById(newstrname[i]);
		}
	
		pageview =(TextView)findViewById(R.id.pageview);
		Button button1 = (Button)findViewById(R.id.button1);
		Button button2 = (Button)findViewById(R.id.button2);
//		Button button3 = (Button)findViewById(R.id.button3);
		button1.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(page == 0)
					return;
				else{
					SaveToMap();
					page--;
					SetEditText(page);
				}
			}
		});
		button2.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				SaveToMap();
				page++;
				SetEditText(page);
			}
		});
//		button3.setOnClickListener(new OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				if(pageview.getText().toString().matches("\\d{1,3}")){
//					SaveToMap();
//					page=Integer.parseInt(pageview.getText().toString())-1;
//					SetEditText(page);
//				}
//			}
//		});		
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
	
		LoadFromFile();
		SetEditText(page);
		if (globalpref.getBoolean(Package, false)) {
			swtActive.setChecked(true);
		}	
		else {
			swtActive.setChecked(false);
		}
			
		
		 
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_app, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_save) {
			SaveToFile();
			AlertDialog.Builder builder = new AlertDialog.Builder(SetText.this);
			builder.setTitle(R.string.settings_apply_title);
			builder.setMessage(R.string.settings_apply_detail);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
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
		} else if (item.getItemId() == R.id.menu_app_clear_current) {
			for(int i=0;i<EditTextNum;i++){
				OriStrEdittext[i].setText("");
				NewStrEdittext[i].setText("");
			}
			SaveToMap();
		}else if (item.getItemId() == R.id.menu_app_clear_all) {
			data.clear();
			maxpage = page = 0 ;
			SetEditText(page);
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
	
	void SetEditText(int page){
		for(int i=0;i < EditTextNum; i++){
			int num = page*EditTextNum +i;  
			if (data.containsKey("oristr"+num))
				OriStrEdittext[i].setText((String)data.get("oristr"+num));
			else
				OriStrEdittext[i].setText("");
			if (data.containsKey("newstr"+num))
				NewStrEdittext[i].setText((String)data.get("newstr"+num));
			else
				NewStrEdittext[i].setText("");
		}
		pageview.setText(page+1+"");
	}
	
	void LoadFromFile(){
		int num = (maxpage + 1) * EditTextNum;
		for(int i=0;i<num;i++){
			data.put("oristr"+i, preferences.getString("oristr"+i,""));
			data.put("newstr"+i, preferences.getString("newstr"+i,""));
		}
	} 
	
	void SaveToMap(){
		int num;
		for(int i=0;i< EditTextNum ;i++){
			num = i + page * EditTextNum; 
			data.put("oristr"+num, OriStrEdittext[i].getText().toString());
			data.put("newstr"+num, NewStrEdittext[i].getText().toString());
		}
//		Log.e("customtext",page+"page");
		if (page>maxpage)
			maxpage=page;
	}
	 

	void SaveToFile() {
		SaveToMap();
		int num = (maxpage + 1) * EditTextNum;
		int var = 0; 
		for(int i=0;i<num;i++){
			if(!data.get("oristr"+i).equals("")||!data.get("newstr"+i).equals("")){
				editor.putString("oristr"+var, data.get("oristr"+i));
				editor.putString("newstr"+var, data.get("newstr"+i));
				var++;
				Log.e("customtext", var+"var"+i+"i"+ data.get("newstr"+i));
			}
		}
		for(int i=var;i<num;i++){
			editor.remove("oristr"+i);
			editor.remove("newstr"+i);

		}
		maxpage = var / EditTextNum ;
		editor.putInt("maxpage", maxpage);
		editor.commit();
		data.clear();
		LoadFromFile();
		page=0;
		SetEditText(page);
	}
	
}
