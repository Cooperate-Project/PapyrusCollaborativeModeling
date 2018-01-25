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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Tests the renaming of a UML Package sub-model that has stereotyped elements moved out of it on another
 * branch. This scenario contains two diagrams, one for the root of the model and another for the extracted
 * package.
 * <dl>
 * <dt>Origin:</dt>
 * <dd>A class diagram in the root model showing a package containing a class and two other stereotyped
 * elements. This package is a sub-model unit that has its own diagram showing the content of the package,
 * being the class and those stereotyped elements.</dd>
 * <dt>Left:</dt>
 * <dd>The two stereotype elements are moved out of the sub-unit package into the root package. The layout of
 * both diagrams is changed: in the root package to move the two shapes out of the nested package shape, and
 * in the sub-unit package's diagram to delete the shapes for the two elements.</dd>
 * <dt>Right:</dt>
 * <dd>The sub-model unit package is renamed, along with all of the sub-unit resources to match the new
 * package name.</dd>
 * </dl>
 */
@SuppressWarnings("nls")
public class ResourceAttachmentChangeRename4GitMergeTest extends AbstractGitMergeTestCase {

	private static final String TEST_SCENARIO_PATH = "testmodels/resourceattachmentchange/rename4/";

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
		final Model model = (Model)package_.getNestingPackage();
		Type strings = model.getOwnedType("Strings");
		assertThat(strings, stereotypedAs("Utility"));
		assertThat(strings.getStereotypeApplications(), everyItem(storedIn(strings.eResource())));
		Package jface = model.getNestedPackage("jface");
		assertThat(jface, stereotypedAs("Framework"));
		assertThat(jface.getStereotypeApplications(), everyItem(storedIn(jface.eResource())));

		assertThat(package_.getOwnedTypes(), not(empty()));
		assertThat(package_.getOwnedType("Strings"), nullValue());
		assertThat(package_.getNestedPackage("jface"), nullValue());
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
