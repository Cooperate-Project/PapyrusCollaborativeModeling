/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.cdo.explorer.CDOExplorerUtil;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckoutManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.cdo.internal.ui.Activator;
import org.eclipse.papyrus.cdo.internal.ui.SharedImages;
import org.eclipse.papyrus.cdo.internal.ui.l10n.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;

/**
 * This is the CheckoutSelectionBlock type. Enjoy.
 */
public class CheckoutSelectionBlock {

	private final EventBus bus;

	private final Supplier<? extends IRunnableContext> runnableContext;

	private TableViewer repoList;

	private CDOCheckout selectedCheckout;

	public CheckoutSelectionBlock(EventBus bus, Supplier<? extends IRunnableContext> runnableContext) {
		this.bus = bus;
		this.runnableContext = runnableContext;
	}

	public Control createControl(Composite parent) {
		CDOCheckoutManager mgr = CDOExplorerUtil.getCheckoutManager();
		repoList = new TableViewer(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(repoList.getControl());
		repoList.setContentProvider(new RepositoryContentProvider());
		repoList.setLabelProvider(new RepositoryLabelProvider());
		repoList.setInput(mgr);

		if (selectedCheckout != null) {
			repoList.setSelection(new StructuredSelection(selectedCheckout));
			selected(selectedCheckout);
		}

		repoList.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				if (sel.isEmpty()) {
					if (selectedCheckout != null) {
						// veto empty selection
						repoList.setSelection(new StructuredSelection(selectedCheckout));
					}
				} else {
					selected((CDOCheckout) sel.getFirstElement());
				}
			}
		});

		// initially select the first connected repo
		for (CDOCheckout next : mgr.getCheckouts()) {

			if (next.isOpen()) {
				selected(next);
				repoList.setSelection(new StructuredSelection(next));
				break;
			}
		}

		return repoList.getControl();
	}

	public void dispose() {
		// pass
	}

	public void setEnabled(boolean enabled) {
		if (repoList != null) {
			repoList.getControl().setEnabled(enabled);
		}
	}

	public boolean isEnabled() {
		return (repoList != null) && repoList.getControl().isEnabled();
	}

	void selected(final CDOCheckout checkout) {
		selectedCheckout = checkout;

		if (!checkout.isOpen()) {
			try {
				runnableContext.get().run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						SubMonitor sub = SubMonitor.convert(monitor, NLS.bind(Messages.CheckoutSelectionBlock_0, checkout.getLabel()), IProgressMonitor.UNKNOWN);

						try {
							checkout.open();

							// yes, it's a busy wait, but there's not much
							// to be done about that.
							final long deadline = System.currentTimeMillis() + 5000L;
							while (!checkout.isOpen()) {
								Thread.sleep(250L);
								if (System.currentTimeMillis() >= deadline) {
									break;
								}
							}

							repoList.getControl().getDisplay().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!repoList.getControl().isDisposed()) {
										repoList.update(checkout, null);
										bus.post(checkout);
									}
								}
							});
						} finally {
							sub.done();
						}
					}
				});
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CheckoutSelectionBlock_1, e), StatusManager.SHOW);
			}
		}

		bus.post(checkout);
	}

	public void setSelectedCheckout(CDOCheckout checkout) {
		this.selectedCheckout = checkout;

		if (repoList != null) {
			repoList.setSelection(new StructuredSelection(selectedCheckout));
			selected(selectedCheckout);
		}
	}

	public CDOCheckout getSelectedCheckout() {
		return selectedCheckout;
	}

	//
	// Nested types
	//

	private static class RepositoryContentProvider implements IStructuredContentProvider {

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			viewer.refresh();
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return CDOExplorerUtil.getCheckoutManager().getCheckouts();
		}

		@Override
		public void dispose() {
			// pass
		}
	}

	private static class RepositoryLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			boolean open = ((CDOCheckout) element).isOpen();
			return SharedImages.getImage(open ? Activator.ICON_OPEN_REPOSITORY : Activator.ICON_CLOSED_REPOSITORY);
		}

		@Override
		public String getText(Object element) {
			return ((CDOCheckout) element).getLabel();
		}
	}
}
