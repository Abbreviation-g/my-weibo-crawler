package com.my.weibo.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class DownloadWeibo {
	public static void downloadPages(List<String> imgUrlList, IProgressMonitor monitor, File outputFolder) {
		monitor.beginTask("正在下载", imgUrlList.size());
		for (String imgUrl : imgUrlList) {
			if (monitor.isCanceled())
				break;
			monitor.subTask(imgUrl);
			download(imgUrl, outputFolder);
			monitor.worked(1);
		}
	}

	public static void download(String urlStr, File outputFolder) {
		String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
		File imgFile = new File(outputFolder, fileName);
		if (imgFile.exists()) {
			System.out.println(imgFile + " already exists. ");
			return;
		}
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream inputStream = connection.getInputStream();
			if (!outputFolder.exists())
				outputFolder.mkdirs();
			FileOutputStream writer = new FileOutputStream(imgFile);
			byte[] data = new byte[1024];
			int n = 0;
			while ((n = inputStream.read(data)) != -1) {
				writer.write(data, 0, n);
			}
			inputStream.close();
			writer.close();

			System.out.println("Download " + imgFile + " is done. ");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String imgUrl = "https://wx4.sinaimg.cn/large/693ad074gy1fotwfa36jkj221t4vahdv.jpg";
		File outputFolder = new File("C:\\Users\\guo\\Desktop\\temp");
		download(imgUrl, outputFolder);
	}
}
