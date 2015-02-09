package com.alipay.autotest.mobile;

import java.util.ArrayList;
import java.util.List;

import org.testng.IExecutionListener;
import org.testng.TestNG;
import org.uncommons.reportng.HTMLReporter;

import com.alipay.autotest.mobile.appium.TestContext;
import com.alipay.autotest.mobile.monitor.MonitorFactory;
import com.alipay.autotest.mobile.monitor.MonitorInterface;
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
				MonitorFactory.getInstance(
						TestContext.getInstance().getPlatformName())
						.startRecording();
			}

			@Override
			public void onExecutionFinish() {
				TestContext.getInstance().stopAppiumDriver();
				MonitorInterface monitor = MonitorFactory
						.getInstance(TestContext.getInstance()
								.getPlatformName());
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
