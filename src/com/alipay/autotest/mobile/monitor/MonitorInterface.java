package com.alipay.autotest.mobile.monitor;

public interface MonitorInterface {
	
	public void startRecording();
	
	public void finishRecording();
	
	public void recordActionStart(int index, String action);
	
	public void recordActionEnd(int index, String action);
	
	public void generateReport();

}
