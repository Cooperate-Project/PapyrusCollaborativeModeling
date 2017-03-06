/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus (CEA) - bug 422257
 *   Eike Stepper (CEA) - bug 466520
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.ui.editors;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.explorer.ui.checkouts.CDOModelEditorOpener;
import org.eclipse.emf.cdo.internal.ui.InteractiveConflictHandlerSelector;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.ui.CDOInvalidRootAgent;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.internal.cdo.transaction.CDOHandlingConflictResolver;
import org.eclipse.emf.spi.cdo.CDOMergingConflictResolver;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.net4j.util.ReflectUtil;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.papyrus.cdo.core.IResourceSetDisposalApprover;
import org.eclipse.papyrus.cdo.internal.ui.Activator;
import org.eclipse.papyrus.cdo.internal.ui.l10n.Messages;
import org.eclipse.papyrus.cdo.internal.ui.util.UIUtil;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IComponentPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IEditorPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPageLifeCycleEventsListener;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPageVisitor;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ISashWindowsContainer;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.AdapterUtils;
import org.eclipse.papyrus.infra.ui.editor.CoreMultiDiagramEditor;
import org.eclipse.papyrus.infra.ui.util.EditorUtils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This is the PapyrusCDOEditorManager type. Enjoy.
 */
public class PapyrusCDOEditorManager {

	public static final PapyrusCDOEditorManager INSTANCE = new PapyrusCDOEditorManager();

	  private static final boolean INTERACTIVE_CONFLICT_RESOLUTION = !"false"
		      .equalsIgnoreCase(System.getProperty("cdo.interactive.conflict.resolution"));

	private final BiMap<IEditorPart, CDOView> editors = HashBiMap.create();

	private final Map<IEditorPart, Map<IEditorPart, CDOInvalidRootAgent>> invalidRootAgents = new HashMap();

	private final Cache<IWorkbenchPage, EditorListener> editorListeners = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<IWorkbenchPage, EditorListener>() {

		@Override
		public EditorListener load(IWorkbenchPage key) {
			EditorListener result = new EditorListener();
			key.addPartListener(result);
			return result;
		}
	});

	private IResourceSetDisposalApprover disposalApprover = new ResourceSetDisposalApprover();

	private PapyrusCDOEditorManager() {
		super();
	}

	public IEditorPart openEditor(IWorkbenchPage page, CDOResource diResource) throws PartInitException {
		URI uri = diResource.getURI();
		String lastSegment = uri.trimFileExtension().lastSegment();
		return openEditor(page, uri, lastSegment);
	}

	public IEditorPart openEditor(IWorkbenchPage page, URI uri, String name) throws PartInitException {
		return openEditor(page, new PapyrusCDOEditorInput(uri, name));
	}

	public IEditorPart openEditor(IWorkbenchPage page, IEditorInput input) throws PartInitException {
		URI uri = EditorUtils.getResourceURI(input);

		// FIXME(Eike) Port to an API on CDOCheckouts that allows for veto of user-initiated closing of a checkout
		// IInternalPapyrusRepository repository = getRepository(uri);
		// repository.addResourceSetDisposalApprover(disposalApprover);

		IEditorPart result = page.openEditor(input, PapyrusMultiDiagramEditor.EDITOR_ID);

		final EditingDomain domain = result.getAdapter(EditingDomain.class);
		ResourceSet resourceSet = domain.getResourceSet();

		CDOView view = CDOUtil.getView(resourceSet);
		add(view, result);

		if (view instanceof CDOTransaction) {
			CDOTransaction transaction = (CDOTransaction) view;

			if (INTERACTIVE_CONFLICT_RESOLUTION) {
				CDOHandlingConflictResolver conflictResolver = new CDOHandlingConflictResolver() {
					@Override
					protected boolean handleConflict(final ConflictHandler conflictHandler, final long lastNonConflictTimeStamp) {
						if (domain instanceof TransactionalEditingDomain) {
							TransactionalEditingDomain transactionalDomain = (TransactionalEditingDomain) domain;
							final boolean[] handled = { false };

							CommandStack commandStack = domain.getCommandStack();
							commandStack.execute(new RecordingCommand(transactionalDomain) {
								@Override
								protected void doExecute() {
									handled[0] = superHandleConflict(conflictHandler, lastNonConflictTimeStamp);
								}
							});

							return handled[0];
						}

						return superHandleConflict(conflictHandler, lastNonConflictTimeStamp);
					}

					private boolean superHandleConflict(final ConflictHandler conflictHandler, final long lastNonConflictTimeStamp) {
						return super.handleConflict(conflictHandler, lastNonConflictTimeStamp);
					}
				};

				conflictResolver.setConflictHandlerSelector(new InteractiveConflictHandlerSelector());
				transaction.options().addConflictResolver(conflictResolver);
			} else {
				transaction.options().addConflictResolver(new CDOMergingConflictResolver());
			}

			ServicesRegistry services = result.getAdapter(ServicesRegistry.class);
			view.addListener(new PapyrusTransactionListener(services, resourceSet));
		}

		return result;
	}

	void add(final CDOView view, final IEditorPart editor) {
		editors.put(editor, view);

		if (view.isReadOnly() && editor instanceof CoreMultiDiagramEditor) {
			final CoreMultiDiagramEditor coreEditor = (CoreMultiDiagramEditor) editor;
			ISashWindowsContainer sashWindowsContainer = coreEditor.getISashWindowsContainer();

			for (IEditorPart editorPart : sashWindowsContainer.getVisibleIEditorParts()) {
				addInvalidRootAgent(coreEditor, editorPart, view);
			}

			sashWindowsContainer.addPageLifeCycleListener(new IPageLifeCycleEventsListener() {
				@Override
				public void pageOpened(IPage page) {
					if (page instanceof IEditorPage) {
						IEditorPage editorPage = (IEditorPage) page;
						addInvalidRootAgent(coreEditor, editorPage.getIEditorPart(), view);
					}
				}

				@Override
				public void pageClosed(IPage page) {
					if (page instanceof IEditorPage) {
						IEditorPage editorPage = (IEditorPage) page;
						removeInvalidRootAgent(coreEditor, editorPage.getIEditorPart());
					}
				}

				@Override
				public void pageChanged(IPage page) {
					// Do nothing.
				}

				@Override
				public void pageDeactivated(IPage page) {
					// Do nothing.
				}

				@Override
				public void pageActivated(IPage page) {
					// Do nothing.
				}

				@Override
				public void pageAboutToBeOpened(IPage page) {
					// Do nothing.
				}

				@Override
				public void pageAboutToBeClosed(IPage page) {
					// Do nothing.
				}
			});
		}

		view.addListener(new CDOViewListener(editor));
		try {
			editorListeners.get(editor.getSite().getPage(), new Callable<EditorListener>() {

				@Override
				public EditorListener call() throws Exception {
					// Probably not necessary. But the previous API is deprecated. It is probably safer
					// to return a non-null EditorListener, but we're not supposed to enter this method anyway
					EditorListener listener = new EditorListener();
					editor.getSite().getPage().addPartListener(listener);
					return listener;
				}
			}).addEditor(editor);
		} catch (Exception e) {
			// this should be impossible with our cache loader
			Activator.log.error(e);
		}
	}

	private void addInvalidRootAgent(IEditorPart coreEditor, IEditorPart editorPart, final CDOView view) {
		final GraphicalViewer graphicalViewer = getGraphicalViewer(editorPart);
		if (graphicalViewer != null) {
			CDOInvalidRootAgent invalidRootAgent = new CDOInvalidRootAgent(view) {
				private Map<Object, Resource> temporaryResources = new HashMap<Object, Resource>();
				private int lastID;

				@Override
				protected Object createEmptyRoot(CDOObject invalidRoot) {
					Diagram diagram = NotationFactory.eINSTANCE.createDiagram();
					diagram.setName("Unavailable");

					Resource resource = new ResourceImpl(URI.createURI("unavailable://" + (++lastID)));
					resource.getContents().add(diagram);

					view.getResourceSet().getResources().add(resource);
					temporaryResources.put(diagram, resource);
					return diagram;
				}

				@Override
				protected void setRootToUI(Object root) {
					EditPart oldRoot = graphicalViewer.getContents();
					Resource resource = temporaryResources.remove(oldRoot);
					if (resource != null) {
						view.getResourceSet().getResources().remove(resource);
					}

					graphicalViewer.setContents(root);
				}

				@Override
				protected Object getRootFromUI() {
					return graphicalViewer.getContents().getModel();
				}
			};

			Map<IEditorPart, CDOInvalidRootAgent> map = invalidRootAgents.get(coreEditor);
			if (map == null) {
				map = new HashMap<IEditorPart, CDOInvalidRootAgent>();
				invalidRootAgents.put(coreEditor, map);
			}

			map.put(editorPart, invalidRootAgent);
		}
	}

	private void removeInvalidRootAgent(IEditorPart coreEditor, IEditorPart editorPart) {
		Map<IEditorPart, CDOInvalidRootAgent> map = invalidRootAgents.get(coreEditor);
		if (map != null) {
			CDOInvalidRootAgent invalidRootAgent = map.remove(editorPart);
			if (invalidRootAgent != null) {
				invalidRootAgent.dispose();
			}
		}
	}

	private GraphicalViewer getGraphicalViewer(IEditorPart editorPart) {
		if (editorPart instanceof GraphicalEditor) {
			GraphicalEditor graphicalEditor = (GraphicalEditor) editorPart;

			Method method = ReflectUtil.getMethod(GraphicalEditor.class, "getGraphicalViewer");
			return (GraphicalViewer) ReflectUtil.invokeMethod(method, graphicalEditor);
		}

		return null;
	}

	private void closed(IEditorPart coreEditor) {
		CDOView view = editors.remove(coreEditor);
		DawnEditorAdapter.removeAdapter(coreEditor);

		Map<IEditorPart, CDOInvalidRootAgent> map = invalidRootAgents.remove(coreEditor);
		if (map != null) {
			for (CDOInvalidRootAgent invalidRootAgent : map.values()) {
				invalidRootAgent.dispose();
			}
		}
	}

	//
	// Nested types
	//

	private class CDOViewListener extends LifecycleEventAdapter {

		private final IEditorPart editor;

		CDOViewListener(IEditorPart editor) {
			this.editor = editor;
		}

		@Override
		protected void onDeactivated(ILifecycle lifecycle) {
			UIUtil.later(new Runnable() {

				@Override
				public void run() {
					if (editors.containsKey(editor)) {
						editor.getSite().getPage().closeEditor(editor, false);
					}
				}
			});

			lifecycle.removeListener(this);
		}
	}

	private class EditorListener implements IPartListener, IPageLifeCycleEventsListener {

		private final Set<IEditorPart> editors = Sets.newHashSet();

		void addEditor(IEditorPart editor) {
			editors.add(editor);

			Optional<ISashWindowsContainer> sashContainer = AdapterUtils.adapt(editor, ISashWindowsContainer.class);
			if (sashContainer.isPresent()) {
				sashContainer.get().addPageLifeCycleListener(this);
			}
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (editors.remove(part)) {
				IEditorPart editor = (IEditorPart) part;
				closed(editor);
			}

			Optional<ISashWindowsContainer> sashContainer = AdapterUtils.adapt(part, ISashWindowsContainer.class);
			if (sashContainer.isPresent()) {
				sashContainer.get().removePageLifeCycleListener(this);
				sashContainer.get().visit(new IPageVisitor() {

					@Override
					public void accept(IEditorPage page) {
						pageClosed(page);
					}

					@Override
					public void accept(IComponentPage page) {
						// pass
					}
				});
			}
		}

		@Override
		public void pageClosed(IPage page) {
			if (page instanceof IEditorPage) {
				closed(((IEditorPage) page).getIEditorPart());
			}
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			// pass
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			// pass
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			// pass
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			// pass
		}

		@Override
		public void pageChanged(IPage newPage) {
			// pass
		}

		@Override
		public void pageOpened(IPage page) {
			// pass
		}

		@Override
		public void pageActivated(IPage page) {
			// pass
		}

		@Override
		public void pageDeactivated(IPage page) {
			// pass
		}

		@Override
		public void pageAboutToBeOpened(IPage page) {
			// pass
		}

		@Override
		public void pageAboutToBeClosed(IPage page) {
			// pass
		}
	}

	private class ResourceSetDisposalApprover implements IResourceSetDisposalApprover {

		@Override
		public DisposeAction disposalRequested(CDOCheckout checkout, Collection<ResourceSet> resourceSets) {

			DisposeAction result = DisposeAction.CLOSE;
			final List<IEditorPart> dirty = Lists.newArrayList();

			for (ResourceSet next : resourceSets) {
				CDOView view = CDOUtil.getView(next);
				IEditorPart editor = editors.inverse().get(view);

				if ((editor != null) && editor.isDirty()) {
					dirty.add(editor);
				}
			}

			if (!dirty.isEmpty()) {
				Future<Integer> dlgResult = UIUtil.call(new Callable<Integer>() {

					@Override
					public Integer call() {
						MessageDialog dlg = new MessageDialog(dirty.get(0).getSite().getShell(), Messages.PapyrusCDOEditorManager_1, null, Messages.PapyrusCDOEditorManager_2, MessageDialog.QUESTION_WITH_CANCEL, //
								new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);

						return dlg.open();
					}
				});

				try {
					switch (dlgResult.get()) {
					case 0: // Yes
						result = DisposeAction.SAVE;
						break;
					case 1: // No
						result = DisposeAction.CLOSE;
						break;
					case 2: // Cancel
						result = DisposeAction.NONE;
						break;
					default:
						break;
					}
				} catch (Exception e) {
					// shouldn't happen because the UIUtil doesn't support
					// interruption as it runs stuff on the UI thread
					Activator.log.error(e);
				}
			}

			return result;
		}
	}
}
