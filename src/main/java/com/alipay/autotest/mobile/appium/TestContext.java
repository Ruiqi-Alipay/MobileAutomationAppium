package com.alipay.autotest.mobile.appium;

import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.alipay.autotest.mobile.SystemPropoerties;
import com.alipay.autotest.mobile.model.TestAction;
import com.alipay.autotest.mobile.model.TestCase;
import com.alipay.autotest.mobile.model.TestCaseInterface;
import com.alipay.autotest.mobile.testsuites.TestCaseRunner;
import com.alipay.autotest.mobile.utils.ApkUtil;
import com.alipay.autotest.mobile.utils.CommandUtil;
import com.alipay.autotest.mobile.utils.FileUtils;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.TestFileManager;

public class TestContext {

	private static TestContext sInstance;

	private List<TestCase> mTestCaseList;
	private List<XmlSuite> mTestSuites;
	private Map<String, List<Integer>> mTestcaseByCategory;

	private Process mAppiumProcess;
	private AppiumDriver mAppiumDriver;
	private String mPlatformName;
	private String mDeviceName;
	private String mTestAppPath;
	private String mTestAppPackage;
	private List<String> mAppSrcPathList;
	private List<String> mAppEmPathList;
	private List<TestAction> mDefaultRollbackActions;
	private boolean mApiUnder17 = false;

	public class FileExtensionFilter implements FileFilter {

		private String mExtension;

		public FileExtensionFilter(String extension) {
			mExtension = extension;
		}

		public boolean accept(File pathname) {
			if (pathname.getName().endsWith(mExtension)) {
				return true;
			}
			return false;
		}

	}

	public static TestContext getInstance() {
		if (sInstance == null) {
			sInstance = new TestContext();
		}

		return sInstance;
	}

	private TestContext() {

	}

	public boolean isAndroidApiUnder17() {
		return mApiUnder17;
	}

	public boolean isTestingAndroid() {
		return SystemPropoerties.VALUE_ANDROID.equalsIgnoreCase(mPlatformName);
	}

	public List<TestAction> getDefaultRollbackActions() {
		return mDefaultRollbackActions;
	}

	public void startAppiumDriver() {
		stopAppiumDriver();

		int startTrtry = 3;
		while (startTrtry > 0) {
			try {
				if (mAppiumProcess == null) {
					switch (CommandUtil.getOperatingSystemType()) {
					case CommandUtil.WINDOWS:
						LogUtils.log("Starting server..");
						mAppiumProcess = Runtime.getRuntime().exec(
								"cmd /c appium");
						LogUtils.log("Success! server running in "
								+ mAppiumProcess.toString());
						break;
					case CommandUtil.MACOS:
						break;
					case CommandUtil.LINUX:
						break;
					default:
						throw new WebDriverException(
								"Your Operting system is not supported by this auto test framework");
					}
				}

				LogUtils.log("Preparing server..");
				Thread.sleep(8000);
				LogUtils.log("Connecting to server at //127.0.0.1:4723..");
				LogUtils.log("Caution: if a session failed to create error occurs, please try again");
				mAppiumDriver = new SelendroidDriver(new URL(
						"http://127.0.0.1:4723/wd/hub"), TestContext
						.getInstance().getConfigedCapabilicities());
				LogUtils.log("Success! server connected!");
			} catch (Exception e1) {
				if (mAppiumProcess == null) {
					LogUtils.log("Error: start appium server fialed! Error: "
							+ e1.toString());
				}
				if (mAppiumDriver == null) {
					LogUtils.log("Error: connect appium server fialed! Error: "
							+ e1.toString());
				}
			} finally {
				startTrtry--;
			}

			if (mAppiumDriver != null && mAppiumProcess != null) {
				break;
			}
		}

		if (mAppiumDriver == null || mAppiumProcess == null) {
			throw new WebDriverException("Start appium driver failed");
		}
	}

	public void stopAppiumDriver() {
		if (mAppiumProcess != null) {
			mAppiumProcess.destroy();
			mAppiumProcess = null;
		}
	}

	public void setupContext(String[] args) throws Exception {
		System.setProperty("org.uncommons.reportng.escape-output", "false");
		System.setProperty("org.uncommons.reportng.locale", "zh_CN");

		mDefaultRollbackActions = new ArrayList<TestAction>();
		File rollbackFile = new File(TestFileManager.ROOT,
				"rollback_actions.json");
		if (rollbackFile.exists()) {
			String jsonText = new String(FileUtils.readBytes(rollbackFile),
					"UTF-8");
			JSONObject scriptJson = new JSONObject(jsonText);
			mDefaultRollbackActions.addAll(TestAction
					.convertToActions(scriptJson
							.getJSONArray(TestCase.CASE_ROLLBACK_ACTIONS)));
		}

		int recursiveCount = Integer.valueOf(args[0]);
		int recursiveCombine = Integer.valueOf(args[1]);
		String buyerId = args[2];
		String amount = args[3];
		String couponAmount = args[4];

		mTestCaseList = new ArrayList<TestCase>();
		mTestSuites = new ArrayList<XmlSuite>();
		mTestcaseByCategory = new HashMap<String, List<Integer>>();

		// 2. parse the data
		mPlatformName = SystemPropoerties.VALUE_ANDROID;
		mAppSrcPathList = null;
		mAppEmPathList = null;

		// 3. find the testing app file, setup app file path and package name
		// info
		List<File> appFileList = new ArrayList<File>();
		if (SystemPropoerties.VALUE_ANDROID.equalsIgnoreCase(mPlatformName)) {
			mPlatformName = SystemPropoerties.VALUE_ANDROID;
			FileUtils.collectFiles(TestFileManager.ROOT,
					new FileExtensionFilter(".apk"), appFileList, false);
		} else if (SystemPropoerties.VALUE_IOS.equalsIgnoreCase(mPlatformName)) {
			mPlatformName = SystemPropoerties.VALUE_IOS;
			FileUtils.collectFiles(TestFileManager.ROOT,
					new FileExtensionFilter(".ipa"), appFileList, false);
		} else {
			throw new Exception("The mobile platform '" + mPlatformName
					+ "' is not supported at this time!");
		}

		if (appFileList.size() != 1) {
			throw new Exception(
					"Please make sure test resource directory has only one android and ios app file each");
		}

		mTestAppPath = appFileList.get(0).getAbsolutePath();
		// TODO: support ios
		mTestAppPackage = ApkUtil.getPackageName(mTestAppPath);

		// 4. setup test case from test case script file
		List<File> testcaseList = new ArrayList<File>();
		FileUtils.collectFiles(TestFileManager.TEST_CASE_DIR,
				new FileExtensionFilter(".json"), testcaseList, true);
		for (File testcaseFile : testcaseList) {
			String jsonText = new String(FileUtils.readBytes(testcaseFile),
					"UTF-8");
			JSONObject scriptJson = new JSONObject(jsonText);
			String recursiveParam = scriptJson
					.getString(TestCaseInterface.CASE_RECUR_NAME);

			for (int i = 0; i < recursiveCount; i++) {
				mTestCaseList.add(TestCase.parseJSON(scriptJson,
						recursiveParam, buyerId, recursiveCombine, amount,
						couponAmount));
			}
		}

		// 5. setup test suites, group by test case category
		Map<String, XmlSuite> suiteByCategory = new HashMap<String, XmlSuite>();
		int testcaseSize = mTestCaseList.size();
		for (int i = 0; i < testcaseSize; i++) {
			TestCase testCase = mTestCaseList.get(i);
			String testCaseCategory = testCase.getCategory();
			XmlSuite suite = suiteByCategory.get(testCaseCategory);

			if (suite == null) {
				suite = new XmlSuite();
				suite.setName(testCaseCategory);
				suiteByCategory.put(testCaseCategory, suite);
				mTestSuites.add(suite);
			}

			XmlTest test = new XmlTest(suite);
			test.setName(testCase.getCategory() + " " + i);
			List<XmlClass> classes = new ArrayList<XmlClass>();
			classes.add(new XmlClass(TestCaseRunner.class));
			test.setXmlClasses(classes);
			test.addParameter("testcaseIndex", String.valueOf(i));
			test.addParameter("testcaseTotal", String.valueOf(testcaseSize));
			test.addParameter("testsuitCategory", testCaseCategory);

			List<Integer> caseIndexList = mTestcaseByCategory
					.get(testCaseCategory);
			if (caseIndexList == null) {
				caseIndexList = new ArrayList<Integer>();
				mTestcaseByCategory.put(testCaseCategory, caseIndexList);
			}
			caseIndexList.add(i);
		}

		if ("ANDROID".equals(mPlatformName)) {
			// XmlSuite suite = new XmlSuite();
			// suite.setName("鎬讳綋浠ｇ爜瑕嗙洊鐜囨姤鍛�");
			// mTestSuites.add(suite);
			// XmlTest test = new XmlTest(suite);
			// test.setName("鎬讳綋浠ｇ爜瑕嗙洊鐜囨姤鍛�");
			// List<XmlClass> classes = new ArrayList<XmlClass>();
			// classes.add(new XmlClass(CoverageRunner.class));
			// test.setXmlClasses(classes);
		}

		if (mTestSuites.isEmpty()) {
			throw new Exception("None test case has been found!");
		}
	}

	public void clearContext() {
		if (mTestSuites != null) {
			mTestSuites.clear();
		}
		if (mTestCaseList != null) {
			mTestCaseList.clear();
		}
		if (mTestCaseList != null) {
			mTestCaseList.clear();
		}
	}

	public DesiredCapabilities getConfigedCapabilicities() {
		String version = CommandUtil
				.execCmd("adb shell getprop ro.build.version.release");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
		capabilities.setCapability("platformName", mPlatformName);
		capabilities.setCapability("deviceName", mDeviceName);
		capabilities.setCapability("platformVersion", version);
		capabilities.setCapability("newCommandTimeout", 60);
		capabilities.setCapability("app", mTestAppPath);
		capabilities.setCapability("autoLaunch", true);
		capabilities.setCapability("orientation",
				SystemPropoerties.VALUE_PORTRAIT);

		if (Double.valueOf(version.substring(0, 3)) < 4.3) {
			mApiUnder17 = true;
			LogUtils.log("Device API under 17 change to use Selendroid");
			capabilities.setCapability("automationName", "Selendroid");
		} else {
			mApiUnder17 = false;
		}

		capabilities.setCapability("noSign", true);
		capabilities.setCapability("noReset", false);
		capabilities.setCapability("unicodeKeyboard", false);

		// capabilities.setCapability("androidCoverage",
		// AppiumConstant.COVERAGE_INSTRUMENTATION);

		return capabilities;
	}

	public List<String> getAppSrcPathList() {
		return mAppSrcPathList;
	}

	public List<String> getAppEmPathList() {
		return mAppEmPathList;
	}

	public AppiumDriver getDriver() {
		return mAppiumDriver;
	}

	public String getTestAppPath() {
		return mTestAppPath;
	}

	public String getTestAppPackage() {
		return mTestAppPackage;
	}

	public List<Integer> getTestcaseByCategory(String title) {
		return mTestcaseByCategory.get(title);
	}

	public List<XmlSuite> getTestSuites() {
		return mTestSuites;
	}

	public TestCase getTestcase(int index) {
		return mTestCaseList.get(index);
	}

}
