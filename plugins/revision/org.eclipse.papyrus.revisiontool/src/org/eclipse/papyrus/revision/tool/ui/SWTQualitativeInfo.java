/*****************************************************************************
 * Copyright (c) 2016, 2017 CEA LIST
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tessier CEA LIST-implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.revision.tool.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is to display a info like camembert, bar or histogramme
 * <BR>
 * <img src="camembert.png">
 * <BR>
 * <h3>How to use:</h3>
 * <BR>
 * <code>
 *  int[] pourcent = { 30, 20, 10, 40 };<BR>
 *   String[] labels = { "Added", "Modified", "Deleted", "Stable" };<BR>
 *   Device device = Display.getCurrent();<BR>
 *   org.eclipse.swt.graphics.Color[] colors = { device.getSystemColor(SWT.COLOR_GREEN), device.getSystemColor(SWT.COLOR_CYAN),<BR>
 *   device.getSystemColor(SWT.COLOR_RED), device.getSystemColor(SWT.COLOR_YELLOW) };<BR>
 *   SWTQualitativeInfo canva = new SWTQualitativeInfo(shell, SWT.NULL, pourcent, labels, colors, HISTOGRAMME);<BR>
 * </code>
 **/
public class SWTQualitativeInfo extends Canvas {

	/** to display as a disk **/
	public static int DISK = 0;

	/** to display as histogram **/
	public static int HISTOGRAMME = 1;

	/** to display as bar **/
	public static int BAR = 2;

	/**
	 * 
	 * Constructor.
	 *
	 * @param parent
	 *        the composite owner
	 * @param style
	 *        SWT style
	 * @param pourcent
	 *        list of pourcent
	 * @param labels
	 *        list of label that represent pourcent
	 * @param colors
	 *        list of color for each pourcent
	 * @param type
	 *        type of diagram DISK, HISTOGRAMME, BAR
	 */
	public SWTQualitativeInfo(Composite parent, int style, final int[] pourcent, final String[] labels, final org.eclipse.swt.graphics.Color[] colors, final int type) {
		super(parent, style);
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
				e.gc.fillRectangle(0, 0, 300, 300);
				if(type == BAR) {
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
					e.gc.fillRectangle(29, 29, 102, 202);
					int x = 30;
					int y = 30;
					for(int i = 0; i < pourcent.length; i++) {
						e.gc.setBackground(colors[i]);
						e.gc.fillRectangle(x, y, 100, pourcent[i] * 2);
						e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
						e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
						e.gc.drawString(labels[i] + " " + pourcent[i] + "%", x + 110, y + 10);
						y = y + pourcent[i] * 2;
					}
				} else if(type == DISK) {
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
					//e.gc.fillOval(28, 28, 203, 203);
					int startAngle = 0;
					int arcAngle = 0;
					int x = 30;
					int y = 240;
					for(int i = 0; i < pourcent.length; i++) {
						e.gc.setBackground(colors[i]);
						arcAngle = (int)(pourcent[i] * 3.6);
						e.gc.fillArc(30, 30, 200, 200, startAngle, arcAngle);
						e.gc.fillRectangle(x, y, 10, 10);
						e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
						e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
						e.gc.drawString(labels[i] + " " + pourcent[i] + "%", x + 20, y - 2);
						y = y + 20;
						startAngle = startAngle + arcAngle;
					}
				} else {
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
					e.gc.fillRectangle(35, 29, labels.length * 30, 200);
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
					e.gc.fillRectangle(29, 29, 5, 2);
					e.gc.fillRectangle(29, 77, 5, 2);
					e.gc.fillRectangle(29, 127, 5, 2);
					e.gc.fillRectangle(29, 177, 5, 2);
					e.gc.fillRectangle(29, 227, 5, 2);
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
					e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
					e.gc.drawString("100%", 2, 19);
					e.gc.drawString("75%", 2, 67);
					e.gc.drawString("50%", 2, 117);
					e.gc.drawString("25%", 2, 167);
					e.gc.drawString("0%", 2, 217);
					int x = 39;
					int y = 30;
					for(int i = 0; i < pourcent.length; i++) {
						e.gc.setBackground(colors[i]);
						e.gc.fillRectangle(x, (100 - pourcent[i]) * 2 + 29, 20, pourcent[i] * 2);
						e.gc.fillRectangle(labels.length * 30 + 40, y + 10, 10, 10);
						e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
						e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
						e.gc.drawString(labels[i] + " " + pourcent[i] + "%", labels.length * 30 + 50, y + 10);
						y = y + 20;
						x = x + 30;
					}
				}
			}
		});
	}

	/**
	 * static main to execute the example
	 * 
	 * @param a
	 */
	public static void main(String[] a) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		int[] pourcent = { 30, 20, 10, 40 };
		String[] labels = { "Added", "Modified", "Deleted", "Stable" };
		Device device = Display.getCurrent();
		org.eclipse.swt.graphics.Color[] colors = { device.getSystemColor(SWT.COLOR_GREEN), device.getSystemColor(SWT.COLOR_CYAN), device.getSystemColor(SWT.COLOR_RED), device.getSystemColor(SWT.COLOR_YELLOW) };
		SWTQualitativeInfo canva = new SWTQualitativeInfo(shell, SWT.NULL, pourcent, labels, colors, BAR);
		shell.setLayout(new FillLayout());
		shell.pack();
		shell.setSize(300, 300);
		shell.setText("Repartition");
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
}
