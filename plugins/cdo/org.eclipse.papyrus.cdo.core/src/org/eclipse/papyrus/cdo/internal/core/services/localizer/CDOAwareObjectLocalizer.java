/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus - bug 488558
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.core.services.localizer;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.eresource.CDOResourceNode;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.papyrus.cdo.internal.core.CDOUtils;
import org.eclipse.papyrus.infra.core.services.SharedServiceFactory;
import org.eclipse.papyrus.infra.services.localizer.DefaultObjectLocalizer;
import org.eclipse.papyrus.infra.services.localizer.IObjectLocalizer;


/**
 * This is the CDOAwareObjectLocalizer type. Enjoy.
 */
public class CDOAwareObjectLocalizer extends DefaultObjectLocalizer {

	public CDOAwareObjectLocalizer() {
		super();
	}

	@Override
	public EObject getLocalEObject(ResourceSet localSet, EObject remoteObject) {
		EObject result = null;

		if (!(remoteObject instanceof CDOResourceNode)) {
			result = super.getLocalEObject(localSet, remoteObject);
		} else if (remoteObject instanceof CDOResource) {
			result = (CDOResource) getLocalResource(localSet, (CDOResource) remoteObject);
		} else {
			// these need special handling!
			CDOResourceNode node = (CDOResourceNode) remoteObject;
			String path = node.getPath();

			CDOView view = CDOUtils.getView(localSet);
			if (view != null) {
				try {
					result = view.getResourceNode(path);
				} catch (Exception e) {
					// normal consequence of the node path not being known
				}
			}
		}

		return result;
	}

	@Override
	public Resource getLocalResource(ResourceSet localSet, Resource remoteResource) {
		Resource result = null;

		if (!(remoteResource instanceof CDOResource)) {
			result = super.getLocalResource(localSet, remoteResource);
		} else {
			CDOResource cdo = (CDOResource) remoteResource;
			String path = cdo.getPath();

			CDOView view = CDOUtils.getView(localSet);
			if (view != null) {
				try {
					result = view.getResource(path);
				} catch (Exception e) {
					// normal consequence of the resource path not being known
				}
			}
		}

		return result;
	}

	//
	// Nested types
	//

	public static class Factory extends SharedServiceFactory<IObjectLocalizer> {

		public Factory() {
			super(IObjectLocalizer.class, CDOAwareObjectLocalizer::new);
		}

	}
}
