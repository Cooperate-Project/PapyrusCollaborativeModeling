/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.core.importer.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This is the AllModelImportTests type. Enjoy.
 */
@RunWith(Suite.class)
@SuiteClasses({ModelImportConfigurationTest.class,
	OneToOneModelImportMappingTest.class,
	ManyToOneModelImportMappingTest.class, ModelImporterTest.class})
public class AllModelImportTests {

	public AllModelImportTests() {
		super();
	}

}
