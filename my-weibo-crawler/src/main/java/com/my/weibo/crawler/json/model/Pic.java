package com.my.weibo.crawler.json.model;

public class Pic {
	private String size;
	private String url;
	private Object geo;
	private Pic large;

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setGeo(Object geo) {
		this.geo = geo;
	}
	
	public Object getGeo() {
		return geo;
	}

	public Pic getLarge() {
		return large;
	}

	public void setLarge(Pic large) {
		this.large = large;
	}

	public class Geo {
		private int width;
		private int height;
		private boolean croped;

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public boolean isCroped() {
			return croped;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public void setCroped(boolean croped) {
			this.croped = croped;
		}
	}
}
