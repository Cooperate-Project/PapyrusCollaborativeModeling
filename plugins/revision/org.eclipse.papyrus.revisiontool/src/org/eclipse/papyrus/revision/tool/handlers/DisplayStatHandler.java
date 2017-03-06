/*****************************************************************************
 * Copyright (c) 2014, 2017 CEA LIST, Christian W. Damus, and others.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus - bug 468079
 *  Christian W. Damus - bug 471836
 *
 *****************************************************************************/
package org.eclipse.papyrus.revision.tool.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.revision.tool.core.ReviewResourceManager;
import org.eclipse.papyrus.revision.tool.ui.ReviewsEditor;
import org.eclipse.papyrus.revision.tool.ui.SWTQualitativeInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Model;

/**
 * This class is used to create and html developper doc file.
 *
 */
public class DisplayStatHandler extends IDMAbstractHandler {

	protected static final String INTERNAL_DIRECTORY_NAME = "/doc"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.papyrus.revisiontool.commentview");
		if(part instanceof ReviewsEditor) {
			ReviewResourceManager reviewResourceManager = ((ReviewsEditor)part).getReviewResourceManager();
			if(reviewResourceManager.getCurrentReviewModelWithoutLoading() != null) {
				Comparison comparison = reviewResourceManager.getDiffModel();
				Model model = (Model)getSelection();
				int elementNumber = 0;
				int addedElement = 0;
				int modifiedElement = 0;
				int removedElement = 0;
				int stableElement = 0;
				Iterator<EObject> iterator = model.eAllContents();
				while(iterator.hasNext()) {
					EObject eObject = iterator.next();
					elementNumber++;
					if(comparison.getMatch(eObject) != null) {
						modifiedElement++;
					}
					if(comparison.getDifferences(eObject).size() > 0) {
						if(comparison.getDifferences(eObject).get(0).getKind().equals(DifferenceKind.DELETE) && isChangeAboutContaiment(comparison, eObject)) {
							removedElement++;
						} else if(comparison.getDifferences(eObject).get(0).getKind().equals(DifferenceKind.ADD) && isChangeAboutContaiment(comparison, eObject)) {
							addedElement++;
						} else if(comparison.getDifferences(eObject).get(0).getKind().equals(DifferenceKind.CHANGE)) {
							modifiedElement++;
						}
					} else {
						stableElement++;
					}
				}
				final Shell shell = new Shell(Display.getCurrent());
				int pourcentA = addedElement * 100 / elementNumber;
				if(pourcentA == 0 && addedElement > 0) {
					pourcentA = 1;
				}
				int pourcentM = modifiedElement * 100 / elementNumber;
				if(pourcentM == 0 && modifiedElement > 0) {
					pourcentM = 1;
				}
				int pourcentR = removedElement * 100 / elementNumber;
				if(pourcentR == 0 && removedElement > 0) {
					pourcentR = 1;
				}
				int pourcentS = stableElement * 100 / elementNumber;
				if(pourcentS == 0 && stableElement > 0) {
					pourcentS = 1;
				}
				int[] pourcent = { pourcentA, pourcentM, pourcentR, pourcentS };
				String[] labels = { "Added " + addedElement + ":", "Modified " + modifiedElement + ":", "Deleted " + removedElement + ":", "Stable " + stableElement + ":" };
				Device device = Display.getCurrent();
				org.eclipse.swt.graphics.Color[] colors = { device.getSystemColor(SWT.COLOR_GREEN), device.getSystemColor(SWT.COLOR_BLUE), device.getSystemColor(SWT.COLOR_RED), device.getSystemColor(SWT.COLOR_GRAY) };
				SWTQualitativeInfo canva = new SWTQualitativeInfo(shell, SWT.NULL, pourcent, labels, colors, SWTQualitativeInfo.HISTOGRAMME);
				shell.setLayout(new FillLayout());
				shell.pack();
				shell.setSize(300, 300);
				shell.setText("Statistic about modifications");
				shell.open();
			}
		}
		return null;
	}

	protected boolean isChangeAboutContaiment(Comparison comparison, EObject element) {
		if(comparison.getDifferences(element).get(0) instanceof ReferenceChange) {
			ReferenceChange refChange = (ReferenceChange)comparison.getDifferences(element).get(0);
			if(refChange.getReference().isContainment()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		if(getSelection() instanceof Model) {
			Model model = (Model)getSelection();
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.papyrus.revisiontool.commentview");
			if(part instanceof ReviewsEditor) {
				ReviewResourceManager reviewResourceManager = ((ReviewsEditor)part).getReviewResourceManager();
				if(reviewResourceManager.getCurrentReviewModelWithoutLoading() != null) {
					Comparison comparison = reviewResourceManager.getDiffModel();
					if(comparison != null) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
