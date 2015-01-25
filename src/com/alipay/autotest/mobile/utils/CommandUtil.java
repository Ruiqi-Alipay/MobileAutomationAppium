/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.autotest.mobile.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * 
 * @author qiyi.wxc
 * @version $Id: CommandUtil.java, v 0.1 2014年9月17日 下午12:18:25 qiyi.wxc Exp $
 */
public class CommandUtil {
	/**
	 * types of Operating Systems
	 */
	public static final int WINDOWS = 1;
	public static final int MACOS = 2;
	public static final int LINUX = 3;
	public static final int OTHER = 4;

	/**
	 * detect the operating system from the os.name System property and cache
	 * the result
	 * 
	 * @returns - the operating system detected
	 */
	public static int getOperatingSystemType() {
		String OS = System.getProperty("os.name", "generic").toLowerCase(
				Locale.ENGLISH);
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			return MACOS;
		} else if (OS.indexOf("win") >= 0) {
			return WINDOWS;
		} else if (OS.indexOf("nux") >= 0) {
			return LINUX;
		} else {
			return OTHER;
		}
	}

	public static String execCmd(String command) {
		BufferedReader br = null;
		StringBuffer stringBuffer = new StringBuffer();
		try {
			Process p = Runtime.getRuntime().exec(command);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				if ("".equals(line.trim()))
					continue;

				stringBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuffer.toString();
	}
}
