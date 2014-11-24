package liubaoyua.customtext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.graphics.Paint;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HookMethod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

   private static final String PKG_NAME = "liubaoyua.customtext";
   private XSharedPreferences globalprefs;
   private XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    	 globalprefs = new XSharedPreferences(PKG_NAME);
         globalprefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        
    	globalprefs.reload();
        if (!globalprefs.getBoolean("moduleswitch", true)) {
            return;
        }
        boolean tag = false; 
        // detect the packageName, if it is this app,set tag to true 
        if (!globalprefs.getBoolean(lpparam.packageName, false)){ //
        	if(!lpparam.packageName.equals(PKG_NAME))
        		return;
        	else
        		tag = true;
	    }
        // textMethodHook method require a final var, so...
        final boolean tag2 = tag; 
        XposedBridge.log(tag2 + ": tag2 loaded.");
    	prefs = new XSharedPreferences(PKG_NAME,lpparam.packageName);
    	prefs.makeWorldReadable();
    	XposedBridge.log(lpparam.packageName + ": globalprefs loaded.");
    	final int num = prefs.getInt("maxpage", 0)*10+10;
    	final String[] oristr = new String[num];
    	final String[] newstr = new String[num];
    	for (int i=0; i<num ; i++){
    		oristr[i] = prefs.getString("oristr"+i, "");
    		newstr[i] = prefs.getString("newstr"+i, "");
    	}

    	// common method hook for textviews
    	XC_MethodHook textMethodHook = new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            	String abc = (String) methodHookParam.args[0];
                if (abc != null) {
                	if (!tag2){ // tag2 won't be true in other app.
                		for(int i=0; i<num ; i++){
                			if (!oristr[i].equals(""))
                				abc=abc.replaceAll(oristr[i],newstr[i]);
                		}

                	}
                	else{
                		abc=abc.replaceAll("文本自定义","文本自定义,模块测试可用 :)" );
                		abc=abc.replaceAll("Custom Text","Custom Text, it works:)" );
                	}
                	methodHookParam.args[0] = abc;
                }
            }
        };

        
       findAndHookMethod(TextView.class, "setText", CharSequence.class,
    		   TextView.BufferType.class, boolean.class, int.class, textMethodHook);
       findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
       findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,
    		   float.class, float.class, Paint.class, textMethodHook);
        
         
        
    }

}