package com.my.weibo.crawler.ui;

import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class CommonPrintStream extends ConsolePrintStream {
	private Color color;

	public CommonPrintStream(OutputStream out, Text text) {
		super(out, text);
		this.color = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	}

	@Override
	protected Color getColor() {
		return color;
	}
}
