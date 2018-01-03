/*******************************************************************************
 * Copyright (c) 2018 Christian W. Damus and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Christian W. Damus - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.compare.uml2.internal.postprocessor;

import static org.eclipse.emf.compare.utils.MatchUtil.getMatchedObject;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusPostProcessor.IS_RESOURCE_REFACTORING_MOVE;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusPostProcessor.IS_STEREOTYPE_APPLICATION_RESOURCE_MOVE;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.getResource;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.index;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.opposite;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.MatchResource;
import org.eclipse.emf.compare.ResourceAttachmentChange;
import org.eclipse.emf.compare.merge.ResourceAttachmentChangeMerger;
import org.eclipse.emf.compare.merge.ResourceChangeAdapter;
import org.eclipse.emf.compare.uml2.internal.postprocessor.util.UMLCompareUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.compare.uml2.internal.UMLPapyrusComparePlugin;
import org.eclipse.uml2.uml.Element;

/**
 * A specialized merger for the {@link ResourceAttachmentChange}s in resources that are refactored (renamed or
 * moved) and which, therefore, are not actually logically moving those objects.
 *
 * @author Christian W. Damus
 */
public class ResourceRefactoringMerger extends ResourceAttachmentChangeMerger {

	/**
	 * Initializes me.
	 */
	public ResourceRefactoringMerger() {
		super();
	}

	@Override
	public boolean isMergerFor(Diff target) {
		return IS_STEREOTYPE_APPLICATION_RESOURCE_MOVE.apply(target)
				|| IS_RESOURCE_REFACTORING_MOVE.apply(target);
	}

	@Override
	protected Resource findOrCreateTargetResource(Match match, boolean rightToLeft) {
		DifferenceSource targetSide = rightToLeft ? DifferenceSource.LEFT : DifferenceSource.RIGHT;
		EObject movedObject = getMatchedObject(match, targetSide);

		if (movedObject instanceof Element) {
			// This is not the stereotype application case
			return super.findOrCreateTargetResource(match, rightToLeft);

		} else {
			// We were moved to another resource. If our base element was, also, then
			// we should be moved to its resource regardless

			Element baseElement = UMLCompareUtil.getBaseElement(movedObject);
			if (baseElement != null) {
				return baseElement.eResource();
			}
		}

		return super.findOrCreateTargetResource(match, rightToLeft);
	}

	@Override
	protected void move(ResourceAttachmentChange diff, boolean rightToLeft) {
		Match match = diff.getMatch();

		Resource left = match.getLeft().eResource();
		Resource right = match.getRight().eResource();

		super.move(diff, rightToLeft);

		if (IS_RESOURCE_REFACTORING_MOVE.apply(diff)) {
			// We've moved the UML element. Bring along all stereotype applications
			// and other related content and delete the originating resources
			PapyrusResourceIndex index = index(match);

			if (rightToLeft) {
				MatchResource leftMatch = index.getMatch(left);
				Resource newLeft = demandResource(leftMatch, DifferenceSource.LEFT);
				newLeft.getContents().addAll(left.getContents());
				markForDeletion(left);

				for (Map.Entry<String, MatchResource> next : index.getGroup(leftMatch, DifferenceSource.LEFT)
						.entrySet()) {
					if (next.getValue() != leftMatch) {
						left = next.getValue().getLeft();
						newLeft = demandResource(next.getValue(), DifferenceSource.LEFT);
						newLeft.getContents().addAll(left.getContents());
						markForDeletion(left);
					}
				}
			} else {
				MatchResource rightMatch = index.getMatch(right);
				Resource newRight = demandResource(rightMatch, DifferenceSource.RIGHT);
				newRight.getContents().addAll(right.getContents());
				markForDeletion(right);

				for (Map.Entry<String, MatchResource> next : index
						.getGroup(rightMatch, DifferenceSource.RIGHT).entrySet()) {
					if (next.getValue() != rightMatch) {
						right = next.getValue().getRight();
						newRight = demandResource(next.getValue(), DifferenceSource.RIGHT);
						newRight.getContents().addAll(right.getContents());
						markForDeletion(right);
					}
				}
			}
		}
	}

	/**
	 * Add a {@code resource} the collection of resources to be deleted when the merge result is saved.
	 *
	 * @param resource
	 *            a resource to be deleted on save of the merge
	 */
	protected void markForDeletion(Resource resource) {
		ResourceChangeAdapter rca = (ResourceChangeAdapter)EcoreUtil.getExistingAdapter(resource,
				ResourceChangeAdapter.class);
		if (rca != null) {
			try {
				Field resourcesToDelete = rca.getClass().getDeclaredField("resourcesToDelete"); //$NON-NLS-1$
				resourcesToDelete.setAccessible(true);

				@SuppressWarnings("unchecked")
				Collection<? super Resource> resourcesToDeleteValue = (Collection<? super Resource>)resourcesToDelete
						.get(rca);
				resourcesToDeleteValue.add(resource);
			} catch (Exception e) {
				UMLPapyrusComparePlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, UMLPapyrusComparePlugin.PLUGIN_ID,
								"Failed to mark resource for deletion: " + resource.getURI(), //$NON-NLS-1$
								e));
			}
		}
	}

	/**
	 * Obtains the new (refactored) resource on the given {@code side} of the comparison, based on the URI of
	 * the resource opposite to it in the {@code match}. The resulting resource is created if it does not yet
	 * exist in the resource set on this {@code side}.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            the side of the comparison in which to get the refactored resource
	 * @return the refactored resource
	 */
	protected Resource demandResource(MatchResource match, DifferenceSource side) {
		ResourceSet rset = getResource(match, side).getResourceSet();
		URI uri = getResource(match, opposite(side)).getURI();

		Resource result = rset.getResource(uri, false);
		if (result == null) {
			result = rset.createResource(uri);
		}

		return result;
	}
}
