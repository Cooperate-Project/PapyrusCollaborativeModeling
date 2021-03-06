/*****************************************************************************
 * Copyright (c) 2014, 2017 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) patrick.tessier@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.revision.tool.ui;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

/**
 * This figure specifies that  a change about an attribute has been done on an element
 *
 */
public class SetFig extends Figure{
	protected String text="";
	/**
	 * 
	 * @param text
	 */
	public SetFig(String text) {
		this.text=text;
	}
	
@Override
public void paint(Graphics graphics) {
	super.paint(graphics);
	graphics.pushState();
	Device device = Display.getCurrent ();
	graphics.setLineWidth(10);
	graphics.setLineWidth(10);
	graphics.setForegroundColor(new org.eclipse.swt.graphics.Color(device, 0, 0, 250));
	graphics.setBackgroundColor(new org.eclipse.swt.graphics.Color(device, 0, 0, 250));
	graphics.drawRectangle(getBounds().x,getBounds().y, getBounds().width, getBounds().height);
	graphics.drawText(text, getBounds().x,getBounds().y-10);
	graphics.popState();
}

}
