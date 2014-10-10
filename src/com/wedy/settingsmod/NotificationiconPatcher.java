package com.wedy.settingsmod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.List;

import android.content.res.XModuleResources;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class NotificationiconPatcher implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
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
		boolean isDatawkk = preference.getBoolean("key_datawkk", false);
		if(isDatawkk){
		
			resparam.res.setReplacement("com.android.settings", "bool", "config_showAdviceOfCharge", modRes.fwd(R.bool.config_showAdviceOfCharge));
		}
		boolean isBtwa = preference.getBoolean("key_btw", false);

		if(isBtwa){
		
			resparam.res.setReplacement("com.android.settings", "bool", "config_showTetheringDialogCheckbox", true);
		}
		boolean isBtapn = preference.getBoolean("key_btwapn", false);
		if(isBtapn){
		
			resparam.res.setReplacement("com.android.settings", "bool", "config_hideTetherDunApn", false);
			resparam.res.setReplacement("com.android.settings", "bool", "config_editableTetherDunApn", true);
		}
		

	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
	if ( ! lpparam.packageName.equals("com.android.settings") )
	return;
	
	boolean isBnum = preference.getBoolean("key_battery", false);
		if(isBnum){
		
			@SuppressWarnings("unused")
			final Class<?> powerUsageSummary = XposedHelpers.findClass("com.android.settings.fuelgauge.PowerUsageSummary", lpparam.classLoader);
			final Class<?> batteryHistoryPreference = XposedHelpers.findClass("com.android.settings.fuelgauge.BatteryHistoryPreference", lpparam.classLoader);
			final Class<?> powerGaugePreference = XposedHelpers.findClass("com.android.settings.fuelgauge.PowerGaugePreference", lpparam.classLoader);
			findAndHookMethod("com.android.settings.fuelgauge.PowerUsageSummary", lpparam.classLoader, "refreshStats", new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
			PreferenceGroup mAppListGroup = (PreferenceGroup) XposedHelpers.getObjectField(param.thisObject, "mAppListGroup");
			mAppListGroup.removeAll();
			mAppListGroup.setOrderingAsAdded(false);
			Preference mBatteryStatusPref = (Preference) XposedHelpers.getObjectField(param.thisObject, "mBatteryStatusPref");
			mBatteryStatusPref.setOrder(-2);
			mAppListGroup.addPreference(mBatteryStatusPref);
			Object mStatsHelper = XposedHelpers.getObjectField(param.thisObject, "mStatsHelper");
			Object hist = XposedHelpers.newInstance(batteryHistoryPreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.callMethod(mStatsHelper, "getStats"));
			XposedHelpers.callMethod(hist, "setOrder", -1);
			XposedHelpers.callMethod(mAppListGroup, "addPreference", hist);
			if ((Double) XposedHelpers.callMethod(XposedHelpers.callMethod(mStatsHelper, "getPowerProfile"), "getAveragePower", "screen.full") < 10.0) {
			XposedHelpers.callMethod(param.thisObject, "addNotAvailableMessage");
			return null;
			}
			XposedHelpers.callMethod(mStatsHelper, "refreshStats", false);
			@SuppressWarnings("unchecked")
			List<Object> usageList = (List<Object>) XposedHelpers.callMethod(mStatsHelper, "getUsageList");
			for (Object sipper : usageList) {
			//if ((Double) XposedHelpers.callMethod(sipper, "getSortValue") < 5.0)
			//continue;
			final double percentOfTotal = ((Double) XposedHelpers.callMethod(sipper, "getSortValue") / (Double) XposedHelpers.callMethod(mStatsHelper, "getTotalPower") * 100);
			//if (percentOfTotal < 1)
			//continue;
			Object pref = XposedHelpers.newInstance(powerGaugePreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.callMethod(sipper, "getIcon"), sipper);
			final double percentOfMax = ((Double) XposedHelpers.callMethod(sipper, "getSortValue") * 100) / (Double) XposedHelpers.callMethod(mStatsHelper, "getMaxPower");
			XposedHelpers.setObjectField(sipper, "percent", percentOfTotal);
			XposedHelpers.callMethod(pref, "setTitle", XposedHelpers.getObjectField(sipper, "name"));	
			XposedHelpers.callMethod(pref, "setOrder", (int) (Integer.MAX_VALUE - (Double) XposedHelpers.callMethod(sipper, "getSortValue")));
			XposedHelpers.callMethod(pref, "setPercent", percentOfMax, percentOfTotal);
			if (XposedHelpers.getObjectField(sipper, "uidObj") != null) {
			XposedHelpers.callMethod(pref, "setKey", Integer.toString( (Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(sipper, "uidObj"), "getUid") ));
			}
			int MAX_ITEMS_TO_LIST = Integer.parseInt(preference.getString("key_app_num", "30"));
			XposedHelpers.callMethod(mAppListGroup, "addPreference", pref);
			if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST+1))
			break;
			}	
			return null;
			}
			});
		}
	}

}
