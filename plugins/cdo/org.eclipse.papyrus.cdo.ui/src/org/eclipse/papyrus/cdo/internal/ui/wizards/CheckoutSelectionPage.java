/*****************************************************************************
 * Copyright (c) 2015, 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Eike Stepper (CEA) - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.ui.wizards;

import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.papyrus.cdo.internal.ui.l10n.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Suppliers;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * This is the CheckoutSelectionPage type. Enjoy.
 */
public class CheckoutSelectionPage extends ModelImportWizardPage {

	private static final String MESSAGE = Messages.CheckoutSelectionPage_0;

	private CheckoutSelectionBlock checkoutSelectionBlock;


	private CDOCheckout checkout;

	public CheckoutSelectionPage(EventBus bus) {
		super("repository", Messages.CheckoutSelectionPage_2, null, bus, MESSAGE); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite result = new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(1, false));

		new Label(result, SWT.NONE).setText(Messages.CheckoutSelectionPage_3);

		checkoutSelectionBlock = new CheckoutSelectionBlock(getEventBus(), Suppliers.ofInstance(getContainer()));
		checkoutSelectionBlock.createControl(result);

		if (checkout != null) {
			checkoutSelectionBlock.setSelectedCheckout(checkout);
		}

		setControl(result);

		validatePage();
	}

	@Override
	public void dispose() {
		checkoutSelectionBlock.dispose();

		super.dispose();
	}

	@Subscribe
	public void selected(CDOCheckout checkout) {
		this.checkout = checkout;

		if ((checkoutSelectionBlock != null) && (checkoutSelectionBlock.getSelectedCheckout() != checkout)) {
			checkoutSelectionBlock.setSelectedCheckout(checkout);
		}
	}

	@Override
	protected Diagnostic doValidatePage() {
		Diagnostic result = Diagnostic.CANCEL_INSTANCE;

		if (getSelectedRepository() != null) {
			result = Diagnostic.OK_INSTANCE;
		}

		return result;
	}

	public CDOCheckout getSelectedRepository() {
		return checkout;
	}
}
