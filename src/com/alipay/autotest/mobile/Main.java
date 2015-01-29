package com.alipay.autotest.mobile;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.testng.IExecutionListener;
import org.testng.TestNG;
import org.uncommons.reportng.HTMLReporter;

import com.alipay.autotest.mobile.appium.TestContext;
import com.alipay.autotest.mobile.monitor.Monitor;
import com.alipay.autotest.mobile.utils.CommandUtil;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.TestFileManager;

/**
 * Test entrance class.
 * 
 * @author ruiqi.li
 */
public class Main {

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		System.out.println("Charset: " + Charset.defaultCharset());
		String devices = CommandUtil.execCmd("adb devices");
		if (devices.indexOf("device") == devices.lastIndexOf("device")) {
			LogUtils.log("None android devices connected! /n" + devices);
			return;
		}

		TestFileManager fileManager = TestFileManager.getInstance();
		fileManager.clearAllReports();

		TestContext context = TestContext.getInstance();
		try {
			LogUtils.log("Starting setup test resources..");
			context.setupContext();
		} catch (Exception e) {
			LogUtils.log("Setup test context failed: " + e.toString());
			return;
		}

		List<Class> listeners = new ArrayList<Class>();
		listeners.add(HTMLReporter.class);

		TestNG testNG = new TestNG();
		testNG.addExecutionListener(new IExecutionListener() {

			@Override
			public void onExecutionStart() {
				LogUtils.log("Starting test evnernment, this may take a few seconds..");
				TestContext.getInstance().startAppiumDriver();
				Monitor.getInstance().startRecording();
			}

			@Override
			public void onExecutionFinish() {
				TestContext.getInstance().stopAppiumDriver();
				Monitor monitor = Monitor.getInstance();
				monitor.finishRecording();
				monitor.generateReport();
			}
		});
		testNG.setListenerClasses(listeners);
		testNG.setXmlSuites(context.getTestSuites());
		testNG.setVerbose(8);
		testNG.setUseDefaultListeners(false);
		testNG.setOutputDirectory(TestFileManager.ENVIRONMENT_ROOT
				.getAbsolutePath());
		testNG.run();
	}

}
