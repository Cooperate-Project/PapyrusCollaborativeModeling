/*******************************************************************************
 * Copyright (C) 2015, 2018 EclipseSource Munich Gmbh and Others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Philip Langer - initial API and implementation
 *     Christian W. Damus - bug 529217
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.egit;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Tests the renaming of a UML Package sub-model that is resobed on another branch. This scenario contains two
 * diagrams, one for the root of the model and another for the extracted package.
 * <dl>
 * <dt>Origin:</dt>
 * <dd>A class diagram in the root model showing a package containing a class. This package is a sub-model
 * unit that has its own diagram showing the content of the package, being the class.</dd>
 * <dt>Left:</dt>
 * <dd>The sub-model unit is resorbed.</dd>
 * <dt>Right:</dt>
 * <dd>The sub-model unit package is renamed, along with all of the sub-unit resources to match the new
 * package name.</dd>
 * </dl>
 *
 * @author Philip Langer <planger@eclipsesource.com>
 */
@SuppressWarnings("nls")

public class ResourceAttachmentChangeRename2GitMergeTest extends AbstractGitMergeTestCase {

	private static final String TEST_SCENARIO_PATH = "testmodels/resourceattachmentchange/rename2/";

	private static final String SUBUNIT_UML = "Subunit1.uml";

	private static final String MODEL_UML = "model.uml";

	@Override
	protected String getTestScenarioPath() {
		return TEST_SCENARIO_PATH;
	}

	@Override
	protected boolean shouldValidate(File file) {
		return file.getName().equals(MODEL_UML) || file.getName().equals(SUBUNIT_UML);
	}

	@Override
	protected void validateResult() throws Exception {
		assertThat("no conflicts", isConflicting());

		assertThat(asList("model.di", "model.notation", MODEL_UML), //
				everyItem(fileExists()));

		assertThat(asList("model.notation", MODEL_UML, //
				"Subunit1.notation", "Subunit1.di", SUBUNIT_UML), //
				everyItem(isConflicted()));
		assertThat("model.di", not(isConflicted()));
	}

	@Override
	protected void validateResult(Resource resource) throws Exception {
		// Nothing further to check (every scenario conflicts)
	}
}
