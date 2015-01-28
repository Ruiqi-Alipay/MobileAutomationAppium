package com.alipay.autotest.mobile.model;

/**
 * QA interface, according to the test yaml script file
 * 
 * @author ruiqi.li
 */
public interface TestCaseInterface {
	
	public final static String CASE_CONFIG_REF = "configRef";

	/**
	 * Test case title
	 */
	public final static String CASE_TITLE = "title";

	/**
	 * Run test case parameters
	 */
	public final static String CASE_PARAMETER = "parameters";

	/**
	 * Rollback actions, use to bring the display page to the test starting
	 * page.
	 */
	public final static String CASE_ROLLBACK_ACTIONS = "rollbackActions";

	/**
	 * Action list
	 */
	public final static String CASE_ACTIONS = "actions";
	
	
	public final static String CASE_ORDER = "order";
	
	public final static String CASE_ORDER_REF = "reference";
	public final static String CASE_ORDER_AMOUNT = "amount";
	public final static String CASE_ORDER_COUPONAMOUNT = "couponAmount";
	public final static String CASE_ORDER_COUNT = "count";
}
