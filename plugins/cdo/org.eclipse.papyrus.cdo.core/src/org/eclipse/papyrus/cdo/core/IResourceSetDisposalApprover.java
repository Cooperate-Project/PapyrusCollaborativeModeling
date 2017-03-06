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
package org.eclipse.papyrus.cdo.core;

import java.util.Collection;

import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * A protocol for objects that a {@link CDOCheckout} may consult to
 * approve the closure of resource sets that have uncommitted changes.
 */
public interface IResourceSetDisposalApprover {

	DisposeAction disposalRequested(CDOCheckout checkout,
			Collection<ResourceSet> resourceSets);

	//
	// Nested types
	//

	enum DisposeAction {
		NONE, SAVE, CLOSE;
	}
}
