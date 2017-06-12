/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.cdo.core.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.cdo.eresource.impl.CDOResourceImpl;
import org.eclipse.emf.common.util.URI;

/**
 * @author XZ223605
 * needed to be compatible with internationnalization
 */
public class PapyrusCDOResourceImpl extends CDOResourceImpl {
	
	/**
	 * Constructor.
	 *
	 */
	public PapyrusCDOResourceImpl(URI uri) {
		super(uri);
	}

	/**
	 * @see org.eclipse.emf.cdo.eresource.impl.CDOResourceImpl#getDefaultSaveOptions()
	 * 
	 * @return
	 */
	@Override
	public Map<Object, Object> getDefaultSaveOptions() {
		return new HashMap<Object, Object>();
	}
	
}
