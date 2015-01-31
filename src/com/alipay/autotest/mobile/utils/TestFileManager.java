package com.alipay.autotest.mobile.utils;

import java.io.File;

/**
 * Persistent file manager, manage include final report file and interim file.
 * 
 * @author ruiqi.li
 */
public class TestFileManager {

	private static TestFileManager sInstance;

	// Report root directory
	public final static File ENVIRONMENT_ROOT = new File(System.getenv("TEST_ROOT"));
	public final static File REPORT_ROOT = new File(ENVIRONMENT_ROOT, "html");
	public final static File TEST_VERIFY_IMG_DIR = new File(ENVIRONMENT_ROOT,
			"/verify_image");
	
	public static TestFileManager getInstance() {
		if (sInstance == null) {
			sInstance = new TestFileManager();
		}

		return sInstance;
	}

	private TestFileManager() {

	}

	public void createReportDirectoryIfNeeded() throws Exception {
		if (!REPORT_ROOT.exists()) {
			REPORT_ROOT.mkdirs();
		}

		// FileUtils.copyTree(new File(EXTERNAL_RES_DIR, "lightbox"), REPORT_ROOT);
	}

	public File getVerityImageFile(String verifyFileName) {
		return new File(TEST_VERIFY_IMG_DIR, verifyFileName);
	}

	public void clearAllReports() {
		emptyDirectory(REPORT_ROOT);
	}

	public void deleteTree(File dir) {
		emptyDirectory(dir);
		dir.delete();
	}

	public void emptyDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		int len = files.length;
		for (int i = 0; i < len; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				deleteTree(file);
			} else {
				file.delete();
			}
		}
	}

}
