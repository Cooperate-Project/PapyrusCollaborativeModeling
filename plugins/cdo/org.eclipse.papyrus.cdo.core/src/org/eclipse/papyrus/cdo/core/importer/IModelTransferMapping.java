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
package org.eclipse.papyrus.cdo.core.importer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.common.util.Diagnostic;

/**
 * This is the IModelTransferMapping type. Enjoy.
 */
public interface IModelTransferMapping {

	IModelTransferConfiguration getConfiguration();

	void mapTo(IModelTransferNode source, IPath path);

	IPath getMapping(IModelTransferNode node);

	CDOCheckout getCheckout();

	void setCheckout(CDOCheckout checkout);

	Diagnostic validate();

	void addModelTransferMappingListener(IModelTransferMappingListener listener);

	void removeModelTransferMappingListener(IModelTransferMappingListener listener);
}
