/*******************************************************************************
 * Copyright (C) 2017 Christian W. Damus and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Christian W. Damus - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.egit;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.ide.ui.tests.framework.ResolutionStrategyID;
import org.eclipse.emf.compare.ide.ui.tests.framework.annotations.ResolutionStrategies;
import org.eclipse.emf.compare.ide.ui.tests.git.framework.GitTestRunner;
import org.eclipse.emf.compare.ide.ui.tests.git.framework.GitTestSupport;
import org.eclipse.emf.compare.ide.ui.tests.git.framework.annotations.GitInput;
import org.eclipse.emf.compare.ide.ui.tests.git.framework.annotations.GitMerge;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

/**
 * Test that a class refactored as a sub-unit does not lose its stereotype applications when merged into a
 * branch that has the class integrated.
 *
 * @author Christian W. Damus
 * @see <a href="http://eclip.se/526932">bug 526932</a>
 */
@SuppressWarnings({"nls", "boxing" })
@RunWith(GitTestRunner.class)
@ResolutionStrategies(ResolutionStrategyID.WORKSPACE)
public class StereotypeApplicationsInSubunitsTest {

	@GitMerge(local = "left", remote = "right-extract-submodel")
	@GitInput("data/bug526932.zip")
	public void stereotypeApplicationNotLost(GitTestSupport testSupport) throws Exception {
		// Check that the merge completed

		assertThat(testSupport.noConflict(), is(true));
		assertThat(testSupport.getMergeResult().getMergeStatus().isSuccessful(), is(true));
		assertThat(testSupport.getStatus().hasUncommittedChanges(), is(false));

		// Check that all the files we expect are present

		String pattern = "Plant/%s.%s";
		List<String> extensions = Arrays.asList("di", "notation", "uml");
		List<String> units = Arrays.asList("Plant", "Plant_Block");
		List<String> expectedFiles = Lists.newArrayListWithCapacity(units.size() * extensions.size());
		for (String unit : units) {
			for (String ext : extensions) {
				expectedFiles.add(String.format(pattern, unit, ext));
			}
		}

		assertThat(expectedFiles, everyItem(fileExists(testSupport)));

		// Check the stereotype applications

		URI resourceURI = URI.createPlatformResourceURI("Plant/Plant_Block.uml", true);
		ModelSet mset = new ModelSet();

		try {
			mset.loadModels(resourceURI);
			org.eclipse.uml2.uml.Class class_ = UML2Util.load(mset, resourceURI, UMLPackage.Literals.CLASS);
			assertThat(class_, isApplied("SysML::Blocks::Block"));
		} finally {
			mset.unload();
		}
	}

	//
	// Test framework
	//

	/**
	 * Assertion that a file exists according to a Git test {@code support}.
	 * 
	 * @param support
	 *            the Git test support
	 * @return the file-exists assertion
	 */
	Matcher<String> fileExists(final GitTestSupport support) {
		return new CustomTypeSafeMatcher<String>("exists") {

			@Override
			protected boolean matchesSafely(String item) {
				return support.fileExists(item);
			}
		};
	}

	/**
	 * Assertion that an UML element has a {@code stereotype} applied.
	 * 
	 * @param stereotype
	 *            the stereotype that is expected to be applied
	 * @return the is-applied assertion
	 */
	Matcher<Element> isApplied(final String stereotype) {
		return new CustomTypeSafeMatcher<Element>("has " + stereotype + " applied") {

			@Override
			protected boolean matchesSafely(Element item) {
				Stereotype applied = item.getAppliedStereotype(stereotype);
				return applied != null;
			}
		};
	}
}
