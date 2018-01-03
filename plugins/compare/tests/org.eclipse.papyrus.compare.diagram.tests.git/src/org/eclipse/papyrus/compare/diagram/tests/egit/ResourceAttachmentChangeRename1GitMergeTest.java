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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Tests the renaming of a UML Package sub-model that has internal changes on another branch. This scenario
 * contains two diagrams, one for the root of the model and another for the extracted package.
 * <dl>
 * <dt>Origin:</dt>
 * <dd>A class diagram in the root model showing a package containing a class. This package is a sub-model
 * unit that has its own diagram showing the content of the package, being the class.</dd>
 * <dt>Left:</dt>
 * <dd>The one class in the sub-model unit is renamed and another class added in the package. The layout of
 * both classes in the sub-model unit's diagram is changed.</dd>
 * <dt>Right:</dt>
 * <dd>The sub-model unit package is renamed, along with all of the sub-unit resources to match the new
 * package name.</dd>
 * </dl>
 *
 * @author Philip Langer <planger@eclipsesource.com>
 */
@SuppressWarnings("nls")
public class ResourceAttachmentChangeRename1GitMergeTest extends AbstractGitMergeTestCase {

	private static final String TEST_SCENARIO_PATH = "testmodels/resourceattachmentchange/rename1/";

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
		assertThat("conflicts remain", noConflict());

		assertThat(asList("model.di", "model.notation", MODEL_UML, //
				"Subunit1.di", "Subunit1.notation", SUBUNIT_UML), //
				everyItem(fileExists()));

		assertThat(asList("Package1.di", "Package1.notation", "Package1.uml"), //
				everyItem(not(fileExists())));
	}

	@Override
	protected void validateResult(Resource resource) throws Exception {
		switch (resource.getURI().lastSegment()) {
			case MODEL_UML:
				checkModelResource(resource);
				break;
			case SUBUNIT_UML:
				checkSubunitResource(resource);
				break;
		}
	}

	private void checkModelResource(Resource resource) {
		assertThat(resource.getContents(), hasItem(any(Model.class)));
		final Model model = (Model)EcoreUtil.getObjectByType(resource.getContents(),
				UMLPackage.Literals.MODEL);

		assertThat(model.getNestedPackages(), hasItem(any(Package.class)));
		final Package package_ = model.getNestedPackages().get(0);

		checkLeftChanges(package_);
		checkRightChanges(package_);
	}

	private void checkLeftChanges(Package package_) {
		assertThat(package_.getOwnedType("Foo"), notNullValue());
		assertThat(package_.getOwnedType("Bar"), notNullValue());
	}

	private void checkRightChanges(Package package_) {
		assertThat(package_.getName(), is("Subunit1"));
	}

	private void checkSubunitResource(Resource resource) {
		assertThat(resource.getContents(), hasItem(any(Package.class)));
		final Package package_ = (Package)EcoreUtil.getObjectByType(resource.getContents(),
				UMLPackage.Literals.PACKAGE);

		checkLeftChanges(package_);
		checkRightChanges(package_);
	}
}
