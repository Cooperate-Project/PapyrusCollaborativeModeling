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

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.filter;
import static org.eclipse.emf.compare.utils.EMFComparePredicates.ofKind;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.getResource;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.index;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.opposite;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.setResource;
import static org.eclipse.papyrus.compare.uml2.internal.postprocessor.PapyrusResourceIndex.setURI;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.MatchResource;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.ResourceAttachmentChange;
import org.eclipse.emf.compare.postprocessor.IPostProcessor;
import org.eclipse.emf.compare.uml2.internal.postprocessor.util.UMLCompareUtil;
import org.eclipse.emf.compare.utils.MatchUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;

/**
 * Post-processor for comparisons of Papyrus models.
 *
 * @author Christian W. Damus
 */
public class PapyrusPostProcessor implements IPostProcessor {

	/**
	 * A predicate matching diffs that are {@link ResourceAttachmentChange}s that move a stereotype
	 * application.
	 */
	static final Predicate<Diff> IS_STEREOTYPE_APPLICATION_RESOURCE_MOVE = and(isStereotypeApplicationRAC(),
			ofKind(DifferenceKind.MOVE));

	/**
	 * A predicate matching diffs that are {@link ResourceAttachmentChange}s that move the principal UML
	 * element of a resource.
	 */
	static final Predicate<Diff> IS_RESOURCE_REFACTORING_MOVE = and(isResourceRefactoringRAC(),
			ofKind(DifferenceKind.MOVE));

	/**
	 * Initializes me.
	 */
	public PapyrusPostProcessor() {
		super();
	}

	public void postMatch(Comparison comparison, Monitor monitor) {
		// Handle refactoring of model resources, esp. sub-units
		rematchRefactoredUMLResources(comparison, monitor);
	}

	public void postDiff(Comparison comparison, Monitor monitor) {
		// Pass
	}

	public void postRequirements(Comparison comparison, Monitor monitor) {
		// Ensure that certain diffs of stereotype applications are merged
		// only after certain other diffs of their base UML elements
		findStereotypeApplicationDependencies(comparison);
	}

	public void postEquivalences(Comparison comparison, Monitor monitor) {
		// Pass
	}

	public void postConflicts(Comparison comparison, Monitor monitor) {
		// Pass
	}

	public void postComparison(Comparison comparison, Monitor monitor) {
		// Pass
	}

	/**
	 * Find diffs for stereotype applications moved from one resource to another and add the moves (if any) of
	 * their base elements as pre-requisites.
	 * 
	 * @param comparison
	 *            the contextual comparison
	 */
	protected void findStereotypeApplicationDependencies(Comparison comparison) {
		for (ResourceAttachmentChange rac : getStereotypeApplicationResourceMoves(comparison)) {
			// Also the diff that moves the base element, if any
			Match match = rac.getMatch();
			EObject stereotypeApplication = MatchUtil.getMatchedObject(match, rac.getSource());

			Element baseElement = UMLCompareUtil.getBaseElement(stereotypeApplication);
			if (baseElement != null) {
				Match baseMatch = comparison.getMatch(baseElement);
				Diff moveDiff = findMoveDiff(baseMatch, comparison, rac.getSource());
				if (moveDiff != null) {
					rac.getRequires().add(moveDiff);
				}
			}
		}
	}

	/**
	 * Obtain a predicate matching diffs that are {@link ResourceAttachmentChange}s pertaining to a stereotype
	 * application.
	 * 
	 * @return the is-stereotype-application-RAC predicate
	 */
	protected static Predicate<Diff> isStereotypeApplicationRAC() {
		return new Predicate<Diff>() {
			public boolean apply(Diff input) {
				if (!(input instanceof ResourceAttachmentChange)) {
					return false;
				}

				Match match = input.getMatch();
				EObject object = match.getLeft();
				if (object == null) {
					object = match.getRight();
				}

				return object != null && !(object instanceof Element)
						&& UMLCompareUtil.getBaseElement(object) != null;
			}
		};
	}

	/**
	 * Selects the resource attachment changes that indicate moves of stereotype applications from a resource
	 * to another.
	 * 
	 * @param comparison
	 *            the comparison from which to select the diffs
	 * @return the stereotype application moves between resources
	 */
	protected Iterable<ResourceAttachmentChange> getStereotypeApplicationResourceMoves(
			Comparison comparison) {

		return filter(filter(comparison.getDifferences(), ResourceAttachmentChange.class),
				IS_STEREOTYPE_APPLICATION_RESOURCE_MOVE);
	}

	/**
	 * Obtains a predicate matching {@link ResourceAttachmentChange}s of UML elements that signify the
	 * refactoring (rename/move) of a resource.
	 * 
	 * @return the is-resource-refactoring-RAC predicate
	 */
	protected static Predicate<Diff> isResourceRefactoringRAC() {
		return new Predicate<Diff>() {
			public boolean apply(Diff input) {
				if (!(input instanceof ResourceAttachmentChange)) {
					return false;
				}

				Match match = input.getMatch();
				EObject object = match.getLeft();
				if (object == null) {
					object = match.getRight();
				}

				return object instanceof Element && index(match).getResourceRefactoring(match) != null;
			}
		};
	}

	/**
	 * Find the nearest diff to a given {@code match} that moves it (perhaps indirectly via the content tree)
	 * from a resource to another resource.
	 * 
	 * @param match
	 *            a match for which to look for a move diff
	 * @param comparison
	 *            the contextual comparison
	 * @param side
	 *            the side of the comparison on which to look for move diffs
	 * @return a diff that moves that {@code match}ed object from one resource to another, or {@code null} if
	 *         it is not moved between resources
	 */
	protected Diff findMoveDiff(Match match, Comparison comparison, DifferenceSource side) {
		Diff result = null;

		if (match != null) {
			EObject object = MatchUtil.getMatchedObject(match, side);
			if (object != null) {
				for (Diff next : comparison.getDifferences(object)) {
					if (next instanceof ReferenceChange
							&& ((ReferenceChange)next).getReference().isContainment()) {
						result = next;
						break;
					}
				}
			}

			if (result == null) {
				// Maybe the object is moved to a resource root?
				Iterator<ResourceAttachmentChange> iter = Iterators.filter(match.getDifferences().iterator(),
						ResourceAttachmentChange.class);
				if (iter.hasNext()) {
					result = iter.next();
				}
			}

			if (result == null && match.eContainer() instanceof Match) {
				// Look up the tree
				result = findMoveDiff((Match)match.eContainer(), comparison, side);
			}
		}

		return result;
	}

	/**
	 * Analyze the matching of resources to re-match any that have different URIs but that by dint of their
	 * primary UML element having moved are actually a refactoring (rename or move) of a single logical UML
	 * resource.
	 * 
	 * @param comparison
	 *            the contextual comparison
	 * @param monitor
	 *            progress monitor
	 */
	protected void rematchRefactoredUMLResources(Comparison comparison, Monitor monitor) {
		// Look for renamed resources, which are matched by their root UML elements
		Map<Resource, MatchResource> updates = Maps.newHashMap();
		PapyrusResourceIndex resourceGroups = index(comparison);

		for (MatchResource mres : comparison.getMatchedResources()) {
			if (!rematch(comparison, mres, DifferenceSource.LEFT, resourceGroups, updates)) {
				// Try the other way around
				rematch(comparison, mres, DifferenceSource.RIGHT, resourceGroups, updates);
			}
		}

		if (!updates.isEmpty()) {
			// Remove incomplete matches
			for (Iterator<MatchResource> iter = Lists.reverse(comparison.getMatchedResources())
					.iterator(); iter.hasNext() && !updates.isEmpty();) {

				MatchResource next = iter.next();

				MatchResource update = updates.remove(next.getLeft());
				if (update == null || update == next) {
					update = updates.remove(next.getRight());
					if (update == next) {
						update = null;
					}
				}

				if (update != null) {
					// MatchResource::locationChanges is no longer used,
					// so don't worry about updating that

					// Capture the origin from the match we're deleting
					if (update.getOrigin() == null) {
						update.setOrigin(next.getOrigin());
						update.setOriginURI(next.getOriginURI());
					}

					// Remove the redundant match
					iter.remove();
				}
			}
		}
	}

	/**
	 * Attempt to re-match a UML resource that appears to be unmatched with a UML resource on the other side,
	 * also unmatched, that contains the same root {@link Element}, on the assumption that such element
	 * constitutes logical content of the resource.
	 * 
	 * @param comparison
	 *            the contextual comparison
	 * @param mres
	 *            a resource to re-match
	 * @param side
	 *            the side from which to try to find a new match on the other side
	 * @param index
	 *            an index of relationships between resources in the context of the the {@code comparison}
	 * @param updates
	 *            collects the incomplete matches of resources that were merged into incomplete matches on the
	 *            other side, which need to be removed from the {@code comparison} in a subsequent step
	 *            (because it isn't safe to do so in-line)
	 * @return {@code true} if a new match was found for the resource from this {@code side}; {@code false},
	 *         otherwise
	 */
	protected boolean rematch(Comparison comparison, MatchResource mres, DifferenceSource side,
			PapyrusResourceIndex index, Map<Resource, MatchResource> updates) {

		final DifferenceSource opposite = opposite(side);
		final Resource oneSide = getResource(mres, side);
		final Resource otherSide = getResource(mres, opposite);

		if (oneSide != null && otherSide == null) {
			// Don't process a resource match that we've already melded into another
			if (!updates.containsKey(oneSide) && !oneSide.getContents().isEmpty()) {
				EObject root = oneSide.getContents().get(0);
				if (!(root instanceof Element)) {
					// Only need this for UML resources that reliably contain
					// only one real element, to handle stereotype applications
					// that are additional non-UML roots
					return false;
				}

				Match rootMatch = comparison.getMatch(root);
				if (rootMatch != null) {
					EObject otherRoot = MatchUtil.getMatchedObject(rootMatch, opposite);
					if (otherRoot == null) {
						return false;
					}

					Resource other = ((InternalEObject)otherRoot).eDirectResource();
					if (other != null) {
						combine(mres, other, opposite, index, updates);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Combine a resource match with an{@code other} resource.
	 * 
	 * @param uml
	 *            an unmatched UML resource to complete with the {@code other}
	 * @param other
	 *            a resource with which to complete the match
	 * @param otherSide
	 *            the side on which the {@code other} resource is
	 * @param index
	 *            an index of relationships between resources in the context of the the {@code comparison}
	 * @param updates
	 *            collects the incomplete matches of resources that were merged into incomplete matches on the
	 *            other side, which need to be removed from the {@code comparison} in a subsequent step
	 *            (because it isn't safe to do so in-line)
	 */
	protected void combine(MatchResource uml, Resource other, DifferenceSource otherSide,
			PapyrusResourceIndex index, Map<Resource, MatchResource> updates) {

		// Meld the matches for the UML resource
		setResource(uml, otherSide, other);
		setURI(uml, otherSide, other.getURI().toString());
		updates.put(other, uml);

		// And do the same for other Papyrus resources in the same group
		DifferenceSource thisSide = opposite(otherSide);
		Map<String, MatchResource> thisGroup = index.getGroup(uml, thisSide);
		Map<String, MatchResource> otherGroup = index.getGroup(uml, otherSide);

		for (Map.Entry<String, MatchResource> next : thisGroup.entrySet()) {
			MatchResource combine = next.getValue();

			if (combine != uml && getResource(combine, otherSide) == null) {
				MatchResource combineWith = otherGroup.get(next.getKey());
				if (combineWith != null && getResource(combineWith, thisSide) == null) {
					Resource combineWithResource = getResource(combineWith, otherSide);
					setResource(combine, otherSide, combineWithResource);
					setURI(combine, otherSide, combineWithResource.getURI().toString());
					updates.put(combineWithResource, combine);
				}
			}
		}
	}
}
