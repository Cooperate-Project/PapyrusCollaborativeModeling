/*****************************************************************************
 * Copyright (c) 2009, 2017 CEA LIST, Christian W. Damus, and others.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA LIST) - adapted for CDO repository resource hyperlinks
 *  Eike Stepper (CEA) - bug 466520
 *  Christian W. Damus - bug 488965
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.ui.hyperlink;

import org.eclipse.emf.cdo.eresource.EresourcePackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.window.Window;
import org.eclipse.papyrus.cdo.internal.ui.dialogs.CheckoutBrowseDialog;
import org.eclipse.papyrus.infra.hyperlink.ui.AbstractEditHyperlinkShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Objects;

/**
 * The editor shell facade for creation and editing of CDO resource hyperlinks.
 */
public class CDOResourceHyperlinkEditorShell extends AbstractEditHyperlinkShell {

	private boolean usedefaultTooltip = true;

	private CDOResourceHyperlink hyperlink;

	/**
	 * Instantiates me.
	 */
	public CDOResourceHyperlinkEditorShell(Shell parentShell) {
		super(parentShell, true);
	}

	public CDOResourceHyperlink getHyperlink() {
		return hyperlink;
	}

	public void setHyperlink(CDOResourceHyperlink hyperlink) {
		this.hyperlink = hyperlink;
		updateFields();
	}

	@Override
	protected void contentsCreated() {
		// intialize "use default" check box
		getUseDefaultCheckBox().setSelection(usedefaultTooltip);
		getObjectLabelText().setEditable(false);
		if (usedefaultTooltip) {
			getTooltipInputText().setEditable(false);
			getTooltipInputText().setText(getObjectLabelText().getText());
		}

		updateFields();
	}

	private void updateFields() {
		if (hyperlink != null) {
			if (getObjectLabelText() != null) {
				getObjectLabelText().setText(hyperlink.getHyperlink().toString());
			}
			if (getTooltipInputText() != null) {
				getTooltipInputText().setText(hyperlink.getTooltipText());
			}

			if ((getObjectLabelText() != null) && (getTooltipInputText() != null)) {
				usedefaultTooltip = Objects.equal(getObjectLabelText().getText(), getTooltipInputText().getText());
				getUseDefaultCheckBox().setSelection(usedefaultTooltip);
				getTooltipInputText().setEditable(!usedefaultTooltip);
			}
		}
	}

	@Override
	protected void onSearch() {
		CheckoutBrowseDialog browse = new CheckoutBrowseDialog(getShell(), Messages.CDOResourceHyperlinkEditorShell_title, Messages.CDOResourceHyperlinkEditorShell_message, null, SWT.OPEN);
		browse.setBlockOnOpen(true);

		String initialURIString = getObjectLabelText().getText().trim();
		if (!initialURIString.isEmpty()) {
			browse.setInitialURI(URI.createURI(initialURIString));
		}

		// select resource nodes of file or model kind, not folders
		browse.setNodeTypeFilter(EresourcePackage.Literals.CDO_RESOURCE_LEAF);

		if (browse.open() == Window.OK) {
			URI selected = browse.getSelectedURI();
			if (selected != null) {
				getObjectLabelText().setText(selected.toString());
			}

			if (usedefaultTooltip) {
				getTooltipInputText().setText(selected.toString());
			}
		}
	}

	@Override
	protected void onUseDefaultTooltip() {
		usedefaultTooltip = getUseDefaultCheckBox().getSelection();
		if (usedefaultTooltip) {
			getTooltipInputText().setEditable(false);
			getTooltipInputText().setText(getObjectLabelText().getText());
		} else {
			getTooltipInputText().setEditable(true);
		}
	}

	@Override
	protected void cancelPressed() {
		hyperlink = null;
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		if (hyperlink == null) {
			hyperlink = new CDOResourceHyperlink();
		}

		hyperlink.setHyperlink(URI.createURI(getObjectLabelText().getText().trim()));
		hyperlink.setTooltipText(getTooltipInputText().getText().trim());

		super.okPressed();
	}
}
