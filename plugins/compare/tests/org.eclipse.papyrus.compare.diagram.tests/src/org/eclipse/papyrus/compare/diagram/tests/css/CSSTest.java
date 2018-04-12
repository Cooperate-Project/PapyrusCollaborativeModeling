/*******************************************************************************
 * Copyright (c) 2017 Christian W. Damus and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Christian W. Damus - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.css;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.compare.diagram.tests.AbstractTest;
import org.eclipse.papyrus.compare.diagram.tests.DiagramInputData;
import org.eclipse.papyrus.compare.diagram.tests.css.data.CSSInputData;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;
import org.junit.Test;

/**
 * Test cases for CSS and other {@link ViewPrototype}-related concerns.
 *
 * @author Christian W. Damus
 */
@SuppressWarnings({"nls", "boxing" })
public class CSSTest extends AbstractTest {

	private final CSSInputData input = new CSSInputData();

	/**
	 * Verify that Papyrus finds the correct view prototype for the compared diagrams.
	 */
	@Test
	public void viewPrototype() throws IOException {
		Resource left = input.getLeft();
		Resource right = input.getRight();

		buildComparison(left, right);

		Diagram diagram = (Diagram)EcoreUtil.getObjectByType(left.getContents(),
				NotationPackage.Literals.DIAGRAM);
		ViewPrototype proto = ViewPrototype.get(diagram);
		assertThat("View prototype not found", proto.isUnavailable(), is(false));
		assertThat(proto.getRepresentationKind().getId(), containsString("sysml14"));
	}

	@Override
	protected DiagramInputData getInput() {
		return input;
	}

}
