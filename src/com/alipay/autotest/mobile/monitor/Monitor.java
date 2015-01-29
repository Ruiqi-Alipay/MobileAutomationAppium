package com.alipay.autotest.mobile.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.alipay.autotest.mobile.appium.TestContext;
import com.alipay.autotest.mobile.utils.CommandUtil;
import com.alipay.autotest.mobile.utils.GZipUtils;
import com.alipay.autotest.mobile.utils.LogUtils;
import com.alipay.autotest.mobile.utils.ProcessUtil;
import com.alipay.autotest.mobile.utils.ShellExecute;
import com.alipay.autotest.mobile.utils.TestFileManager;

public class Monitor {
	private static Monitor sInstance;
	private String mPackageName;
	private int mAppPid;
	private int mAppUid;
	private DecimalFormat mFormater;
	private double[] mNetworkData;
	private StringBuilder mLogBuilder;
	private boolean mNewStart;
	private String mRecordFilePath;
	private Thread mThread;

	public static Monitor getInstance() {
		if (sInstance == null) {
			sInstance = new Monitor();
		}

		return sInstance;
	}

	private Monitor() {

	}

	public void startRecording() {
		mFormater = new DecimalFormat("#.00");

		mPackageName = TestContext.getInstance().getTestAppPackage();
		List<Integer> pids = ProcessUtil.getPidsByPackageName(mPackageName);
		if (pids != null && pids.size() != 0) {
			mAppPid = pids.get(0);
		}
		mAppUid = ProcessUtil.getUidByPid(mAppPid);

		mNetworkData = retriveNetworkData();
		mLogBuilder = new StringBuilder();

		mNewStart = true;

		if (!TestFileManager.REPORT_ROOT.exists()) {
			TestFileManager.REPORT_ROOT.mkdirs();
		}
		mRecordFilePath = TestFileManager.REPORT_ROOT + "/performance.report";
	}

	public void finishRecording() {
		recordContent(mRecordFilePath, "]");
	}

	public void recordActionStart(int index, String action) {
		try {
			Runtime.getRuntime().exec("adb logcat -c");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		mLogBuilder.setLength(0);
		mNetworkData = retriveNetworkData();

		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Process cpuProcess = null;
				BufferedReader mReader = null;
				try {
					cpuProcess = Runtime.getRuntime().exec(
							"adb logcat | grep " + mAppPid + "\n");
					mReader = new BufferedReader(new InputStreamReader(
							cpuProcess.getInputStream(), "UTF-8"));
					String line = null;
					while ((line = mReader.readLine()) != null) {
						if (Thread.currentThread().isInterrupted()) {
							return;
						}

						if (line.equals("")) {
							continue;
						}

						mLogBuilder.append(line).append("\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (mReader != null) {
						try {
							mReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (cpuProcess != null) {
						cpuProcess.destroy();
						cpuProcess = null;
					}
				}
			}
		});
		mThread.start();
	}

	public void recordActionEnd(int index, String action) {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}

		String memoInfoTotal = new ShellExecute().execute("dumpsys meminfo "
				+ mPackageName).console.toString();
		
		int start = memoInfoTotal.indexOf("Dalvik Heap");
		int end = memoInfoTotal.indexOf("Dalvik Other");
		if (start > 0 && end > start) {
			String[] datas = memoInfoTotal.substring(start, end).split("\\s+");
			
			double heapSize = Double.valueOf(mFormater.format(Double
					.valueOf(datas[6]) / 1024));

			double[] networkData = retriveNetworkData();
			double sent = networkData[0] - mNetworkData[0];
			double rev = networkData[1] - mNetworkData[1];

			sent = round(sent, 1);
			rev = round(rev, 1);

			int cupProcent = retriveCPUUsage();

			JSONObject record = new JSONObject();
			record.put("index", index);
			record.put("action", action);
			record.put("heap", heapSize);
			record.put("sent", sent);
			record.put("reve", rev);
			record.put("cpu", cupProcent);

			JSONObject data = new JSONObject();
			data.put("log", mLogBuilder.toString());
			data.put("mem", memoInfoTotal);
			record.put("data", data);

			if (mNewStart) {
				recordContent(mRecordFilePath, "[");
			} else {
				recordContent(mRecordFilePath, ",");
			}
			recordContent(mRecordFilePath, record.toString());
		}
		mNewStart = false;
	}

	public void generateReport() {
		new Thread() {

			@Override
			public void run() {
				LogUtils.log("Generating report...");
				int waitSecond = 60;
				File htmlReport = new File(TestFileManager.REPORT_ROOT,
						"reportng.js");
				while (waitSecond > 0) {
					waitSecond--;

					if (htmlReport.exists()) {
						break;
					} else {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
				}

				if (htmlReport.exists()) {
					Calendar calendar = Calendar.getInstance();
					StringBuilder builder = new StringBuilder();
					builder.append(calendar.get(Calendar.YEAR)).append("-");
					builder.append(calendar.get(Calendar.MONTH) + 1)
							.append("-");
					builder.append(calendar.get(Calendar.DAY_OF_MONTH)).append(
							"_");
					builder.append(calendar.get(Calendar.HOUR_OF_DAY)).append(
							"h-");
					builder.append(calendar.get(Calendar.MINUTE)).append("m-");
					builder.append(calendar.get(Calendar.SECOND)).append("s");

					try {
						String name = "report_" + builder.toString();
						GZipUtils.zipDirectory(TestFileManager.REPORT_ROOT,
								TestFileManager.ENVIRONMENT_ROOT, name);
						TestFileManager.getInstance().deleteTree(
								TestFileManager.REPORT_ROOT);
						LogUtils.log("Generate report success: " + name);

						LogUtils.log("Uploading report..");
						File reportFile = new File(
								TestFileManager.ENVIRONMENT_ROOT, name
										+ ".tar.gz");
						HttpEntity httpEntity = MultipartEntityBuilder
								.create()
								.addBinaryBody("file", reportFile,
										ContentType.create("image/jpeg"),
										name + ".report").build();

						HttpPost request = new HttpPost(
								"http://autotest.d10970aqcn.alipay.net/autotest/api/report");
						request.setEntity(httpEntity);

						HttpClient client = new DefaultHttpClient();
						HttpResponse response = client.execute(request);
						if (response.getStatusLine().getStatusCode() == 200) {
							LogUtils.log("Upload success: http://autotest.d10970aqcn.alipay.net/reporter/reports/"
									+ name + ".report/index.html");
							reportFile.delete();
						} else {
							LogUtils.log("Upload failed please upload this report manually: status code "
									+ response.getStatusLine().getStatusCode());
						}
					} catch (Exception e) {
						e.printStackTrace();
						LogUtils.log("Generate report failed: " + e);
					}
				} else {
					LogUtils.log("Generat report failed: html folder not found!");
				}
			}

		}.start();
	}

	private double[] retriveNetworkData() {
		String contentRcv = "" + 0;
		String contentSnd = "" + 0;

		String uids = CommandUtil.execCmd("adb shell ls /proc/uid_stat | grep "
				+ mAppUid);
		if (uids.contains("" + mAppUid)) {
			contentRcv = CommandUtil.execCmd("adb shell cat /proc/uid_stat/"
					+ mAppUid + "/tcp_rcv");
			contentSnd = CommandUtil.execCmd("adb shell cat /proc/uid_stat/"
					+ mAppUid + "/tcp_snd");
		}

		double sentValue = Double.valueOf(mFormater.format(Double
				.valueOf(contentSnd) / 1024));
		double revValue = Double.valueOf(mFormater.format(Double
				.valueOf(contentRcv) / 1024));

		return new double[] { sentValue, revValue };
	}

	private int retriveCPUUsage() {
		BufferedReader in = null;
		Process cpuProcess = null;
		try {
			cpuProcess = Runtime.getRuntime().exec(
					"adb shell top -n 999999 -d 1" + "\n");
			in = new BufferedReader(new InputStreamReader(
					cpuProcess.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.equals("") || !line.contains(mPackageName)) {
					continue;
				}

				String[] rows = line.trim().split("\\s{1,}");
				return Integer.valueOf(rows[2].substring(0,
						rows[2].indexOf("%")));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (cpuProcess != null) {
				cpuProcess.destroy();
				cpuProcess = null;
			}
		}

		return 0;
	}

	private void recordContent(String filePath, String content) {
		FileWriterWithEncoding writer = null;
		try {
			writer = new FileWriterWithEncoding(filePath, "UTF-8", true);
			writer.write(content);
		} catch (IOException e) {

		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static double round(double value, int places) {
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}