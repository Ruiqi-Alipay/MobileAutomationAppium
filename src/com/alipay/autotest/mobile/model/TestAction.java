/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.model;

import io.appium.java_client.android.AndroidKeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alipay.autotest.mobile.utils.TextUtils;

/**
 * Simulated user interaction.
 * 
 * @author ruiqi.li
 */
public class TestAction implements TestActionInterface {

	// original encoded action command
	private String mOriginalCommand;

	// action type
	private String mType;

	// action parameters
	private String mParams;

	// action target
	private TestTarget mTarget;

	public static List<TestAction> convertToActions(JSONArray actionArray,
			Map<String, String> serverPramMap) throws Exception {
		List<TestAction> actionList = new ArrayList<TestAction>();
		if (actionArray != null) {
			int length = actionArray.length();
			for (int i = 0; i < length; i++) {
				actionList.add(convertToAction(actionArray.getJSONObject(i),
						serverPramMap));
			}
		}

		return actionList;
	}

	public static TestAction convertToAction(JSONObject actionObject,
			Map<String, String> serverPramMap) throws Exception {
		String actionType = actionObject.getString(TYPE);
		String actionParams = null;
		String textTarget = null;
		TestTarget target = null;

		if (TestActionTypes.ACTION_TYPE_KEYEVENT.equals(actionType)) {
			actionParams = String.valueOf(textKeyToKeyCode(actionObject
					.getString(TARGET)));
		} else {
			if (actionObject.has(PARAM)) {
				actionParams = actionObject.getString(PARAM);
				actionParams = processGloblePram(actionParams, serverPramMap);
			}
			if (actionObject.has(TARGET)) {
				textTarget = actionObject.getString(TARGET);
				textTarget = processGloblePram(textTarget, serverPramMap);
				if (!TextUtils.isEmpty(textTarget)) {
					target = new TestTarget(textTarget);
				}
			}
		}

		return new TestAction(actionType
				+ (actionParams == null ? "" : " | " + actionParams)
				+ (textTarget == null ? "" : " | " + textTarget), actionType,
				actionParams, target);
	}

	public TestAction(String originalCommand, String type, String param,
			TestTarget target) {
		mOriginalCommand = originalCommand;
		mType = type;
		mParams = param;
		mTarget = target;
	}

	/**
	 * @return the action original command in yaml script
	 */
	public String getOriginalCommand() {
		return mOriginalCommand;
	}

	/**
	 * @return action target widget
	 */
	public TestTarget getTestTarget() {
		return mTarget;
	}

	/**
	 * @return action type
	 */
	public String getType() {
		return mType;
	}

	public void setParams(String params) {
		mParams = params;
	}

	/**
	 * @return action parameters
	 */
	public String getParams() {
		return mParams;
	}
	
	public static String processGloblePram(String param, Map<String, String> serverPramMap) {
		if (param.indexOf("{") == 0
				&& param.indexOf("}") == (param.length() - 1)) {
			String paramKey = param.substring(1, param.length() - 1);
			if (serverPramMap.containsKey(paramKey)) {
				return serverPramMap.get(paramKey);
			}
		}
		
		return param;
	}

	private static int textKeyToKeyCode(String key) throws Exception {
		if (KeyEventInterface.BACK.equals(key)) {
			return AndroidKeyCode.BACK;
		} else if (KeyEventInterface.HOME.equals(key)) {
			return AndroidKeyCode.HOME;
		} else {
			throw new Exception("Key event type '" + key + "' not support!");
		}
	}
}
