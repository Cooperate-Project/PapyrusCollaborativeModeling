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
package org.eclipse.papyrus.cdo.internal.ui.dialogs;

import org.eclipse.emf.cdo.eresource.CDOResourceFolder;
import org.eclipse.emf.cdo.eresource.CDOResourceNode;
import org.eclipse.emf.cdo.eresource.EresourcePackage;
import org.eclipse.emf.cdo.explorer.CDOExplorerUtil;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.util.CDOURIUtil;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.cdo.view.CDOViewInvalidationEvent;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.ui.views.IElementFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.cdo.internal.core.CDOUtils;
import org.eclipse.papyrus.cdo.internal.ui.l10n.Messages;
import org.eclipse.papyrus.cdo.internal.ui.util.UIUtil;
import org.eclipse.papyrus.cdo.internal.ui.views.CheckoutItemProvider;
import org.eclipse.papyrus.cdo.internal.ui.widgets.ActionButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CreateFolderAction;

import com.google.common.base.Strings;


/**
 * This is the CheckoutBrowseDialog type. Enjoy.
 */
public class CheckoutBrowseDialog extends TitleAreaDialog {

	private final String windowTitle;

	private final String title;

	private final String message;

	private final int style;

	private final CDOCheckout checkout;

	private boolean allowOverwrite = false;

	private EClass nodeTypeFilter = EresourcePackage.Literals.CDO_RESOURCE_NODE;

	private TreeViewer tree;

	private Text nameText;

	private URI initialURI;

	private CDOResourceNode selection;

	private String name;

	private IListener viewerRefresh;

	/**
	 * @param style
	 *            one of {@link SWT#OPEN} or {@link SWT#SAVE}
	 */
	public CheckoutBrowseDialog(Shell parentShell, String title, String message, CDOView view, int style) {
		this(parentShell, Messages.CheckoutBrowseDialog_windowTitle, title, message, view, style);
	}

	/**
	 * @param style
	 *            one of {@link SWT#OPEN} or {@link SWT#SAVE}
	 */
	public CheckoutBrowseDialog(Shell parentShell, String windowTitle, String title, String message, CDOView view, int style) {
		super(parentShell);

		this.windowTitle = windowTitle;
		this.title = title;
		this.message = message;
		this.style = checkStyle(style);

		this.checkout = (view == null) ? null : CDOExplorerUtil.getCheckout(view);

		setHelpAvailable(false);
	}

	int checkStyle(int style) {
		setShellStyle(getShellStyle() | (style & ~(SWT.OPEN | SWT.SAVE)));

		return style & (SWT.OPEN | SWT.SAVE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(windowTitle);

		newShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				shellDisposed();
			}
		});
	}

	private void shellDisposed() {
		if (viewerRefresh != null) {
			for (CDOCheckout next : CDOExplorerUtil.getCheckoutManager().getCheckouts()) {
				if (next.isOpen()) {
					next.getView().removeListener(viewerRefresh);
				}
			}
		}
	}

	public boolean isAllowOverwrite() {
		return allowOverwrite;
	}

	public void setAllowOverwrite(boolean allowOverwrite) {
		this.allowOverwrite = allowOverwrite;
	}

	public EClass getNodeTypeFilter() {
		return nodeTypeFilter;
	}

	public void setNodeTypeFilter(EClass nodeTypeFilter) {
		boolean defaulted = (nodeTypeFilter == null) || !EresourcePackage.Literals.CDO_RESOURCE_NODE.isSuperTypeOf(nodeTypeFilter);
		this.nodeTypeFilter = defaulted ? EresourcePackage.Literals.CDO_RESOURCE_NODE : nodeTypeFilter;
	}

	public URI getInitialURI() {
		return initialURI;
	}

	public void setInitialURI(URI initialURI) {
		this.initialURI = initialURI;
	}

	public URI getSelectedURI() {
		return (getReturnCode() != Window.OK) ? null : basicGetSelectedURI();
	}

	private URI basicGetSelectedURI() {
		URI result = null;

		if (selection != null) {
			result = selection.getURI();
			if (name != null) {
				String resourceName = Strings.emptyToNull(name);
				if (resourceName != null) {
					if (!(selection instanceof CDOResourceFolder)) {
						// append the name to the parent folder
						result = result.trimSegments(1);
					}
					result = result.appendSegment(resourceName);
				}
			}
		}

		return result;
	}

	boolean isOpenStyle() {
		return (style & SWT.OPEN) != 0;
	}

	boolean isSaveStyle() {
		return (style & SWT.SAVE) != 0;
	}

	CDOCheckout getCheckout() {
		return checkout;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(title);
		setMessage(message);

		Composite main = (Composite) super.createDialogArea(parent);
		Composite result = new Composite(main, SWT.NONE);
		result.setLayout(new GridLayout(isSaveStyle() ? 3 : 1, false));
		result.setLayoutData(new GridData(GridData.FILL_BOTH));

		tree = new TreeViewer(result, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, isSaveStyle() ? 2 : 1, 1));

		CheckoutItemProvider itemProvider = (getCheckout() == null) ? new CheckoutItemProvider(null, showConnectedRepositories()) : new CheckoutItemProvider(null, getCheckout());
		tree.setContentProvider(itemProvider);
		tree.setLabelProvider(new DecoratingLabelProvider(itemProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		tree.setSorter(itemProvider);
		tree.setInput(CDOExplorerUtil.getCheckoutManager());
		for (CDOCheckout next : CDOExplorerUtil.getCheckoutManager().getCheckouts()) {
			if (next.isOpen()) {
				next.getView().addListener(getViewerRefresh());
			}
		}

		if (isSaveStyle()) {
			// don't need to create new folders when opening an existing resource
			Composite actionButtons = new Composite(result, SWT.NONE);
			actionButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			actionButtons.setLayout(new RowLayout(SWT.VERTICAL));

			ActionButton newFolderButton = new ActionButton(Messages.CheckoutBrowseDialog_newFolderAction, new CreateFolderAction(this), SWT.PUSH);
			newFolderButton.attach(tree);
			newFolderButton.createControl(actionButtons);

			// don't need to type in names of non-existent resources when opening an existing resource
			Label label = new Label(result, SWT.NONE);
			label.setText(Messages.CheckoutBrowseDialog_nameLabel);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gd.verticalIndent = convertVerticalDLUsToPixels(8);
			label.setLayoutData(gd);

			nameText = new Text(result, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			gd.verticalIndent = convertVerticalDLUsToPixels(8);
			nameText.setLayoutData(gd);
		}

		if (getInitialURI() != null) {
			initializeSelection();
		} else if (getCheckout() != null) {
			selection = getCheckout().getView().getRootResource();
		}

		tree.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					if (getCheckout() != null) {
						selection = getCheckout().getView().getRootResource();
					} else {
						selection = null;
					}
				} else {
					selection = CDOUtils.adapt(sel.getFirstElement(), CDOResourceNode.class);
				}

				validateSelection();
			}
		});

		if (nameText != null) {
			nameText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					name = Strings.emptyToNull(nameText.getText().trim());
					validateSelection();
				}
			});
		}

		return result;
	}

	private IElementFilter showConnectedRepositories() {
		return new IElementFilter() {

			@Override
			public boolean filter(Object element) {
				boolean result = false;

				if (element instanceof CDOCheckout) {
					result = ((CDOCheckout) element).isOpen();
				}

				return result;
			}
		};
	}

	private void initializeSelection() {
		URI uri = getInitialURI();
		CDOView view = getCheckout().getView();

		String path = CDOURIUtil.extractResourcePath(uri);
		if (view.hasResource(path)) {
			selection = view.getResourceNode(path);
			tree.setSelection(new StructuredSelection(selection));
			tree.expandToLevel(selection, 1);
			if (nameText != null) {
				name = selection.getName();
				nameText.setText(name);
			}
		} else if (uri.segmentCount() > 0) {
			// try the containing folder, then
			URI parent = uri.trimSegments(1);
			path = CDOURIUtil.extractResourcePath(parent);
			if (view.hasResource(path)) {
				// if the root resource, it is represented by the checkout
				selection = view.getResourceNode(path);
				Object treeSelection = selection.isRoot() ? getCheckout() : selection;
				tree.setSelection(new StructuredSelection(treeSelection), true);
				tree.expandToLevel(treeSelection, 1);
				if (nameText != null) {
					name = uri.lastSegment();
					nameText.setText(name);
				}
			}
		} else {
			// select the checkout (representing the root resource)
			selection = view.getRootResource();
			Object treeSelection = getCheckout();
			tree.setSelection(new StructuredSelection(treeSelection), true);
			tree.expandToLevel(treeSelection, 1);
		}
	}

	private void validateSelection() {
		String error = null;
		String warning = null;
		String info = null;
		boolean disable = false;

		if (name != null) {
			if (!URI.validSegment(name)) {
				error = Messages.CheckoutBrowseDialog_invalidURISeg;
			}
		}

		if (error == null) {
			if (selection == null) {
				disable = true;
			} else {
				String path = CDOURIUtil.extractResourcePath(basicGetSelectedURI());
				CDOView view = selection.cdoView();

				if (isOpenStyle() && !view.hasResource(path)) {
					error = Messages.CheckoutBrowseDialog_noSuchResource;
				} else if (isOpenStyle()) {
					// then the resource exists. Is it the kind we want?
					CDOResourceNode node = view.getResourceNode(path);
					if (!getNodeTypeFilter().isInstance(node)) {
						disable = true;
						info = NLS.bind(Messages.CheckoutBrowseDialog_wrongSelection, getNodeType(getNodeTypeFilter()));
					}
				} else if (isSaveStyle() && view.hasResource(path)) {
					if (isAllowOverwrite()) {
						warning = Messages.CheckoutBrowseDialog_existsWarning;
					} else {
						error = Messages.CheckoutBrowseDialog_existsError;
					}
				}
			}
		}

		getButton(IDialogConstants.OK_ID).setEnabled(!disable && (error == null));
		if (error != null) {
			setMessage(error, IMessageProvider.ERROR);
		} else if (warning != null) {
			setMessage(warning, IMessageProvider.WARNING);
		} else if (info != null) {
			setMessage(info, IMessageProvider.INFORMATION);
		} else {
			setMessage(message);
		}
	}

	private String getNodeType(EClass nodeClass) {
		String result = Messages.CheckoutBrowseDialog_anyNode;

		if (nodeClass.getEPackage() == EresourcePackage.eINSTANCE) {
			switch (nodeClass.getClassifierID()) {
			case EresourcePackage.CDO_RESOURCE:
				result = Messages.CheckoutBrowseDialog_modelKind;
				break;
			case EresourcePackage.CDO_RESOURCE_FOLDER:
				result = Messages.CheckoutBrowseDialog_folderKind;
				break;
			case EresourcePackage.CDO_TEXT_RESOURCE:
				result = Messages.CheckoutBrowseDialog_textKind;
				break;
			case EresourcePackage.CDO_BINARY_RESOURCE:
				result = Messages.CheckoutBrowseDialog_binKind;
				break;
			case EresourcePackage.CDO_FILE_RESOURCE:
				result = Messages.CheckoutBrowseDialog_fileKind;
				break;
			case EresourcePackage.CDO_RESOURCE_LEAF:
				result = Messages.CheckoutBrowseDialog_leafKind;
				break;
			}
		}

		return result;
	}

	private IListener getViewerRefresh() {
		if (viewerRefresh == null) {
			viewerRefresh = new IListener() {

				@Override
				public void notifyEvent(IEvent event) {
					if (event instanceof CDOViewInvalidationEvent) {
						if ((getContents() != null) && !getContents().isDisposed()) {
							if (UIUtil.ensureUIThread(this, event)) {
								tree.refresh();
							}
						}
					}
				}
			};
		}

		return viewerRefresh;
	}
}
