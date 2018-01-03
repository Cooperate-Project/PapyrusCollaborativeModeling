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

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.ComparePackage;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.MatchResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * An index of relationships between Papyrus resources on each side of a comparison.
 *
 * @author Christian W. Damus
 */
public final class PapyrusResourceIndex {
	/**
	 * Mapping by side and Papyrusesque extensionless URI of the groups of resources that each together
	 * comprise a Papyrus model.
	 */
	private final Map<SidedURI, Map<String, MatchResource>> groups = Maps.newHashMap();

	/** Cache of matches by resource. */
	private final Map<Resource, MatchResource> matches = Maps.newHashMap();

	/** Token stored in the match cache for cache misses. */
	private final MatchResource noMatch = CompareFactory.eINSTANCE.createMatchResource();

	/** The comparison that I index. */
	private final Comparison comparison;

	/**
	 * Index the resource matches in the given {@code comparison}.
	 * 
	 * @param comparison
	 *            the comparison to index
	 */
	private PapyrusResourceIndex(Comparison comparison) {
		super();

		this.comparison = comparison;

		for (MatchResource next : comparison.getMatchedResources()) {
			if (next.getLeftURI() != null) {
				URI uri = URI.createURI(next.getLeftURI());
				SidedURI key = SidedURI.left(uri.trimFileExtension());
				getGroup(key).put(uri.fileExtension(), next);
			}
			if (next.getRightURI() != null) {
				URI uri = URI.createURI(next.getRightURI());
				SidedURI key = SidedURI.right(uri.trimFileExtension());
				getGroup(key).put(uri.fileExtension(), next);
			}
		}
	}

	/**
	 * Get all of the resource matches (including the given {@code match}) that are the resources together
	 * comprising the Papyrus model containing it.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            the side of the comparison on which the {@code match} is being considered
	 * @return a mapping of file extension to match for all of the resources of the Papyrus model of which the
	 *         {@code match} is one
	 */
	public Map<String, MatchResource> getGroup(MatchResource match, DifferenceSource side) {
		URI uri = URI.createURI(getURI(match, side));
		return getGroup(SidedURI.of(uri.trimFileExtension(), side));
	}

	/**
	 * Get the group for the given URI on its side, creating the group if necessary.
	 * 
	 * @param key
	 *            a URI (without extension) on a side of the comparison
	 * @return its resource match group
	 */
	private Map<String, MatchResource> getGroup(SidedURI key) {
		Map<String, MatchResource> result = groups.get(key);
		if (result == null) {
			result = Maps.newHashMap();
			groups.put(key, result);
		}
		return result;
	}

	/**
	 * Queries the match for a given {@code resource} in the context of my comparison.
	 * 
	 * @param resource
	 *            a resource in my comparison
	 * @return its match
	 */
	public MatchResource getMatch(Resource resource) {
		MatchResource result = matches.get(resource);

		if (result == null) {
			result = noMatch;

			if (resource != null) {
				for (MatchResource next : comparison.getMatchedResources()) {
					if (next.getLeft() == resource || next.getRight() == resource
							|| next.getOrigin() == resource) {

						result = next;
						break;
					}
				}
			} // else always cache a miss for the null resource

			// Cache the result, whether a miss ('noMatch') or not
			matches.put(resource, result);
		}

		return (result == noMatch) ? null : result;
	}

	/**
	 * Obtains the match that represents the refactoring of a {@code resource} of which the given matched
	 * object is a root. This resource match will match resources on either side that have different URIs,
	 * which is the refactoring.
	 * 
	 * @param objectMatch
	 *            a match of an object that is a UML resource root element, either the root of the entire
	 *            model or of some sub-unit
	 * @return the refactoring resource match that was inferred from the different resources in the given
	 *         object match, or {@code null} if there is no refactoring of the resources containing the object
	 */
	public MatchResource getResourceRefactoring(Match objectMatch) {
		EObject left = objectMatch.getLeft();
		EObject right = objectMatch.getRight();

		if (left == null || right == null) {
			// There is no resource match if the object doesn't exist on both sides
			return null;
		}

		MatchResource leftRes = getMatch(left.eResource());
		MatchResource rightRes = getMatch(right.eResource());

		if (leftRes == null || rightRes == null) {
			// We could not have detected a resource match without both sides
			return null;
		}

		if (Objects.equal(leftRes.getLeftURI(), rightRes.getRightURI())) {
			// Not refactored
			return null;
		}

		// It's not a rename/move/etc. resource refactoring if the resources
		// on the left and right were not matched together
		return leftRes == rightRes ? leftRes : null;
	}

	/**
	 * Obtains the unique (lazily computed) index of Papyrus resources in the context of a {@code comparison}.
	 * 
	 * @param comparison
	 *            a comparison
	 * @return its resource index
	 */
	public static PapyrusResourceIndex index(Comparison comparison) {
		Attachment attachment = (Attachment)EcoreUtil.getExistingAdapter(comparison, Attachment.class);
		if (attachment == null) {
			attachment = new PapyrusResourceIndex(comparison).new Attachment(comparison);
		}
		return attachment.getIndex();
	}

	/**
	 * Obtains the unique (lazily computed) index of Papyrus resources in the context of the comparison
	 * containing a {@code match}.
	 * 
	 * @param match
	 *            a match in some comparison
	 * @return its resource index
	 */
	public static PapyrusResourceIndex index(Match match) {
		return index(match.getComparison());
	}

	/**
	 * Compute the side opposite a given {@code side}.
	 * 
	 * @param side
	 *            a side of the comparison
	 * @return the other side
	 */
	protected static DifferenceSource opposite(DifferenceSource side) {
		return DifferenceSource.get(1 - side.ordinal());
	}

	/**
	 * Obtains the resource on the given {@code side} of a {@code match}.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            a side
	 * @return the resource on that {@code side} of the {@code match}
	 */
	protected static Resource getResource(MatchResource match, DifferenceSource side) {
		switch (side) {
			case LEFT:
				return match.getLeft();
			case RIGHT:
				return match.getRight();
			default:
				throw new IllegalArgumentException("side"); //$NON-NLS-1$
		}
	}

	/**
	 * Obtains the resource URI on the given {@code side} of a {@code match}.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            a side
	 * @return the URI on that {@code side} of the {@code match}
	 */
	protected static String getURI(MatchResource match, DifferenceSource side) {
		switch (side) {
			case LEFT:
				return match.getLeftURI();
			case RIGHT:
				return match.getRightURI();
			default:
				throw new IllegalArgumentException("side"); //$NON-NLS-1$
		}
	}

	/**
	 * Assigns the {@code resource} on the given {@code side} of a {@code match}.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            a side
	 * @param resource
	 *            the resource to assign on that {@code side} of the {@code match}
	 */
	protected static void setResource(MatchResource match, DifferenceSource side, Resource resource) {
		switch (side) {
			case LEFT:
				match.setLeft(resource);
				break;
			case RIGHT:
				match.setRight(resource);
				break;
			default:
				throw new IllegalArgumentException("side"); //$NON-NLS-1$
		}
	}

	/**
	 * Assigns the resource {@code URI} on the given {@code side} of a {@code match}.
	 * 
	 * @param match
	 *            a resource match
	 * @param side
	 *            a side
	 * @param URI
	 *            the URI to assign on that {@code side} of the {@code match}
	 */
	protected static void setURI(MatchResource match, DifferenceSource side, String uri) {
		switch (side) {
			case LEFT:
				match.setLeftURI(uri);
				break;
			case RIGHT:
				match.setRightURI(uri);
				break;
			default:
				throw new IllegalArgumentException("side"); //$NON-NLS-1$
		}
	}

	//
	// Nested types
	//

	/**
	 * An adapter that attaches the unique instance of the index to a {@link Comparison} for subsequent
	 * retrieval.
	 */
	private final class Attachment extends AdapterImpl {
		/**
		 * Initializes me with my target {@code comparison}.
		 * 
		 * @param comparison
		 *            the comparison to which I attach
		 */
		Attachment(Comparison comparison) {
			super();

			comparison.eAdapters().add(this);
		}

		@Override
		public boolean isAdapterForType(Object type) {
			return type == PapyrusResourceIndex.class || type == Attachment.class;
		}

		/**
		 * Obtains the index that I attach to the comparison.
		 * 
		 * @return my index
		 */
		PapyrusResourceIndex getIndex() {
			return PapyrusResourceIndex.this;
		}

		@Override
		public void notifyChanged(Notification msg) {
			if (msg.isTouch() || msg.getNotifier() != comparison) {
				return;
			}

			switch (msg.getFeatureID(Comparison.class)) {
				case ComparePackage.COMPARISON__MATCHED_RESOURCES:
					// Purge cache of resource matches
					matches.clear();
					break;
				default:
					// Pass
					break;
			}
		}
	}
}
