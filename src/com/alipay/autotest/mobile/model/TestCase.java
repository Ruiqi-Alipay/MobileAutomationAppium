/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alipay.autotest.mobile.utils.DataBankComUtils;

/**
 * Single test case, represented by each yaml file in the testcase folder
 * 
 * @author ruiqi.li
 */
public class TestCase implements TestCaseInterface {

	// Test case description, extract from yaml script
	private String mName;

	// Test case parameter, extract from yaml script
	private Map<String, String> mParamsMap;

	// Test case command queue, extract from yaml script, will be execute one by
	// one starting from the first item in the array
	private List<TestAction> mActions;

	private List<TestAction> mRollbackActions;

	private String mOrderRef;
	private String mBuyerId;
	private int mCombineCount;
	private String mAmount;
	private String mCouponAmount;
	private String mConfigRef;

	public static TestCase parseJSON(JSONObject scriptJson, String buyerId,
			Map<String, String> serverparams) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (scriptJson.has(CASE_PARAMETER)) {
			JSONArray originalParamArray = scriptJson
					.getJSONArray(CASE_PARAMETER);
			int length = originalParamArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject item = originalParamArray.getJSONObject(i);
				paramsMap.put(item.getString("name"), item.getString("value"));
			}
		}

		String name = scriptJson.getString(CASE_TITLE);
		if (paramsMap.containsKey(name)) {
			name = paramsMap.get(name);
		}

		String configRef = null;
		if (scriptJson.has(CASE_CONFIG_REF)) {
			configRef = scriptJson.getString(CASE_CONFIG_REF);
		}

		String orderRef = null;
		int count = 1;
		String amount = null;
		String couponAmount = null;
		if (scriptJson.has(CASE_ORDER)) {
			JSONObject order = scriptJson.getJSONObject(CASE_ORDER);
			if (order.has(CASE_ORDER_REF)) {
				orderRef = String.valueOf(order.get(CASE_ORDER_REF));
			}

			if (order.has(CASE_ORDER_AMOUNT)) {
				amount = String.valueOf(order.get(CASE_ORDER_AMOUNT));
			}

			if (order.has(CASE_ORDER_COUPONAMOUNT)) {
				couponAmount = String.valueOf(order
						.get(CASE_ORDER_COUPONAMOUNT));
			}

			if (order.has(CASE_ORDER_COUNT)) {
				count = order.getInt(CASE_ORDER_COUNT);
			}
		}

		List<TestAction> rollbackActions = null;
		if (scriptJson.has(CASE_ROLLBACK_ACTIONS)) {
			rollbackActions = TestAction.convertToActions(
					scriptJson.getJSONArray(CASE_ROLLBACK_ACTIONS),
					serverparams);
		}

		List<TestAction> actionList = null;
		if (scriptJson.has(CASE_ACTIONS)) {
			actionList = TestAction.convertToActions(
					scriptJson.getJSONArray(CASE_ACTIONS), serverparams);
		}

		return new TestCase(name, paramsMap, actionList, rollbackActions,
				orderRef, buyerId, count, amount, couponAmount, configRef);
	}

	public TestCase(String name, Map<String, String> paramsMap,
			List<TestAction> actions, List<TestAction> rollbackActions,
			String orderRef, String buyerId, int count, String amount,
			String couponAmount, String configRef) {
		mName = name;
		mParamsMap = paramsMap;
		mActions = actions;
		mRollbackActions = rollbackActions;
		mOrderRef = orderRef;
		mBuyerId = buyerId;
		mCombineCount = count;
		mAmount = amount;
		mCouponAmount = couponAmount;
		mConfigRef = configRef;
	}

	public void prepareRecursiveData() {
		if (mOrderRef == null) {
			return;
		}

		Random random = new Random();
		StringBuilder recursiveParamBuilder = new StringBuilder();
		for (int i = 0; i < mCombineCount; i++) {
			String amount = mAmount;
			if (amount == null) {
				amount = String.valueOf(random.nextInt(100) + 1);
			}

			String couponAmount = mCouponAmount;
			if (couponAmount == null) {
				couponAmount = String.valueOf(random.nextInt(100) + 1);
			}

			recursiveParamBuilder.append(DataBankComUtils.creatNewIpayTradeNo(
					mBuyerId, amount, couponAmount));
			if (i != mCombineCount - 1) {
				recursiveParamBuilder.append(",");
			}
		}

		mParamsMap.put(mOrderRef, recursiveParamBuilder.toString());
	}

	public String getRecursivePram() {
		return mOrderRef;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getAmount() {
		return mAmount;
	}

	public String getCouponAmount() {
		return mCouponAmount;
	}

	public String getConfigRef() {
		return mConfigRef;
	}

	public List<TestAction> getRollbackActions() {
		return mRollbackActions;
	}

	/**
	 * @return test case name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return test case parameter
	 */
	public String getParameter(String key) {
		return mParamsMap.get(key);
	}

	/**
	 * @return action list
	 */
	public List<TestAction> getActions() {
		return mActions;
	}

}
