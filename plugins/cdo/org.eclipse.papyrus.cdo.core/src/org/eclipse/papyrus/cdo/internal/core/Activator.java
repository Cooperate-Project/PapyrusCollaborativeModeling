/*******************************************************************************
 *  Copyright (c) 2017 CEA LIST.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  		CEA LIST - Initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.cdo.internal.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.papyrus.infra.core.log.LogHelper;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.papyrus.cdo.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/** Logging helper */
	public static LogHelper log = new LogHelper();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// register the log helper
		log.setPlugin(plugin);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		log = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
