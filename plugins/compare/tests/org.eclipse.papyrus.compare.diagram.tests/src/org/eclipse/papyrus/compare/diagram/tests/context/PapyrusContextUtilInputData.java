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

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.compare.diagram.tests.DiagramInputData;

@SuppressWarnings("nls")
public class PapyrusContextUtilInputData extends DiagramInputData {

	public Resource getPapyrusLeft() throws IOException {
		return loadFromClassLoader("data/papyrus/left.di");
	}

	public Resource getPapyrusRight() throws IOException {
		return loadFromClassLoader("data/papyrus/right.di");
	}

	public Resource getUMLLeft() throws IOException {
		return loadFromClassLoader("data/papyrus/left.uml");
	}

	public Resource getUMLRight() throws IOException {
		return loadFromClassLoader("data/papyrus/right.uml");
	}

	public Resource getEcoreLeft() throws IOException {
		return loadFromClassLoader("data/ecore/left.ecore");
	}

	public Resource getEcoreRight() throws IOException {
		return loadFromClassLoader("data/ecore/right.ecore");
	}
}
