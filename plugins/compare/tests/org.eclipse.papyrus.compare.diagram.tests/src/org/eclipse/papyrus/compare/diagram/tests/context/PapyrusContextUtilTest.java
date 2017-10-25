/*******************************************************************************
 * Copyright (c) 2016, 2017 EclipseSource Muenchen GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Dirix - initial API and implementation
 *     Christian W. Damus - bug 516257
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.context;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;

import java.io.IOException;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.compare.diagram.ide.ui.internal.context.PapyrusContextUtil;
import org.eclipse.papyrus.compare.diagram.tests.AbstractTest;
import org.eclipse.papyrus.compare.diagram.tests.DiagramInputData;
import org.eclipse.papyrus.infra.core.resource.sasheditor.DiModel;
import org.eclipse.uml2.uml.profile.standard.StandardFactory;
import org.junit.Test;

@SuppressWarnings({"nls", "boxing" })
public class PapyrusContextUtilTest extends AbstractTest {

	private PapyrusContextUtilInputData input = new PapyrusContextUtilInputData();

	@Test
	public void testPapyrusContext() throws IOException {
		final Resource left = input.getPapyrusLeft();
		final Resource right = input.getPapyrusRight();

		Comparison comparison = buildComparison(left, right);
		assertTrue(PapyrusContextUtil.isPapyrusContext(comparison));
	}

	@Test
	public void testEcoreContext() throws IOException {
		final Resource left = input.getEcoreLeft();
		final Resource right = input.getEcoreRight();

		Comparison comparison = buildComparison(left, right);
		assertFalse(PapyrusContextUtil.isPapyrusContext(comparison));
	}

	@Override
	protected DiagramInputData getInput() {
		return input;
	}

	@Test
	public void testIsUMLResource() throws IOException {
		final Resource left = input.getUMLLeft();
		final Resource right = input.getUMLRight();
		final Predicate<Resource> isUML = PapyrusContextUtil.isUMLResource();

		assertThat("Left should be UML", isUML.apply(left), is(true));
		assertThat("Right should be UML", isUML.apply(right), is(true));

		left.setURI(left.getURI().trimFileExtension().appendFileExtension(DiModel.DI_FILE_EXTENSION));
		assertThat("Left should not look like UML", isUML.apply(left), is(false));
		right.getContents().add(0, StandardFactory.eINSTANCE.createUtility());
		assertThat("Right should not contain UML", isUML.apply(right), is(false));
	}
}
