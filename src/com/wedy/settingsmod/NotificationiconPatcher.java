package com.wedy.settingsmod;


import android.content.res.XModuleResources;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class NotificationiconPatcher implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
	private static XSharedPreferences preference = null;
	private static String MODULE_PATH = null;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
	preference = new XSharedPreferences(NotificationiconPatcher.class.getPackage().getName());
		MODULE_PATH = startupParam.modulePath;
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (!resparam.packageName.equals("com.android.settings"))
			return;

		
		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		boolean isDataw = preference.getBoolean("key_dataw", false);

		if(isDataw){
		
			resparam.res.setReplacement("com.android.settings", "bool", "config_showMobileDataCostWarning", modRes.fwd(R.bool.config_showMobileDataCostWarning));
		}
		boolean isBtwa = preference.getBoolean("key_btw", false);

		if(isBtwa){
		
			resparam.res.setReplacement("com.android.settings", "bool", "config_showTetheringDialogCheckbox", true);
		}

	}

}
