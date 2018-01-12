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

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Tests the renaming of a UML Package sub-model on both sides of a merge to different URIs.
 * <dl>
 * <dt>Origin:</dt>
 * <dd>A class diagram in the root model showing a package containing a class and two other stereotyped
 * elements. This package is a sub-model unit that has its own diagram showing the content of the package,
 * being the class and those stereotyped elements.</dd>
 * <dt>Left:</dt>
 * <dd>The sub-model unit resources to are renamed.</dd>
 * <dt>Right:</dt>
 * <dd>The sub-model unit package is renamed, along with all of the sub-unit resources to match the new
 * package name, resulting in resource names (URIs) that are different to the left side.</dd>
 * </dl>
 */
@SuppressWarnings("nls")
public class ResourceAttachmentChangeRename5GitMergeTest extends AbstractGitMergeTestCase {
	private static final String TEST_SCENARIO_PATH = "testmodels/resourceattachmentchange/rename5/";

	private static final String SUBUNIT_UML = "Subunit1.uml";

	private static final String NEWNAME_UML = "NewName.uml";

	@Override
	protected String getTestScenarioPath() {
		return TEST_SCENARIO_PATH;
	}

	@Override
	protected boolean shouldValidate(File file) {
		return false; // There are no mergeable changes to verify in any file
	}

	@Override
	protected void validateResult() throws Exception {
		assertThat("no conflicts", isConflicting());

		assertThat(asList("NewName.di", "NewName.notation", NEWNAME_UML, //
				"Subunit1.di", "Subunit1.notation", SUBUNIT_UML), //
				everyItem(isConflicted()));
	}

	@Override
	protected void validateResult(Resource resource) throws Exception {
		// Pass
	}
}
