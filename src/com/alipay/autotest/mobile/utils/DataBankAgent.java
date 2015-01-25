package com.alipay.autotest.mobile.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class DataBankAgent {

	protected String host = "";

	public DataBankAgent(String host) {
		this.host = host;
	}

	public String callDataBankAgent(String agentId, String userParams) {
		String path = "/execute/executeData.htm";
		String url = "http://" + host + path;
		String result = "";
		String runInfoIds = "";

		URL serverUrl = null;
		HttpURLConnection urlConnection = null;
		try {
			serverUrl = new URL(url);
			urlConnection = (HttpURLConnection) serverUrl.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");

			BufferedWriter httpRequestBodyWriter = new BufferedWriter(
					new OutputStreamWriter(urlConnection.getOutputStream()));
			StringBuilder bodyBuilder = new StringBuilder();
			bodyBuilder.append("loginName").append("=").append("tianzhong.ctz")
					.append("&");
			bodyBuilder.append("dataId").append("=").append(agentId)
					.append("&");
			bodyBuilder.append("userParam").append("=").append(userParams)
					.append("&");
			bodyBuilder.append("isShare").append("=").append("0").append("&");
			bodyBuilder.append("number").append("=").append("1").append("&");
			bodyBuilder.append("version").append("=").append("1.0.0");
			httpRequestBodyWriter.write(bodyBuilder.toString());
			httpRequestBodyWriter.close();

			Scanner httpResponseScanner = new Scanner(
					urlConnection.getInputStream());
			StringBuilder resultBuilder = new StringBuilder();
			while (httpResponseScanner.hasNextLine()) {
				resultBuilder.append(httpResponseScanner.nextLine());
			}
			httpResponseScanner.close();

			result = resultBuilder.toString();

			if (result != null && !result.isEmpty()) {
				runInfoIds = result.split(",")[0].split(":")[1].substring(1);
				runInfoIds = runInfoIds.substring(0, runInfoIds.length() - 1);
			}
			return runInfoIds.trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		return null;
	}

	public String getDataBankResult(String runInfos) {
		String path = "/execute/executeDataStatus.htm";
		String url = "http://" + host + path;
		String result = "";

		URL serverUrl = null;
		HttpURLConnection urlConnection = null;
		for (int i = 0; i < 60; i++) {
			try {
				serverUrl = new URL(url);
				urlConnection = (HttpURLConnection) serverUrl.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("POST");

				BufferedWriter httpRequestBodyWriter = new BufferedWriter(
						new OutputStreamWriter(urlConnection.getOutputStream()));
				StringBuilder bodyBuilder = new StringBuilder();
				bodyBuilder.append("runInfoId").append("=").append(runInfos);
				httpRequestBodyWriter.write(bodyBuilder.toString());
				httpRequestBodyWriter.close();

				Scanner httpResponseScanner = new Scanner(
						urlConnection.getInputStream());
				StringBuilder resultBuilder = new StringBuilder();
				while (httpResponseScanner.hasNextLine()) {
					resultBuilder.append(httpResponseScanner.nextLine());
				}
				httpResponseScanner.close();

				result = resultBuilder.toString();
				if (result.indexOf("COMPLETED") > 0) {
					break;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
			}
		}

		return result;
	}

	public static void main(String[] args) {


	}
}
