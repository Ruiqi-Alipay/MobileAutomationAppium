package com.alipay.autotest.mobile.testsuites;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.alipay.autotest.mobile.appium.AppiumHelper;
import com.alipay.autotest.mobile.appium.LocalAndroidDriver;
import com.alipay.autotest.mobile.appium.TestContext;
import com.alipay.autotest.mobile.model.TestAction;
import com.alipay.autotest.mobile.model.TestActionTypes;
import com.alipay.autotest.mobile.model.TestCase;
import com.alipay.autotest.mobile.monitor.MonitorFactory;
import com.alipay.autotest.mobile.monitor.MonitorInterface;
import com.alipay.autotest.mobile.monitor.ReportHelper;
import com.alipay.autotest.mobile.utils.AliElementNotFoundException;
import com.alipay.autotest.mobile.utils.FileNameUtil;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.TestFileManager;

/**
 * Order test script runner
 * 
 * @author ruiqi.li
 */
public class TestCaseRunner {

	private static final int ACTION_RETRY = 2;

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite(ITestContext context) throws Exception {

	}

	@Test
	@Parameters({ "testcaseIndex", "testcaseTotal", "testsuitCategory" })
	public void test(String testcaseIndex, String testcaseTotal,
			String testsuitCategory) throws InterruptedException {
		int caseIndex = Integer.valueOf(testcaseIndex);
		LogUtils.log("====================================================================");
		LogUtils.log(">>>>>>>>>>>>>>>>>>>>>>>>  " + testsuitCategory + "  "
				+ (caseIndex + 1) + "/" + testcaseTotal
				+ "  <<<<<<<<<<<<<<<<<<<<<<<<");
		TestCase testCase = TestContext.getInstance().getTestcase(
				testsuitCategory, caseIndex);
		LogUtils.log("Creating trade ID..");
		testCase.prepareRecursiveData();
		LogUtils.log("Creat trade ID finished");

		boolean result = true;
		AppiumDriver driver = TestContext.getInstance().getDriver();

		File captureDir = TestFileManager.REPORT_ROOT;
		if (!captureDir.exists()) {
			captureDir.mkdirs();
		}

		dot();

		String message = "启动:" + TestContext.getInstance().getTestAppPath();
		File captureImage = AppiumHelper.takeCaptureToDir(driver, captureDir,
				"启动界面.png");
		ReportHelper.recordAction(message, captureImage, true);

		if (caseIndex == 0) {
			TestCase systemConfig = TestContext.getInstance().getSystemConfig();
			if (systemConfig != null) {
				runActions(systemConfig.getActions(), systemConfig, caseIndex,
						captureDir, driver);
			}
			if (testCase.getConfigRef() != null) {
				TestCase configScript = TestContext.getInstance()
						.getConfigScript(testCase.getConfigRef());
				if (configScript != null) {
					runActions(configScript.getActions(), configScript,
							caseIndex, captureDir, driver);
				}
			}
		}
		result = runActions(testCase.getActions(), testCase, caseIndex,
				captureDir, driver);

		if (!result) {
			Assert.fail("判断结果时出现错误");
		}
	}

	private boolean runActions(List<TestAction> actions, TestCase testCase,
			int caseIndex, File captureDir, AppiumDriver driver) {
		if (actions == null) {
			return true;
		}

		TestAction lastAction = null;
		MonitorInterface monitor = MonitorFactory.getInstance(TestContext
				.getInstance().getPlatformName());

		int i = 0;
		for (TestAction action : actions) {
			String originalAction = action.getOriginalCommand();
			try {
				monitor.recordActionStart(caseIndex, originalAction);
				dot();

				for (int retry = 1; retry <= ACTION_RETRY; retry++) {
					try {
						configActionParameters(action, testCase);
						AppiumHelper.performAction(driver, action,
								AppiumHelper.DEFAULT_WAIT_ELEMENT_SECOND);
						File captureImage = AppiumHelper.takeCaptureToDir(
								driver,
								captureDir,
								(++i)
										+ FileNameUtil
												.convertStringToFileName(action
														.getOriginalCommand())
										+ ".png");
						ReportHelper.recordAction(
								getActionRecordText(
										action.getOriginalCommand(), true),
								captureImage, true);
						lastAction = action;
						break;
					} catch (AliElementNotFoundException noneElement) {
						LogUtils.log("Element not found: "
								+ noneElement.getMessage());
						if (lastAction != null && retry != ACTION_RETRY) {
							try {
								LogUtils.log("Retry last action (" + retry
										+ ") : "
										+ lastAction.getOriginalCommand());
								AppiumHelper.performAction(driver, lastAction,
										1);
							} catch (Exception e) {
								LogUtils.log("Error: Retry last action failed!");
								throw noneElement;
							}
						} else {
							throw noneElement;
						}
					} catch (WebDriverException webException) {
						LogUtils.log("WebDriver Error: "
								+ webException.getMessage());
						if (retry == ACTION_RETRY) {
							throw webException;
						} else {
							LogUtils.log("WebDriver Error: retry " + retry);
							dot();
							continue;
						}
					}
				}
			} catch (Exception e) {
				LogUtils.log(e.toString());
				File captureImage = AppiumHelper.takeCaptureToDir(
						driver,
						captureDir,
						FileNameUtil.convertStringToFileName(action
								.getOriginalCommand()) + ".png");
				String failMessage = getActionRecordText(
						action.getOriginalCommand(), false);
				ReportHelper.recordAction(failMessage, captureImage, false);
				Assert.fail(failMessage, e);
				return false;
			} finally {
				monitor.recordActionEnd(caseIndex, originalAction);
			}
		}

		return true;
	}

	@AfterMethod
	public void afterMethod(ITestResult result) throws Exception {
		Reporter.setCurrentTestResult(result);
	}

	@AfterTest
	public void afterTest(ITestContext context) throws Exception {
		String category = context.getCurrentXmlTest().getParameter(
				"testsuitCategory");
		String indexText = context.getCurrentXmlTest().getParameter(
				"testcaseIndex");
		int caseIndex = Integer.parseInt(indexText);
		TestCase testCase = TestContext.getInstance().getTestcase(category,
				caseIndex);
		backToHomePage(caseIndex, testCase);
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {

	}

	private void backToHomePage(int caseIndex, TestCase testCase) {
		AppiumDriver driver = TestContext.getInstance().getDriver();
		List<TestAction> robackActions = new ArrayList<TestAction>();
		List<TestAction> robackVerifies = new ArrayList<TestAction>();
		List<TestAction> rollbackActions = testCase.getRollbackActions();

		for (TestAction action : rollbackActions) {
			if (action.getType()
					.equals(TestActionTypes.ACTION_TYPE_TEST_VERIFY)
					|| action.getType().equals(
							TestActionTypes.ACTION_TYPE_PIXEL_VERIFY)) {
				robackVerifies.add(action);
			} else {
				robackActions.add(action);
			}
		}

		robackActions.addAll(TestContext.getInstance()
				.getDefaultRollbackActions());

		for (int backToHomePages = 1; backToHomePages <= 2; backToHomePages++) {
			for (TestAction action : robackActions) {
				try {
					dot();
					AppiumHelper.performAction(driver, action,
							AppiumHelper.DEFAULT_ROLLBACK_WAIT_SECOND);
				} catch (Exception e) {

				}

				boolean success = true;
				for (TestAction verify : robackVerifies) {
					try {
						dot();
						AppiumHelper.performAction(driver, verify,
								AppiumHelper.DEFAULT_ROLLBACK_WAIT_SECOND);
					} catch (Exception e) {
						success = false;
						break;
					}
				}

				if (success) {
					return;
				}
			}
		}

		if (driver instanceof LocalAndroidDriver) {
			((LocalAndroidDriver) driver).resetApp();
		} else {
			((IOSDriver) driver).resetApp();
		}
	}

	private void configActionParameters(TestAction action, TestCase testcase) {
		String paramPlaceHold = action.getParams();
		String paramValue = testcase.getParameter(paramPlaceHold);
		if (paramValue != null) {
			action.setParams(paramValue);
		}
	}

	private String getVerifyRecordText(String message, boolean success) {
		return "判断结果 '" + message + (success ? "' 正确\n" : "' 时出现错误\n");
	}

	private String getActionRecordText(String message, boolean success) {
		return "执行 '" + message + (success ? "' 成功" : "' 时出现错误\n");
	}

	private void dot() throws InterruptedException {
		Thread.sleep(300);
	}

}
