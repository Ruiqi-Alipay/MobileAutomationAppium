package com.alipay.autotest.mobile.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

public class IOSKeyboardManager {
	private boolean textKeyboard = true;
	private int NUM_X = 0;
	private int NUM_Y = 0;

	public IOSKeyboardManager(AppiumDriver driver) {
		String[] dimensions = driver.manage().window().getSize().toString()
				.split("\\D");
		int screenWidth = Integer.parseInt(dimensions[1]);
		int screenHeight = Integer.parseInt(dimensions[3]);
		NUM_X = 30;
		NUM_Y = screenHeight - 30;
	}

	public void keyin(AppiumDriver driver, String text) {
		int number = -1;
		try {
			number = Integer.valueOf(text);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (number >= 0) {
			if (textKeyboard) {
				driver.tap(1, NUM_X, NUM_Y, 1);
				textKeyboard = false;
				sleepInMillionsecond(300);
			}

			driver.findElementByName(text).click();
		} else {
			if (!textKeyboard) {
				driver.tap(1, NUM_X, NUM_Y, 1);
				textKeyboard = true;
				sleepInMillionsecond(300);
			}
			driver.findElementByName(text).click();
		}
	}

	private static void sleepInMillionsecond(int second) {
		try {
			Thread.sleep(second);
		} catch (InterruptedException e) {
		}
	}
}
