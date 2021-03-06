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
 *   Christian W. Damus (CEA) - bug 386118
 *   Eike Stepper (CEA) - bug 466520
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.ui.adapters;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.papyrus.cdo.internal.core.CDOUtils;
import org.eclipse.papyrus.emf.facet.custom.metamodel.v0_2_0.internal.treeproxy.EObjectTreeElement;

/**
 * This is the CDOAdapterFactory type. Enjoy.
 */
public class CDOAdapterFactory implements IAdapterFactory {

	private final Class<?>[] supported = { CDOObject.class, CDOView.class };

	public CDOAdapterFactory() {
		super();
	}

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		Object result = null;

		if (adapterType == CDOObject.class) {
			EObject eObject = null;
			if (adaptableObject instanceof EditPart) {
				// notation views are important as CDOObjects for locking and
				// conflict purposes, so do not defer to the semantic element
				eObject = (EObject) ((EditPart) adaptableObject).getModel();
			} else if (adaptableObject instanceof EObject) {
				eObject = (EObject) adaptableObject;
			} else {
				// try really hard to get an EObject
				if (adaptableObject instanceof IAdaptable) {
					eObject = (EObject) ((IAdaptable) adaptableObject).getAdapter(EObject.class);
				}
				if (eObject == null) {
					eObject = (EObject) Platform.getAdapterManager().getAdapter(adaptableObject, EObject.class);
				}
			}

			// get the CDOObject from the EObject (if possible)
			result = CDOUtils.getCDOObject(eObject);
		} else if (adapterType == CDOView.class) {
			if (adaptableObject instanceof EditPart) {
				Object object = ((EditPart) adaptableObject).getModel();
				if (object instanceof EObject) {
					CDOObject cdoObject = CDOUtils.getCDOObject((EObject) object);
					if (cdoObject != null) {
						result = cdoObject.cdoView();
					}
				}
			} else if (adaptableObject instanceof EObjectTreeElement) {
				EObject eObject = ((EObjectTreeElement) adaptableObject).getEObject();
				if (eObject != null) {
					CDOObject cdoObject = CDOUtils.getCDOObject(eObject);
					if (cdoObject != null) {
						result = cdoObject.cdoView();
					}
				}
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return supported;
	}

}
