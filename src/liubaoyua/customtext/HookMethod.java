package liubaoyua.customtext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.graphics.Paint;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
//import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HookMethod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

   private static final String PKG_NAME = "liubaoyua.customtext";
   private XSharedPreferences global_prefs;
   private XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    	 global_prefs = new XSharedPreferences(PKG_NAME);
         global_prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        
    	global_prefs.reload();
        if (!global_prefs.getBoolean("moduleswitch", true)) {
            return;
        }
    	prefs = new XSharedPreferences(PKG_NAME,lpparam.packageName);
    	prefs.makeWorldReadable();
    	boolean ImageText = global_prefs.getBoolean("ImageText", false);
    	final boolean global_tag = global_prefs.getBoolean(PKG_NAME+"_preferences", false);
    	final boolean current_app_tag = global_prefs.getBoolean(lpparam.packageName,false);
    	boolean my_app_tag =lpparam.packageName.equals(PKG_NAME)?true:false;
    	XC_MethodHook textMethodHook ;
    	if (my_app_tag){
    		if (current_app_tag)
    			return;
    		textMethodHook = new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
	            	String abc = (String) methodHookParam.args[0];
	            	if (abc != null) {
		                abc=abc.replaceAll("^文本自定义$","文本自定义,模块测试可用 :)" );
		                abc=abc.replaceAll("^Custom Text$","Custom Text, it works:)" );
		                methodHookParam.args[0] = abc;
	            	}
	            }
    	 	};
    	}else{
        	if(!global_tag&&!current_app_tag)
    			return;
    		final int num = prefs.getInt("maxpage", 0)*10+10;
        	final String[] oristr = new String[num];
        	final String[] newstr = new String[num];
        	if(current_app_tag){
            	for (int i=0; i<num ; i++){
            		oristr[i] = prefs.getString("oristr"+i, "");
            		newstr[i] = prefs.getString("newstr"+i, "");
            	}
        	}
    		final int global_num = global_prefs.getInt("maxpage", 0)*10+10;
        	final String[] global_oristr = new String[global_num];
        	final String[] global_newstr = new String[global_num];
        	if(global_tag){
            	for (int i=0; i<global_num ; i++){
            		global_oristr[i] = global_prefs.getString("oristr"+i, "");
            		global_newstr[i] = global_prefs.getString("newstr"+i, "");
            	}
        	}
        	if (ImageText){
    	    		textMethodHook = new XC_MethodHook() {
    		            @Override
    		            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
    		            	CharSequence actualText = (CharSequence) methodHookParam.args[0];
    		            		if (actualText != null) {
    		            			String abc = actualText.toString();
    		            			if(global_tag)
    		            				abc=ReplaceText(global_oristr, global_newstr, global_num, abc);
    		            			if(current_app_tag)
    		            				abc=ReplaceText(oristr, newstr, num, abc);
    		            			methodHookParam.args[0] =abc ;
    		            	}
    		            }
    	    	 	};
    		}else{
        		textMethodHook = new XC_MethodHook() {
		            @Override
		            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
	            		if (!methodHookParam.args[0].getClass().getSimpleName().contains("SpannableString")){
	            			String abc = (String) methodHookParam.args[0];
		            		if (abc != null) {
		            			if(global_tag)
		            				abc=ReplaceText(global_oristr, global_newstr, global_num, abc);
		            			if(current_app_tag)
		            				abc=ReplaceText(oristr, newstr, num, abc);
		            			methodHookParam.args[0] = abc;
		            		}	
		            	}
		            }
	    	 	};
        	}
    	}
        
       findAndHookMethod(TextView.class, "setText", CharSequence.class,
    		   TextView.BufferType.class, boolean.class, int.class, textMethodHook);
       findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
       findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,
    		   float.class, float.class, Paint.class, textMethodHook);
    }
    
    
    private String ReplaceText(String[] oristr, String[] newstr, int num, String abc){
    	for(int i=0; i<num ; i++){
          	if (!oristr[i].equals(""))
          		abc=abc.replaceAll(oristr[i],newstr[i]);
    	}
    	return abc;
    } 
    
}