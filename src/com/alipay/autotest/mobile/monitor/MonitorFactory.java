package com.alipay.autotest.mobile.monitor;

import com.alipay.autotest.mobile.SystemPropoerties;


public class MonitorFactory {
	private static MonitorInterface sInstance;

	public static MonitorInterface getInstance(String platformName) {
		if (sInstance == null) {
			if (SystemPropoerties.VALUE_ANDROID.equals(platformName)) {
				sInstance = new AndroidMonitor();
			} else {
				sInstance = new IOSMonitor();
			}
		}

		return sInstance;
	}

}