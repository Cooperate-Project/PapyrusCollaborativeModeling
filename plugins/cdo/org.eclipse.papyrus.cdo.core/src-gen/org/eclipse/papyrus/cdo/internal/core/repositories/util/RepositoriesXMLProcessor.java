/**
 * Copyright (c) 2013, 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 */
package org.eclipse.papyrus.cdo.internal.core.repositories.util;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.util.XMLProcessor;
import org.eclipse.papyrus.cdo.internal.core.repositories.RepositoriesPackage;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class RepositoriesXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 */
	public RepositoriesXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		RepositoriesPackage.eINSTANCE.eClass();
	}

	/**
	 * Register for "*" and "xml" file extensions the RepositoriesResourceFactoryImpl factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new RepositoriesResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new RepositoriesResourceFactoryImpl());
		}
		return registrations;
	}

} // RepositoriesXMLProcessor
