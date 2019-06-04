package com.my.weibo.crawler.json.model;

import java.io.IOException;
import java.net.URISyntaxException;
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

import com.alibaba.fastjson.JSON;

public class Container {
	private Data data;
	private int ok;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public int getOk() {
		return ok;
	}

	public void setOk(int ok) {
		this.ok = ok;
	}
	
	public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException {
		String uid = "3261134763"; 
		String containerid = "107603"+uid;
		
		String url = "https://m.weibo.cn/api/container/getIndex";
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> list = new LinkedList<>();
        list.add(new BasicNameValuePair("count", "25"));
        list.add(new BasicNameValuePair("page", Integer.toString(1)));
        list.add(new BasicNameValuePair("containerid", containerid));
        uriBuilder.setParameters(list);
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		HttpClient httpClient = clientBuilder.build();
		HttpGet get = new HttpGet(uriBuilder.build());
		HttpResponse response = httpClient.execute(get);
		String responseStr = EntityUtils.toString(response.getEntity());
		Container jsonObject = JSON.parseObject(responseStr,Container.class);
		if(jsonObject == null) {
			//end
			return;
		}
		Data data = jsonObject.getData();
		if(data == null) {
			//end
			return;
		}
		Card[] cards = data.getCards();
		if(cards == null || cards.length ==0) {
			//end
			return;
		}
		for(Card card:cards) {
			Pic[] pics = card.getMblog().getPics();
			if(pics == null || pics.length ==0) {
				continue; 
			}
			for(Pic pic :pics) {
				System.out.println("small: "+pic.getUrl());
				Pic large = pic.getLarge();
				if(large != null) {
					System.out.println("large: "+large.getUrl());
				}
			}
		}
	}
}
