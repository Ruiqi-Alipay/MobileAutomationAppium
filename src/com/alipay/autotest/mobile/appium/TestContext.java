package com.alipay.autotest.mobile.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

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
import org.uiautomation.ios.IOSCapabilities;

import com.alipay.autotest.mobile.SystemPropoerties;
import com.alipay.autotest.mobile.model.TestAction;
import com.alipay.autotest.mobile.model.TestCase;
import com.alipay.autotest.mobile.testsuites.TestCaseRunner;
import com.alipay.autotest.mobile.utils.ApkUtil;
import com.alipay.autotest.mobile.utils.CommandUtil;
import com.alipay.autotest.mobile.utils.GZipUtils;
import com.alipay.autotest.mobile.utils.HttpUtils;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.TestFileManager;

public class TestContext {

	private static TestContext sInstance;

	private List<XmlSuite> mTestSuites;
	private Map<String, List<TestCase>> mTestcaseByCategory;
	private Map<String, TestCase> mConfigScriptMap;

	private Process mAppiumProcess;
	private AppiumDriver mWebDriver;
	private String mPlatformName;
	private String mIOSVersion;
	private String mIOSDevice;
	private File mTestApp;
	private String mTestAppPackage;
	private List<String> mAppSrcPathList;
	private List<String> mAppEmPathList;
	private List<TestAction> mDefaultRollbackActions;
	private boolean mApiUnder17 = false;
	private TestCase mSystemtConfig;

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

	public String getPlatformName() {
		return mPlatformName;
	}

	public List<TestAction> getDefaultRollbackActions() {
		return mDefaultRollbackActions;
	}

	public TestCase getSystemConfig() {
		return mSystemtConfig;
	}

	public void startAppiumDriver() {
		stopAppiumDriver();

		int startTrtry = 3;
		while (startTrtry > 0) {
			try {
				logDivilver();
				if (mAppiumProcess == null) {
					LogUtils.log("Starting selenium server...");
					if (CommandUtil.getOperatingSystemType() == CommandUtil.WINDOWS) {
						mAppiumProcess = Runtime.getRuntime().exec(
								TestFileManager.ENVIRONMENT_ROOT
										+ "/nodejs/appium.cmd");
					} else {
						String command = "java -jar "
								+ TestFileManager.ENVIRONMENT_ROOT
								+ "/ios-server-0.6.6.jar -aut "
								+ TestContext.getInstance().getTestAppPath()
								+ " -port 5555";
						mAppiumProcess = Runtime.getRuntime().exec(command);
					}
					Thread.sleep(8000);
				}
				LogUtils.log("Connecting to server...");
				LogUtils.log("Caution: if a session failed to create error occurs, please try again");

				if (TestContext.getInstance().isTestingAndroid()) {
					mWebDriver = new LocalAndroidDriver(new URL(
							"http://127.0.0.1:4723/wd/hub"),
							getCapabilicities());
				} else {
					mWebDriver = new IOSDriver(new URL(
							"http://127.0.0.1:5555/wd/hub"),
							getCapabilicities());
				}

				LogUtils.log("Success! server connected!");
			} catch (Exception e1) {
				if (mWebDriver == null) {
					LogUtils.log("Error: connect appium server fialed! Error: "
							+ e1.toString());
				}
			} finally {
				startTrtry--;
			}

			if (mWebDriver != null) {
				mTestApp.delete();
				break;
			}
		}

		if (mWebDriver == null) {
			throw new WebDriverException("Start appium driver failed");
		}
	}

	public void stopAppiumDriver() {
		if (mAppiumProcess != null) {
			mAppiumProcess.destroy();
			mAppiumProcess = null;
		}
		if (mWebDriver != null) {
			// mAppiumDriver.close();
			mWebDriver = null;
		}
	}

	public void setupContext() throws Exception {
		System.setProperty("org.uncommons.reportng.escape-output", "false");
		System.setProperty("org.uncommons.reportng.locale", "zh_CN");

		List<String> scriptIds = null;
		JSONObject systemConfig = null;
		int runtimes = 1;
		Scanner scanner = null;
		String appPath = null;
		try {
			scanner = new Scanner(System.in);
			logDivilver();
			mPlatformName = userSelectPlatform(scanner);
			if (SystemPropoerties.VALUE_IOS.equals(mPlatformName)) {
				logDivilver();
				mIOSDevice = userSelectIOSDevice(scanner);
				logDivilver();
				mIOSVersion = userSelectIOSVersion(scanner);
			}
			logDivilver();
			appPath = userSelectApp(scanner, mPlatformName);
			logDivilver();
			systemConfig = userSelectConfig(scanner);
			logDivilver();
			scriptIds = userSelectScript(scanner);
			logDivilver();
			runtimes = userEnterRuntimes(scanner);
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
		if (SystemPropoerties.VALUE_ANDROID.equals(mPlatformName)) {
			mTestAppPackage = ApkUtil
					.getPackageName(mTestApp.getAbsolutePath());
		} else {
			String unzipPath = mTestApp.getAbsolutePath();
			String zipedPath = unzipPath.substring(0, unzipPath.indexOf(".zip"));
			GZipUtils.unZip(mTestApp, TestFileManager.ENVIRONMENT_ROOT);
			mTestApp = new File(zipedPath);
		}

		JSONObject responseRaw = fetchScriptFromServer(scriptIds);
		JSONArray runScripts = responseRaw.getJSONArray("scripts");
		JSONArray serverParameters = responseRaw.getJSONArray("params");
		Map<String, String> serverParamMap = new HashMap<String, String>();
		for (int i = 0; i < serverParameters.length(); i++) {
			JSONObject paramObject = serverParameters.getJSONObject(i);
			serverParamMap.put(paramObject.getString("name"),
					paramObject.getString("value"));
		}

		if (systemConfig != null) {
			mSystemtConfig = TestCase.parseJSON(systemConfig, serverParamMap);
		}

		mConfigScriptMap = new HashMap<String, TestCase>();
		mDefaultRollbackActions = new ArrayList<TestAction>();
		JSONArray configs = responseRaw.getJSONArray("configs");
		if (configs != null) {
			for (int i = 0; i < configs.length(); i++) {
				JSONObject config = configs.getJSONObject(i);
				TestCase testCase = TestCase.parseJSON(config, serverParamMap);
				if (testCase.getName().equals("ROLLBACK_ACTIONS")) {
					mDefaultRollbackActions.addAll(testCase.getActions());
				} else {
					mConfigScriptMap.put(config.getString("id"), testCase);
				}
			}
		}

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
				TestCase testCase = TestCase.parseJSON(scriptJson,
						serverParamMap);

				XmlTest test = new XmlTest(suite);
				test.setName(caseTitle + "-" + (j + 1));
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

	public DesiredCapabilities getCapabilicities() {
		if (SystemPropoerties.VALUE_ANDROID.equals(mPlatformName)) {
			DesiredCapabilities capabilities = new DesiredCapabilities();
			String version = CommandUtil
					.execCmd("adb shell getprop ro.build.version.release");
			capabilities.setCapability("platformVersion", version);

			if (Double.valueOf(version.substring(0, 3)) < 4.3) {
				mApiUnder17 = true;
				LogUtils.log("Device API under 17 change to use Selendroid");
				capabilities.setCapability("automationName", "Selendroid");
			} else {
				mApiUnder17 = false;
			}
			capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
			capabilities.setCapability("newCommandTimeout", 600);
			capabilities.setCapability("autoLaunch", true);
			capabilities.setCapability("orientation",
					SystemPropoerties.VALUE_PORTRAIT);

			capabilities.setCapability("noSign", true);
			capabilities.setCapability("noReset", false);
			capabilities.setCapability("unicodeKeyboard", false);
			capabilities.setCapability("platformName", mPlatformName);
			capabilities.setCapability("app", mTestApp.getAbsolutePath());
			return capabilities;
		} else {
			String appPath = mTestApp.getAbsolutePath();
			String bundleName = appPath.substring(appPath.lastIndexOf("/") + 1,
					appPath.indexOf(".app"));
			DesiredCapabilities capabilities = IOSCapabilities
					.iphone(bundleName);
			capabilities.setCapability("newCommandTimeout", 600);
			capabilities.setCapability("platformName", mPlatformName);
			capabilities.setCapability("platformVersion", mIOSVersion);
			capabilities.setCapability("deviceName", mIOSDevice);

			return capabilities;
		}
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
		return mWebDriver;
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

	private String userSelectPlatform(Scanner scanner) {
		while (true) {
			LogUtils.log("Platform choose:");
			LogUtils.log("1. Android");
			LogUtils.log("2. IOS");

			String userInput = scanner.nextLine();
			try {
				int index = Integer.valueOf(userInput.trim());
				if (index == 1) {
					return SystemPropoerties.VALUE_ANDROID;
				} else if (index == 2) {
					return SystemPropoerties.VALUE_IOS;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private String userSelectIOSDevice(Scanner scanner) {
		String[] devices = new String[] { "iPhone 6", "iPhone 6 Plus",
				"iPhone 5s", "iPhone 5", "iPhone 4s", "iPad 2", "iPad Retina",
				"iPad Air" };
		while (true) {
			LogUtils.log("Choose device:");
			for (int i = 0; i < devices.length; i++) {
				LogUtils.log((i + 1) + ". " + devices[i]);
			}

			String userInput = scanner.nextLine();
			try {
				int index = Integer.valueOf(userInput.trim()) - 1;
				if (index >= 0 && index < devices.length) {
					return devices[index];
				}
			} catch (Exception e) {

			}
		}
	}

	private String userSelectIOSVersion(Scanner scanner) {
		String[] devices = new String[] { "8.1" };
		while (true) {
			LogUtils.log("Choose iOS version:");
			for (int i = 0; i < devices.length; i++) {
				LogUtils.log((i + 1) + ". " + devices[i]);
			}

			String userInput = scanner.nextLine();
			try {
				int index = Integer.valueOf(userInput.trim()) - 1;
				if (index >= 0 && index < devices.length) {
					return devices[index];
				}
			} catch (Exception e) {

			}
		}
	}

	private String userSelectApp(Scanner scanner, String platform) {
		LogUtils.log("Loading server target apps...");
		String responseText = HttpUtils.sendGet(
				"http://autotest.d10970aqcn.alipay.net/autotest/api/testapp?platform="
						+ platform, null);
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

	private JSONObject userSelectConfig(Scanner scanner) {
		LogUtils.log("Loading system configs...");
		String responseText = HttpUtils
				.sendGet(
						"http://autotest.d10970aqcn.alipay.net/autotest/api/sysconfiglist",
						null);
		JSONArray scripts = new JSONArray(responseText);
		for (int i = 0; i < scripts.length(); i++) {
			JSONObject script = scripts.getJSONObject(i);
			LogUtils.log((i + 1) + ".  " + script.getString("title"));
		}
		LogUtils.log("0.  无配置");

		while (true) {
			LogUtils.log("Please enter the configure script serial number(default none):");

			String rawText = scanner.nextLine();
			try {
				if (rawText == null || rawText.isEmpty()) {
					rawText = "0";
				}
				int index = Integer.valueOf(rawText.trim());
				if (index == 0) {
					return null;
				} else {
					return new JSONObject(scripts.getJSONObject(index - 1)
							.getString("content"));
				}
			} catch (Exception e) {

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
			LogUtils.log("How many times you want to run (default: 1):");
			String rawText = scanner.nextLine();
			try {
				if (rawText == null || rawText.length() == 0) {
					rawText = "1";
				}

				int times = Integer.valueOf(rawText.trim());
				return times;
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

	private void logDivilver() {
		LogUtils.log("=====================================================================");
	}
}
