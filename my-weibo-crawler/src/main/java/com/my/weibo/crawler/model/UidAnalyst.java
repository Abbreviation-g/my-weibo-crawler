package com.my.weibo.crawler.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class UidAnalyst {
	public static void main(String[] args) throws MalformedURLException{
		final String mainPageUrl = "https://weibo.com/liuyifeiofficial?is_all=1";
		
		URL url = new URL(mainPageUrl);
		System.out.println(url.getHost());
		
		UidAnalyst getUidTest = new UidAnalyst(mainPageUrl);
		System.out.println(getUidTest.getPageConfig());
	}
	
	private String pageConfig;
	private JSONObject configJson;
	
	public UidAnalyst(String mainPageUrl) {
		initPageText(mainPageUrl);
	}

	private void initPageText(String mainPageUrl) {

		Display display = new Display();
		Shell shell = new Shell(display);
		Browser browser = new Browser(shell, SWT.NONE);

		browser.setUrl(mainPageUrl);
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				Object result = browser.evaluate("return JSON.stringify($CONFIG);",true);
				pageConfig = result.toString();
				configJson = JSON.parseObject(pageConfig);
				shell.close();
			}
		});
		// shell.open();

		shell.pack();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

	}

	public String getPageConfig() {
		return pageConfig;
	}
	
	public String getUid() {
		return configJson.getString("oid");
	}
	
	public String getTitle() {
		return configJson.getString("title_value");
	}
}
