/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.model;

import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alipay.autotest.mobile.appium.AppiumHelper;
import com.alipay.autotest.mobile.utils.ImageUtils;
import com.alipay.autotest.mobile.utils.StringUtil;
import com.alipay.autotest.mobile.utils.TestFileManager;

/**
 * Verify stander use to verify {@link TestAction}
 * 
 * @author ruiqi.li
 */
public class TestVerify implements TestVerifyInterface {

	/**
	 * Verify by UI element
	 */
	public final static String VERIFY_TYPE_ELEMENT = "单元";

	/**
	 * Verify by screenshot consistency
	 */
	public final static String VERIFY_TYPE_IMAGE = "界面";

	// Original encoded verify command
	private String mOriginalCommand;

	// Decoded verify type
	private String mVerifyType;

	// Decoded verify element, plain text or image file name according to the
	// mVerifyType
	private String mVerifyElement;

	// Decoded verify parameters, may be null
	private String mVerifyParams;

	public static List<TestVerify> convertToVerifies(JSONArray verifyArray) {
		List<TestVerify> verifyList = new ArrayList<TestVerify>();
		int length = verifyArray.length();
		for (int i = 0; i < length; i++) {
			verifyList.add(convertToVerify(verifyArray.getJSONObject(i)));
		}

		return verifyList;
	}

	public static TestVerify convertToVerify(JSONObject verifyObject) {
		String type = verifyObject.getString(TYPE);
		String target = verifyObject.getString(TARGET);
		String parameter = null;
		if (verifyObject.has(PARAM)) {
			parameter = verifyObject.getString(PARAM);
		}

		return new TestVerify(type + " | " + target
				+ (parameter == null ? "" : " | " + parameter), type, target,
				parameter);
	}

	public TestVerify(String originalCommand, String decodedType,
			String decodedElement, String decodedParams) {
		mOriginalCommand = originalCommand;
		mVerifyType = decodedType;
		mVerifyElement = decodedElement;
		mVerifyParams = decodedParams;
	}

	/**
	 * @return original verify command from the test script
	 */
	public String getOriginalCommand() {
		return mOriginalCommand;
	}

	/**
	 * @return verify type decoded from the original command
	 */
	public String getVerifyType() {
		return mVerifyType;
	}

	/**
	 * @return verify element decoded from the original command
	 */
	public String getVerifyElement() {
		return mVerifyElement;
	}

	/**
	 * @return verify parameters decoded from the original command
	 */
	public String getVerifyParameter() {
		return mVerifyParams;
	}

	/**
	 * Execute verify
	 */
	public boolean execute(AppiumDriver driver, int waitSecond) {
		if (mVerifyElement == null) {
			return false;
		}

		if (TestVerify.VERIFY_TYPE_ELEMENT.equals(mVerifyType)) {
			try {
				return AppiumHelper.hasElement(driver, new TestTarget(
						TestTarget.TARGET_TYPE_NAME, mVerifyElement),
						waitSecond);
			} catch (Exception e) {
				return false;
			}
		} else if (TestVerify.VERIFY_TYPE_IMAGE.equals(mVerifyType)) {
			try {
				File currentActivity = AppiumHelper.takeTempCapture(driver);
				float percent = StringUtil.strToFloat(mVerifyParams, 0.8F);
				return ImageUtils.sameAs(TestFileManager.getInstance()
						.getVerityImageFile(mVerifyElement), currentActivity,
						percent);
			} catch (Exception e) {
				return false;
			}
		} else {
			// not support
			return false;
		}
	}
}
