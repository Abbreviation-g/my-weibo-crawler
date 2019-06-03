package com.my.weibo.crawler;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.my.weibo.crawler.ui.MainFrame;

public class Main {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);

		MainFrame mainFrame = new MainFrame(shell);
		mainFrame.createContent();

		shell.pack();
		int x = (display.getClientArea().width - shell.getSize().x) / 2;
		int y = (display.getClientArea().height - shell.getSize().y) / 2;
		shell.setLocation(x, y);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
