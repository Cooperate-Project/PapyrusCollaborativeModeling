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
package org.eclipse.papyrus.compare.diagram.tests.css.data;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.compare.diagram.tests.DiagramInputDataWithResourceSetHooks;

@SuppressWarnings("nls")
public class CSSInputData extends DiagramInputDataWithResourceSetHooks {

	public Resource getLeft() throws IOException {
		return loadFromClassLoader("left.notation");
	}

	public Resource getRight() throws IOException {
		return loadFromClassLoader("right.notation");
	}

}
