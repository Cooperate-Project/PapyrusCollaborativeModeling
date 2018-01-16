/*******************************************************************************
 * Copyright (c) 2016, 2018 EclipseSource Services GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Martin Fleck - initial API and implementation
 *     Christian W. Damus - bug 529897
 *******************************************************************************/
package org.eclipse.papyrus.compare.uml2.tests.suite;

import org.eclipse.papyrus.compare.uml2.tests.profiles.migration.ModelSetWrapperTest;
import org.eclipse.papyrus.compare.uml2.tests.profiles.migration.ProfileMigrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the Papyrus Compare UML2 tests.
 * 
 * @author Martin Fleck <mfleck@eclipsesource.com>
 */
@RunWith(Suite.class)
@SuiteClasses({ProfileMigrationTest.class, //
		ModelSetWrapperTest.class, //
})
public class AllUITests {

}
