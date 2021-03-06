/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.appium;

import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.coobird.thumbnailator.Thumbnails;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.alipay.autotest.mobile.model.TestAction;
import com.alipay.autotest.mobile.model.TestActionTypes;
import com.alipay.autotest.mobile.model.TestTarget;
import com.alipay.autotest.mobile.utils.AliElementNotFoundException;
import com.alipay.autotest.mobile.utils.AliKeyboardManager;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.StringUtil;
import com.alipay.autotest.mobile.utils.TextUtils;

/**
 * @author ruiqi.li
 */
public class AppiumHelper {

	public static final int DEFAULT_WAIT_ELEMENT_SECOND = 20;
	public static final int DEFAULT_ROLLBACK_WAIT_SECOND = 3;
	private static final int WEB_DRIVER_RETRY_COUNT = 3;

	/**
	 * Take capture and save to path using the given name
	 * 
	 * @param driver
	 *            AppiumDriver
	 * @param dir
	 *            save path
	 * @param fileName
	 *            save file name
	 */
	public static File takeCaptureToDir(AppiumDriver driver, File dir,
			String fileName) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String dateTime = formatter.format(new Date());
		File file = new File(dir, dateTime + "_" + fileName);
		takeCapture(driver, file);
		return file;
	}

	/**
	 * Take capture and save to file
	 * 
	 * @param driver
	 *            AppiumDriver
	 * @param file
	 *            save to file
	 */
	public static void takeCapture(AppiumDriver driver, File file) {
		File screenshot = driver.getScreenshotAs(OutputType.FILE);
		try {
			Thumbnails.of(screenshot).size(320, 480).outputQuality(0.5)
					.toFile(file);
		} catch (IOException e) {
			screenshot.renameTo(file);
		}
	}

	/**
	 * Take capture
	 * 
	 * @param driver
	 *            AppiumDriver
	 */
	public static File takeTempCapture(AppiumDriver driver) {
		return driver.getScreenshotAs(OutputType.FILE);
	}

	public static void input(AppiumDriver driver, WebElement element,
			String text) {
		element.click();
		sleepInMillionsecond(200);

		if (TestContext.getInstance().isTestingAndroid()) {
			for (int i = 0; i < WEB_DRIVER_RETRY_COUNT; i++) {
				String elementText = element.getText();
				if (TextUtils.isEmpty(elementText)
						|| !text.replaceAll(" ", "").equals(
								elementText.replaceAll(" ", ""))) {
					element.sendKeys(text);
					sleepInMillionsecond(200);
				} else {
					return;
				}
			}
		} else {
			element.sendKeys(text);
		}
	}

	/**
	 * Perform action
	 * 
	 * @param driver
	 *            Appium driver
	 * @param action
	 *            click/input/clear/scroll, etc
	 * @throws VerifyFailedException
	 */
	public static void performAction(AppiumDriver driver, TestAction action,
			int waitSecond) throws AliElementNotFoundException,
			VerifyFailedException {
		LogUtils.log("Perform Action: " + action.getOriginalCommand());
		TestTarget target = action.getTestTarget();
		String actionType = action.getType();
		String actionParams = action.getParams();

		AliKeyboardManager.getInstance(driver);

		if (TestActionTypes.ACTION_TYPE_CLICK.equals(actionType)
				|| TestActionTypes.ACTION_TYPE_CLICK_LOCATION
						.equals(actionType)
				|| TestActionTypes.ACTION_TYPE_CLICK_IMAGE.equals(actionType)) {
			WebElement element = findElement(driver, target, waitSecond);
			try {
				element.click();
			} catch (StaleElementReferenceException e) {
				AliKeyboardManager.getInstance(driver).mockDialogOk(driver);
			}
		} else if (TestActionTypes.ACTION_TYPE_CHECK.equals(actionType)
				|| TestActionTypes.ACTION_TYPE_CHECKBOX_LOCATION
						.equals(actionType)
				|| TestActionTypes.ACTION_TYPE_RADIO_LOCATION
						.equals(actionType)) {
			WebElement element = findElement(driver, target, waitSecond);
			element.click();
		} else if (TestActionTypes.ACTION_TYPE_INPUT.equals(actionType)) {
			WebElement element = findElement(driver, target, waitSecond);

			input(driver, element, actionParams);
			try {
				driver.hideKeyboard();
			} catch (Exception e) {
			}
		} else if (TestActionTypes.ACTION_TYPE_ALIKEYBORAD.equals(actionType)
				|| TestActionTypes.ACTION_TYPE_ALIKEYBORAD_NUM
						.equals(actionType)) {
			sleepInMillionsecond(1000);
			if (TestContext.getInstance().isTestingAndroid()) {
				AliKeyboardManager keyboardManager = AliKeyboardManager
						.getInstance(driver);
				String[] passwordArray = actionParams.split("");
				for (int i = 0; i < passwordArray.length; i++) {
					String oneByOne = passwordArray[i];
					if (!TextUtils.isEmpty(oneByOne)) {
						if (TestActionTypes.ACTION_TYPE_ALIKEYBORAD
								.equals(actionType)) {
							keyboardManager.keyIn(oneByOne, driver);
						} else {
							keyboardManager.passwordIn(oneByOne, driver);
							sleepInMillionsecond(1000);
						}
					}
				}
				if (TestActionTypes.ACTION_TYPE_ALIKEYBORAD.equals(actionType)) {
					keyboardManager.resetKeyboard(driver);
				} else {
					keyboardManager.passwordIn("ok", driver);
				}	
			} else {
				driver.getKeyboard().sendKeys(actionParams);
			}
		} else if (TestActionTypes.ACTION_TYPE_CLEAR.equals(actionType)) {
			WebElement element = findElement(driver, target, waitSecond);
			element.clear();
		} else if (TestActionTypes.ACTION_TYPE_CLEAR_ALL.equals(actionType)) {
			try {
				findElement(driver, target, waitSecond);
				List<WebElement> elements = driver
						.findElementsByClassName(TestContext.getInstance()
								.isTestingAndroid() ? "android.widget.EditText"
								: "UIATextField");

				for (int i = 0; i < WEB_DRIVER_RETRY_COUNT; i++) {
					if (elements != null && !elements.isEmpty()) {
						boolean clearSuccess = true;
						for (WebElement element : elements) {
							element.clear();

							if (TestContext.getInstance().isTestingAndroid()) {
								String text = element.getText();
								if (text != null && text.length() > 10) {
									clearSuccess = false;
								}
							}
						}

						if (clearSuccess) {
							break;
						}
					} else {
						break;
					}
				}
			} catch (Exception e) {
				LogUtils.log(e.getMessage());
			}
		} else if (TestActionTypes.ACTION_TYPE_KEYEVENT.equals(actionType)) {
			driver.sendKeyEvent(StringUtil.strToInteger(actionParams, 0));
		} else if (TestActionTypes.ACTION_TYPE_SCROLLDOWN.equals(actionType)) {
			driver.swipe(100, 500, 100, 100, 1000);
		} else if (TestActionTypes.ACTION_TYPE_QUICK_CHECK.equals(actionType)) {
			String startCharacter = actionParams.substring(0, 1);
			WebElement quickIndex = findElement(driver, new TestTarget(
					TestTarget.TARGET_TYPE_NAME, startCharacter),
					AppiumHelper.DEFAULT_ROLLBACK_WAIT_SECOND);
			quickIndex.click();
			int findRetry = 10;
			while (findRetry > 0) {
				findRetry--;
				try {
					WebElement element = findElement(driver, target, waitSecond);
					element.click();
					break;
				} catch (Exception e) {
					driver.swipe(100, 500, 100, 100, 1000);
					sleepInMillionsecond(100);
				}
			}
		} else if (TestActionTypes.ACTION_TYPE_TEST_VERIFY.equals(actionType)) {
			try {
				boolean result = hasElement(driver, target, waitSecond);
				if (!result) {
					throw new VerifyFailedException(action.getOriginalCommand());
				}
			} catch (Exception e) {
				throw new VerifyFailedException(action.getOriginalCommand());
			}
		} else if (TestActionTypes.ACTION_TYPE_PIXEL_VERIFY.equals(actionType)) {
			// try {
			// File currentActivity = AppiumHelper.takeTempCapture(driver);
			// float percent = StringUtil.strToFloat(mVerifyParams, 0.8F);
			// return ImageUtils.sameAs(TestFileManager.getInstance()
			// .getVerityImageFile(mVerifyElement), currentActivity,
			// percent);
			// } catch (Exception e) {
			// return false;
			// }
		}
	}

	public static boolean hasElement(AppiumDriver driver, TestTarget widget,
			int waitSecond) {
		try {
			return findElement(driver, widget, waitSecond) != null;
		} catch (AliElementNotFoundException e) {
			return false;
		}
	}

	private static WebElement findElement(AppiumDriver driver,
			TestTarget widget, int waitSecond)
			throws AliElementNotFoundException {
		String locatorType = widget.getType();
		String locatorName = widget.getElement();
		try {
			driver.hideKeyboard();
		} catch (Exception e) {
		}

		for (int i = 0; i < waitSecond; i += 2) {
			try {
				if (TestTarget.TARGET_TYPE_NAME.equals(locatorType)) {
					int diliverPos = locatorName.indexOf("||");
					if (diliverPos > 0 && diliverPos < locatorName.length()) {
						String[] items = locatorName.split("\\|\\|");
						for (int index = 0; index < items.length; index++) {
							try {
								WebElement element = TestContext.getInstance()
										.isAndroidApiUnder17() ? driver
										.findElementByLinkText(items[index]
												.trim()) : driver
										.findElementByName(items[index].trim());
								if (element != null) {
									return element;
								}
							} catch (NoSuchElementException e) {

							}
						}
						LogUtils.log("Finding element: " + locatorName);
					} else {
						WebElement element = TestContext.getInstance()
								.isAndroidApiUnder17() ? driver
								.findElementByLinkText(locatorName) : driver
								.findElementByName(locatorName);
						if (element != null) {
							return element;
						}
					}
				} else if (TestTarget.TARGET_TYPE_LOCATION.equals(locatorType)) {
					int dividerIndex = locatorName.indexOf("[");
					String className = locatorName.substring(0, dividerIndex);
					int index = Integer.valueOf(locatorName.substring(
							dividerIndex + 1, locatorName.length() - 1));
					List<WebElement> elements = driver
							.findElementsByClassName(className);
		            
					if (elements != null && elements.size() >= index) {
						if (TestContext.getInstance().isTestingAndroid()) {
							return elements.get(index - 1);
						} else {
							List<WebElement> realElements = new ArrayList<WebElement>();
							for (int x = 0; x < elements.size(); x+=2) {
								try {
									WebElement element = elements.get(x);
									element.sendKeys("");
									realElements.add(element);
								} catch (Exception e) {
									LogUtils.log(e.toString());
								}
							}
							return realElements.get(index - 1);
						}
					}
				}
			} catch (NoSuchElementException e) {
				LogUtils.log("Finding element: " + locatorName);
			}

			sleepInMillionsecond(2000);
		}

		LogUtils.log("Throw exception!!!");

		throw new AliElementNotFoundException("Type: " + locatorType
				+ "; Name: " + locatorName);
	}

	private static void sleepInMillionsecond(int second) {
		try {
			Thread.sleep(second);
		} catch (InterruptedException e) {
		}
	}
}
