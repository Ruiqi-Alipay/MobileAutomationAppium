package com.alipay.autotest.mobile.appium;

import io.appium.java_client.AppiumDriver;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
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
import com.alipay.autotest.mobile.testsuites.TestCaseRunner;
import com.alipay.autotest.mobile.utils.ApkUtil;
import com.alipay.autotest.mobile.utils.CommandUtil;
import com.alipay.autotest.mobile.utils.HttpUtils;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.TestFileManager;

public class TestContext {

	private static TestContext sInstance;

	private List<XmlSuite> mTestSuites;
	private Map<String, List<TestCase>> mTestcaseByCategory;
	private Map<String, TestCase> mConfigScriptMap;

	private AppiumDriver mAppiumDriver;
	private String mPlatformName;
	private String mDeviceName;
	private File mTestApp;
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
		return true;// SystemPropoerties.VALUE_ANDROID.equalsIgnoreCase(mPlatformName);
	}

	public List<TestAction> getDefaultRollbackActions() {
		return mDefaultRollbackActions;
	}

	public void startAppiumDriver() {
		stopAppiumDriver();

		int startTrtry = 3;
		while (startTrtry > 0) {
			try {
				LogUtils.log("Connecting to server at //127.0.0.1:4723..");
				LogUtils.log("Caution: if a session failed to create error occurs, please try again");
				mAppiumDriver = new SelendroidDriver(new URL(
						"http://127.0.0.1:4723/wd/hub"), TestContext
						.getInstance().getConfigedCapabilicities());
				LogUtils.log("Success! server connected!");
				mTestApp.delete();
			} catch (Exception e1) {
				if (mAppiumDriver == null) {
					LogUtils.log("Error: connect appium server fialed! Error: "
							+ e1.toString());
				}
			} finally {
				startTrtry--;
			}

			if (mAppiumDriver != null) {
				break;
			}
		}

		if (mAppiumDriver == null) {
			throw new WebDriverException("Start appium driver failed");
		}
	}

	public void stopAppiumDriver() {
		if (mAppiumDriver != null) {
			mAppiumDriver.close();
			mAppiumDriver = null;
		}
	}

	public void setupContext() throws Exception {
		System.setProperty("org.uncommons.reportng.escape-output", "false");
		System.setProperty("org.uncommons.reportng.locale", "zh_CN");

		List<String> scriptIds = null;
		int runtimes = 1;
		String accountNum = null;
		Scanner scanner = null;
		String appPath = null;
		try {
			scanner = new Scanner(System.in);
			appPath = userSelectApp(scanner);
			scriptIds = userSelectScript(scanner);
			runtimes = userEnterRuntimes(scanner);
			accountNum = userEnterAccount(scanner);
		} finally {
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
		}

		String fileName = appPath.substring(appPath.lastIndexOf("/") + 1);
		LogUtils.log("Downloading " + fileName);
		mTestApp = new File(TestFileManager.ENVIRONMENT_ROOT, fileName);
		org.apache.commons.io.FileUtils.copyURLToFile(new URL(
				"http://autotest.d10970aqcn.alipay.net/" + appPath), mTestApp);
		// TODO: support ios
		mTestAppPackage = ApkUtil.getPackageName(mTestApp.getAbsolutePath());

		JSONObject responseRaw = fetchScriptFromServer(scriptIds);
		JSONArray runScripts = responseRaw.getJSONArray("scripts");
		JSONArray serverParameters = responseRaw.getJSONArray("params");
		Map<String, String> serverParamMap = new HashMap<String, String>();
		for (int i = 0; i < serverParameters.length(); i++) {
			JSONObject paramObject = serverParameters.getJSONObject(i);
			serverParamMap.put(paramObject.getString("name"),
					paramObject.getString("value"));
		}

		mConfigScriptMap = new HashMap<String, TestCase>();
		mDefaultRollbackActions = new ArrayList<TestAction>();
		JSONArray configs = responseRaw.getJSONArray("configs");
		if (configs != null) {
			for (int i = 0; i < configs.length(); i++) {
				JSONObject config = configs.getJSONObject(i);
				TestCase testCase = TestCase.parseJSON(config, null,
						serverParamMap);
				if (testCase.getName().equals("ROLLBACK_ACTIONS")) {
					mDefaultRollbackActions.addAll(testCase.getActions());
				} else {
					mConfigScriptMap.put(config.getString("id"), testCase);
				}
			}
		}

		// 2. parse the data
		mPlatformName = SystemPropoerties.VALUE_ANDROID;

		mTestSuites = new ArrayList<XmlSuite>();
		mTestcaseByCategory = new HashMap<String, List<TestCase>>();
		for (int i = 0; i < runScripts.length(); i++) {
			JSONObject scriptJson = runScripts.getJSONObject(i);
			String caseTitle = scriptJson.getString(TestCase.CASE_TITLE);

			XmlSuite suite = new XmlSuite();
			suite.setName(caseTitle);
			mTestcaseByCategory.put(caseTitle, new ArrayList<TestCase>());
			mTestSuites.add(suite);

			for (int j = 0; j < runtimes; j++) {
				TestCase testCase = TestCase.parseJSON(scriptJson, accountNum,
						serverParamMap);

				XmlTest test = new XmlTest(suite);
				test.setName(caseTitle + "-" + j);
				List<XmlClass> classes = new ArrayList<XmlClass>();
				classes.add(new XmlClass(TestCaseRunner.class));
				test.setXmlClasses(classes);
				test.addParameter("testcaseIndex", String.valueOf(j));
				test.addParameter("testcaseTotal", String.valueOf(runtimes));
				test.addParameter("testsuitCategory", caseTitle);

				mTestcaseByCategory.get(caseTitle).add(testCase);
			}
		}

		if (mTestSuites.isEmpty()) {
			throw new Exception("None test case has been found!");
		}
	}

	public void clearContext() {
		if (mTestSuites != null) {
			mTestSuites.clear();
		}
		if (mTestcaseByCategory != null) {
			mTestcaseByCategory.clear();
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
		capabilities.setCapability("app", mTestApp.getAbsolutePath());
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

	public TestCase getConfigScript(String id) {
		return mConfigScriptMap.get(id);
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
		return mTestApp.getAbsolutePath();
	}

	public String getTestAppPackage() {
		return mTestAppPackage;
	}

	public List<XmlSuite> getTestSuites() {
		return mTestSuites;
	}

	public TestCase getTestcase(String cagetory, int index) {
		return mTestcaseByCategory.get(cagetory).get(index);
	}

	private String userSelectApp(Scanner scanner) {
		LogUtils.log("Loading server target apps...");
		String responseText = HttpUtils.sendGet(
				"http://autotest.d10970aqcn.alipay.net/autotest/api/testapp",
				null);
		JSONArray apps = new JSONArray(responseText);
		for (int i = 0; i < apps.length(); i++) {
			JSONObject app = apps.getJSONObject(i);
			LogUtils.log((i + 1) + ".  " + app.getString("name") + " ("
					+ app.getString("description") + ")");
		}

		while (true) {
			LogUtils.log("Which app you want to test(enter the app index):");

			String userInput = scanner.nextLine();
			try {
				int index = Integer.valueOf(userInput.trim()) - 1;
				return apps.getJSONObject(index).getString("path");
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private List<String> userSelectScript(Scanner scanner) {
		LogUtils.log("Loading server scripts...");
		String responseText = HttpUtils
				.sendGet(
						"http://autotest.d10970aqcn.alipay.net/autotest/api/scriptlist",
						null);
		JSONArray scripts = new JSONArray(responseText);
		for (int i = 0; i < scripts.length(); i++) {
			JSONObject script = scripts.getJSONObject(i);
			LogUtils.log(script.getString("key") + "  "
					+ script.getString("title"));
		}

		while (true) {
			List<String> results = new ArrayList<String>();
			LogUtils.log("Please enter the script serial number you want to run (split by space):");

			String scriptNumberString = scanner.nextLine();
			if (scriptNumberString != null) {
				String[] rawNumbers = scriptNumberString.split(" ");
				for (String number : rawNumbers) {
					results.addAll(parseScriptId(scripts, number.trim()));
				}
			}

			if (!results.isEmpty()) {
				return results;
			}
		}
	}

	private int userEnterRuntimes(Scanner scanner) {
		while (true) {
			LogUtils.log("How many times you want to run (default: 10):");
			String rawText = scanner.nextLine();
			try {
				if (rawText == null || rawText.length() == 0) {
					rawText = "10";
				}

				int times = Integer.valueOf(rawText.trim());
				return times;
			} catch (Exception e) {

			}
		}
	}

	private String userEnterAccount(Scanner scanner) {
		while (true) {
			LogUtils.log("The account use to create order (default: 2188205012137435):");
			String rawText = scanner.nextLine();
			try {
				if (rawText == null || rawText.length() == 0) {
					rawText = "2188205012137435";
				}

				return rawText;
			} catch (Exception e) {

			}
		}
	}

	private JSONObject fetchScriptFromServer(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		for (String id : ids) {
			builder.append(id).append(",");
		}
		builder.deleteCharAt(builder.length() - 1);
		String responseText = HttpUtils
				.sendGet(
						"http://autotest.d10970aqcn.alipay.net/autotest/api/getscripts",
						"ids=" + builder.toString());
		return new JSONObject(responseText);
	}

	private List<String> parseScriptId(JSONArray scripts, String findKey) {
		List<String> results = new ArrayList<String>();
		for (int i = 0; i < scripts.length(); i++) {
			JSONObject script = scripts.getJSONObject(i);
			String key = script.getString("key");
			if (key.equals(findKey)) {
				if (script.has("id")) {
					results.add(script.getString("id"));
				} else if (key.indexOf(".") < 0) {
					int index = 0;
					while (true) {
						index++;
						JSONObject item = finScript(scripts, key + "." + index);
						if (item == null) {
							break;
						}
						results.add(item.getString("id"));
					}
				}
			}
		}

		return results;
	}

	private JSONObject finScript(JSONArray scripts, String findKey) {
		for (int i = 0; i < scripts.length(); i++) {
			JSONObject script = scripts.getJSONObject(i);
			if (script.get("key").equals(findKey)) {
				return script;
			}
		}

		return null;
	}
}
