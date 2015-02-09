package com.alipay.autotest.mobile.appium;

import java.net.URL;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.RemoteTouchScreen;

public class LocalAndroidDriver extends AppiumDriver implements HasTouchScreen {

	public RemoteTouchScreen touch;

	public LocalAndroidDriver(URL remoteAddress, Capabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);
		touch = new RemoteTouchScreen(getExecuteMethod());
	}

	public TouchScreen getTouch() {
		return touch;
	}

	public MobileElement scrollTo(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public MobileElement scrollToExact(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
