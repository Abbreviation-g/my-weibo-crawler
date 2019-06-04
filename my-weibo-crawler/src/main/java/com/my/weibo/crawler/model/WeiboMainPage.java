package com.my.weibo.crawler.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import com.alibaba.fastjson.JSON;
import com.my.weibo.crawler.json.model.Card;
import com.my.weibo.crawler.json.model.Container;
import com.my.weibo.crawler.json.model.Data;
import com.my.weibo.crawler.json.model.Pic;

public class WeiboMainPage {
	private String containerid;

	private List<String> imgUrlList;
	private List<String> rawImgUrlList;

	public WeiboMainPage(String uid) throws ClientProtocolException, URISyntaxException, IOException, InterruptedException {
		this.containerid = "107603" + uid;
		this.imgUrlList = new ArrayList<>();
		this.rawImgUrlList = new ArrayList<>();
	}

	public String getContainerid() {
		return containerid;
	}

	public void scanForImg(IProgressMonitor monitor) throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
		this.imgUrlList = new ArrayList<>();
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		HttpClient httpClient = clientBuilder.build();

		int page = 1;
		while (true) {
			if(monitor.isCanceled())
				break;
			monitor.beginTask("正在解析第"+page+"页", IProgressMonitor.UNKNOWN);
			boolean end = getImgUrl(httpClient, page);
			if (end) {
				break;
			}
			page++;
			Thread.sleep(2 * 1000);
			monitor.done();
		}
		System.out.println(page);
	}

	private boolean getImgUrl(HttpClient httpClient, int page) throws URISyntaxException, ClientProtocolException, IOException{
		String url = "https://m.weibo.cn/api/container/getIndex";
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> list = new LinkedList<>();
        list.add(new BasicNameValuePair("count", "25"));
        list.add(new BasicNameValuePair("page", Integer.toString(page)));
        list.add(new BasicNameValuePair("containerid", getContainerid()));
        uriBuilder.setParameters(list);
		HttpGet get = new HttpGet(uriBuilder.build());
		HttpResponse response = httpClient.execute(get);
		String responseStr = EntityUtils.toString(response.getEntity());
		try {
			Container jsonObject = JSON.parseObject(responseStr,Container.class);
			if(jsonObject == null) {
				//end
				return true;
			}
			Data data = jsonObject.getData();
			if(data == null) {
				//end
				return true;
			}
			Card[] cards = data.getCards();
			if(cards == null || cards.length ==0) {
				//end
				return true;
			}
			for(Card card:cards) {
				Pic[] pics = card.getMblog().getPics();
				if(pics == null || pics.length ==0) {
					continue; 
				}
				for(Pic pic :pics) {
					this.imgUrlList.add(pic.getUrl());
					Pic large = pic.getLarge();
					if(large != null) {
						this.rawImgUrlList.add(large.getUrl());
						System.out.println("Detected: "+large.getUrl());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(responseStr);
		}
		
		return false;
	}
	
	public List<String> getRawImgUrlList() {
		return rawImgUrlList;
	}

	public static void main(String[] args)
			throws ClientProtocolException, URISyntaxException, IOException, InterruptedException {
		String uid = "3261134763";

		WeiboMainPage mainPage = new WeiboMainPage(uid);
		List<String> rawImgUrlList = mainPage.rawImgUrlList;

		System.out.println(rawImgUrlList.size());
	}
}
