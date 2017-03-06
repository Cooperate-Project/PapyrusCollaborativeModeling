/*
 * Copyright (c) 2015, 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Eike Stepper (CEA) - Initial API and implementation
 *
 */
package org.eclipse.papyrus.cdo.internal.ui.editors;

import org.eclipse.emf.cdo.ui.CDOEditorOpener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author Eike Stepper
 */
public class PapyrusCDOEditorOpener extends CDOEditorOpener.Default {
	public static final String ID = "org.eclipse.papyrus.cdo.ui.editors.PapyrusCDOEditorOpener"; //$NON-NLS-1$

	public PapyrusCDOEditorOpener() {
	}

	@Override
	protected IEditorPart doOpenEditor(IWorkbenchPage page, URI uri) {
		String name = uri.trimFileExtension().lastSegment();

		try {
			return PapyrusCDOEditorManager.INSTANCE.openEditor(page, uri, name);
		} catch (PartInitException e) {
			StatusManager.getManager().handle(e.getStatus(),
					StatusManager.SHOW);
		}

		return null;
	}
}
