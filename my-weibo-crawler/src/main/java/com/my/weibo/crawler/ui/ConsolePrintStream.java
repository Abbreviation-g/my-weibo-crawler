package com.my.weibo.crawler.ui;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * 定义一个PrintStream子类,将打印语句输出流重定向到Text组件中显示
 * @author guo
 *
 */
public abstract class ConsolePrintStream extends PrintStream {

	private Text text;

	public ConsolePrintStream(OutputStream out, Text text) {
		super(out);
		this.text = text;
	}

	/**
	 * 重写父类write方法,这个方法是所有打印方法里面都要调用的方法
	 */
	public void write(byte[] buf, int off, int len) {
		final String message = new String(buf, off, len);
		Display.getDefault().syncExec(() -> {
			// 把信息添加到组件中
			if (text != null && !text.isDisposed()) {
				text.setForeground(getColor());
				text.append(message);
			}
		});
	}
	
	protected abstract Color getColor();
}

