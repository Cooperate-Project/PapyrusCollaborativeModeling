/*****************************************************************************
 * Copyright (c) 2014, 2017-2018 CEA LIST.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) patrick.tessier@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.revision.tool.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.revision.tool.core.I_ReviewStereotype;
import org.eclipse.papyrus.revision.tool.core.ReviewResourceManager;
import org.eclipse.papyrus.revision.tool.ui.ReviewsEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Stereotype;

/**
 *This handler is used to accept a ToDo revision.
 */
public class AcceptReviewHandler extends RevisionAbstractHandler {
	/**
	 * The constructor.
	 */
	public AcceptReviewHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ArrayList<Element> elements=getSelectionSet();
		final IWorkbenchPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if( part instanceof ReviewsEditor){
			if( elements.size()!=0){

				RecordingCommand cmd= new RecordingCommand(((ReviewsEditor)part).getReviewResourceManager().getDomain(), "Accept currentReview") {
					@Override
					protected void doExecute() {

						for (Element element : elements) {
							ArrayList<Comment> subCommentsToReview= new ArrayList<Comment>();
							//collect all subcomments
							for (Iterator<Comment> commentInerator = element.getOwnedComments().iterator(); commentInerator.hasNext();) {
								Comment subComment = (Comment) commentInerator.next();
								subCommentsToReview.add(subComment);
							}
							//manage subComments
							for (Iterator<Comment> commentInerator = subCommentsToReview.iterator(); commentInerator.hasNext();) {
								Element subComment = (Comment) commentInerator.next();
								manageTodoReview(part, subComment);

							}

							manageTodoReview(part, element);
						}
					}

					private void manageTodoReview(final IWorkbenchPart part, Element element) {
						Stereotype reviewtoDo= element.getAppliedStereotype(I_ReviewStereotype.TODO_STEREOTYPE);
						if(reviewtoDo!=null ){
							String diffuriFragment=(String)element.getValue(reviewtoDo, I_ReviewStereotype.COMMENT_DIFFREF_ATT);
							if( diffuriFragment!=null){

								//load EObject
								EObject eOject=element.eResource().getEObject(diffuriFragment);
								if(eOject instanceof ReferenceChange  ){
									ReferenceChange referenceChange= (ReferenceChange)eOject;
									//accept Remove
									if( referenceChange.getKind().equals(DifferenceKind.DELETE)){
										acceptDelete(part, element, referenceChange);
									}
									//accept ADD
									else if( referenceChange.getKind().equals(DifferenceKind.ADD)){
										removeDiffAndReview(part, element, referenceChange);

									}
								}
								//accept Set
								else if(eOject instanceof Match ){
									ReviewResourceManager r=((ReviewsEditor)part).getReviewResourceManager();
									removeMatchAndReview(eOject, r, element);

								}
							}
							else {
								((Element)element.eContainer()).getOwnedComments().remove(element);
							}
						}

					}
				};
				((ReviewsEditor)part).getReviewResourceManager().getDomain().getCommandStack().execute(cmd);
			}
		}
		return null;
	}
	protected void removeDiffAndReview(final IWorkbenchPart part, Element element,
			ReferenceChange referenceChange) {
		ReviewResourceManager r=((ReviewsEditor)part).getReviewResourceManager();
		Match modelMatch=r.getDiffModel().getMatch(r.getWorkingModel());
		modelMatch.getDifferences().remove(referenceChange);
		((Element)element.eContainer()).getOwnedComments().remove(element);
	}

	protected void acceptDelete(final IWorkbenchPart part, Element element, ReferenceChange referenceChange) {
		ReviewResourceManager r=((ReviewsEditor)part).getReviewResourceManager();
		EObject eObjectToRemove=referenceChange.getValue();
		removeDiffAndReview(part, element, referenceChange);
		DestroyElementRequest destroyrequest= new DestroyElementRequest(false); 
		destroyrequest.setEditingDomain(r.getDomain());
		destroyrequest.setElementToDestroy(eObjectToRemove);
		if( eObjectToRemove!=null) {
			IElementEditService  provider = ElementEditServiceUtils.getCommandProvider(eObjectToRemove);
			if(provider != null) {
				// Retrieve delete command from the Element Edit service
				ICommand deleteCommand = provider.getEditCommand(destroyrequest);
				if(deleteCommand != null) {
					try {
						deleteCommand.execute(new NullProgressMonitor(), null);
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			}
		}
	}

	protected void removeMatchAndReview(EObject eOject, ReviewResourceManager r,Element element) {
		Match modelMatch=r.getDiffModel().getMatch(r.getWorkingModel());
		modelMatch.getSubmatches().remove(((Match)eOject));
		((Element)element.eContainer()).getOwnedComments().remove(element);
	}

}
