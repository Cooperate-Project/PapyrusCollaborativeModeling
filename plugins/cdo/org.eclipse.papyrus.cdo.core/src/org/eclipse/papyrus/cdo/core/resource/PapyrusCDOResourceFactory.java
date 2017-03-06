/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Eike Stepper (CEA) - bug 466520
 *****************************************************************************/
package org.eclipse.papyrus.cdo.core.resource;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceFactoryImpl;

/**
 * This is the PapyrusCDOResourceFactory type. Enjoy.
 */
public class PapyrusCDOResourceFactory
		extends CDOResourceFactoryImpl {

	private final CDOAwareModelSet modelSet;

	public PapyrusCDOResourceFactory(CDOAwareModelSet modelSet) {
		super();

		this.modelSet = modelSet;
	}

	@Override
	protected boolean isGetResource() {
		return modelSet.isInGetResource() || super.isGetResource();
	}
}
