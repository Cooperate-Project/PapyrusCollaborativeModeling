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
 *   Christian W. Damus (CEA) - bug 431953 (pre-requisite refactoring of ModelSet service start-up)
 *   Eike Stepper (CEA) - bug 466520
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.core.resource;

import org.eclipse.papyrus.cdo.internal.core.l10n.Messages;
import org.eclipse.papyrus.infra.core.editor.ModelSetServiceFactory;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;

/**
 * This is the CDOAwareModelSetServiceFactory type. Enjoy.
 */
public class CDOAwareModelSetServiceFactory extends ModelSetServiceFactory {

	@Override
	public void init(ServicesRegistry servicesRegistry) throws ServiceException {
		super.init(servicesRegistry);
	}

	@Override
	public void disposeService() throws ServiceException {
		try {
			super.disposeService();
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(Messages.CDOAwareModelSetServiceFactory_0, e);
		}
	}

	@Override
	protected ModelSet createModelSet() {
		return new CDOAwareModelSet();
	}

}
