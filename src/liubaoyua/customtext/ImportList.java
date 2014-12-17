package liubaoyua.customtext;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
@SuppressLint("WorldReadableFiles")
public class ImportList extends Activity {

	private ListView listView;
	private File[] currentFiles;
	private static File prefsdir = new File(Environment.getDataDirectory()+"/data/" + "liubaoyua.customtext" + "/shared_prefs" );

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.list);
		LoadImportList();
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
			{
				File tmp = currentFiles[position];
				doImport(tmp);
			}
		});
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	private void inflateListView(File[] files) //¢Ù
	{
		List<Map<String, Object>> listItems = 
			new ArrayList<Map<String, Object>>();
		for (int i = 0; i < files.length; i++)
		{
			Map<String, Object> listItem = 	new HashMap<String, Object>();
			listItem.put("fileName", files[i].getName());
			listItems.add(listItem);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this
			, listItems, R.layout.import_list_item, new String[]{"fileName" }, new int[]{R.id.file_name }){
			@Override  
			public View getView(final int position, View convertView, ViewGroup parent) {  
			    if(convertView==null){  
			    convertView=View.inflate(ImportList.this, R.layout.import_list_item, null);  
			    }  
			    final Button del_button=(Button)convertView.findViewById(R.id.del_button);  
			    del_button.setOnClickListener(new OnClickListener(){  
			  
			        @Override  
			        public void onClick(View arg0) {  
			        	String files[] = currentFiles[position].list();  
					    for (String file : files) {  
					    	new File(currentFiles[position], file).delete();
					    }
						currentFiles[position].delete();
						LoadImportList();
			        }  
			       });  
			 return super.getView(position, convertView, parent);  
			} 
		};  
		listView.setAdapter(simpleAdapter);
//		simpleAdapter.notifyDataSetChanged();
	}
	
	private void doImport(final File tmp) {
		if (!tmp.exists()) {
			Toast.makeText(this, getString(R.string.imp_exp_file_doesnt_exist, tmp.getAbsolutePath()),
					Toast.LENGTH_LONG).show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.menu_import);
		builder.setMessage(R.string.imp_exp_confirm);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new ImportTask().execute(tmp);
			}
		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	
	private class ImportTask extends AsyncTask<File, String, String> {


		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(File... params) {
			File inFile = params[0];
			String tempFilename = "liubaoyua.customtext_preferences";
			// Make sure the shared_prefs folder exists, with the proper permissions
			getSharedPreferences(tempFilename, Context.MODE_WORLD_READABLE).edit().commit();
			try {
				String originfile[] = prefsdir.list();  
				for (String file : originfile) {  
			          new File(prefsdir, file).delete();
			 	}
				String files[] = inFile.list();  
				for (String file : files) {  
			          File srcFile = new File(inFile, file);  
			          File destFile = new File(prefsdir, file);  
			          copyFile(srcFile, destFile);
			          destFile.setReadable(true, false);
			          destFile.setWritable(true, true);
			 	}
			} catch (IOException ex) {
				return getString(R.string.imp_exp_import_error, ex.getMessage());
			}
			return getString(R.string.imp_exp_imported);
		}
		
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(ImportList.this, result, Toast.LENGTH_LONG).show();
		}
	}
	
	private static void copyFile(File source, File dest) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		boolean success = false;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			byte[] buf = new byte[10 * 1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.flush();
			out.close();
			out = null;
			success = true;
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception ex) {
				}
			}
			if (!success) {
				dest.delete();
			}
		}
	}
	
	void LoadImportList(){
		File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() 
				+ "/Custom Text");
		if (root.exists())
		{
			currentFiles = root.listFiles(new FileFilter() {

	            @Override
	            public boolean accept(File file) {
	                if (file.isDirectory())
	                    return true;
	                return false;
	            }
	        });
			Arrays.sort(currentFiles, new Comparator<Object>() {  
	            @Override
	            public int compare(Object file1, Object file2) {
	                return new String(((File)file1).getName().toLowerCase()).compareTo(((File)file2).getName().toLowerCase());
	            }
	        });
			inflateListView(currentFiles);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
