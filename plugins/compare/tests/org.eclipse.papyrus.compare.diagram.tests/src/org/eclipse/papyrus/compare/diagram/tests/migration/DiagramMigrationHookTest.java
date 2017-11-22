/*******************************************************************************
 * Copyright (c) 2017 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Christian W. Damus - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.migration;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.compare.diagram.ide.ui.internal.DiagramMigrationHook;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramVersioningUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for the {@link DiagramMigrationHook} class.
 */
@SuppressWarnings("nls")
public class DiagramMigrationHookTest {

	private final DiagramMigrationHook fixture = new DiagramMigrationHook();

	private final ResourceSet resourceSet = new ResourceSetImpl();

	/**
	 * Initializes me.
	 */
	public DiagramMigrationHookTest() {
		super();
	}

	@Test
	public void postLoadingHook() {
		Collection<URI> uris = uris("model.di", "model.uml", "model.notation");
		load(resourceSet, uris);
		fixture.postLoadingHook(resourceSet, uris);

		// Views no longer have numeric "visual ID" types
		Iterable<View> views = collectAll(resourceSet, View.class);
		assumeThat(views, hasItem(any(View.class)));
		assertThat(views, everyItem(not(hasVisualID())));

		// Diagrams are up-to-date
		Iterable<Diagram> diagrams = Iterables.filter(views, Diagram.class);
		assumeThat(diagrams, hasItem(any(Diagram.class)));
		assertThat(diagrams, everyItem(isCurrentVersion()));
	}

	//
	// Test framework
	//

	/**
	 * Ensure that the UML {@code CacheAdapter} doesn't hold on to our resources.
	 */
	@After
	public void unloadResourceSet() {
		for (Resource next : resourceSet.getResources()) {
			next.unload();
			next.eAdapters().clear();
		}

		resourceSet.getResources().clear();
		resourceSet.eAdapters().clear();
	}

	/**
	 * Create URIs from a bunch of strings.
	 * 
	 * @param uri
	 *            URI strings
	 * @return the corresponding URIs
	 */
	static List<URI> uris(String... uri) {
		List<URI> result = new ArrayList<>(uri.length);
		for (String next : uri) {
			result.add(URI.createURI(next));
		}
		return result;
	}

	/**
	 * Obtains the contents of the test resource that is intended to be persisted at the given target URI.
	 * 
	 * @param targetURI
	 *            the URI of the resource as it would be persisted. The last segment of this URI is taken as
	 *            the name to {@linkplain #getTestInput(String) get}
	 * @return the contents of the resource from the host bundle
	 * @throws IOException
	 *             on failure to find/access the test resource
	 * @see #getTestInput(String)
	 */
	static InputStream getTestInput(URI targetURI) throws IOException {
		return getTestInput(targetURI.lastSegment());
	}

	/**
	 * Obtains the contents of the {@code name}d test resource.
	 * 
	 * @param name
	 *            the test resource name to get
	 * @return the contents of the resource from the host bundle
	 * @throws IOException
	 *             on failure to find/access the test resource
	 */
	static InputStream getTestInput(String name) throws IOException {
		URL url = DiagramMigrationHookTest.class.getResource(String.format("data/a1/%s", name));
		return url.openStream();
	}

	/**
	 * Loads the given resource URIs in a resource set from the corresponding {@linkplain #getTestInput(URI)
	 * test resources} in the host bundle.
	 * 
	 * @param rset
	 *            the resource set context in which to load the resources
	 * @param uris
	 *            the resource URIs to load
	 * @return the loaded resources
	 */
	static Collection<Resource> load(ResourceSet rset, Collection<URI> uris) {
		List<Resource> result = new ArrayList<>(uris.size());

		for (URI next : uris) {
			Resource resource = rset.createResource(next);
			try (InputStream input = getTestInput(next)) {
				resource.load(input, null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Failed to load test resource: " + e.getMessage());
			}
			result.add(resource);
		}

		return result;
	}

	/**
	 * Collect all objects of some {@code type} in a resource set.
	 * 
	 * @param rset
	 *            a resource set
	 * @param type
	 *            the type of objects to retrieve
	 * @return the objects of the given {@code type}
	 */
	static <T> Iterable<T> collectAll(ResourceSet rset, Class<T> type) {
		return Lists.newArrayList(Iterators.filter(rset.getAllContents(), type));
	}

	/**
	 * Hamcrest matcher for diagram views that have "visual ID" types.
	 * 
	 * @return matches views that have numeric types
	 */
	static Matcher<View> hasVisualID() {
		return new FeatureMatcher<View, String>(matchesRegex("\\d+"), "is an integer", "type") {
			@Override
			protected String featureValueOf(View actual) {
				return actual.getType();
			}
		};
	}

	/**
	 * Hamcrest matcher for regular expressions.
	 * 
	 * @param regex
	 *            a regular expression
	 * @return a matcher for strings matching the {@code regex} in their entirety
	 */
	static Matcher<String> matchesRegex(String regex) {
		final java.util.regex.Matcher matcher = Pattern.compile(regex).matcher("");
		return new CustomTypeSafeMatcher<String>(String.format("matches regex '%s'", regex)) {
			@Override
			protected boolean matchesSafely(String item) {
				matcher.reset(item);
				return matcher.matches();
			}
		};
	}

	/**
	 * Hamcrest matcher for diagrams that are at the current version.
	 * 
	 * @return matches diagrams that are up-to-date
	 */
	static Matcher<Diagram> isCurrentVersion() {
		return new CustomTypeSafeMatcher<Diagram>("is up-to-date") {
			@Override
			protected boolean matchesSafely(Diagram item) {
				return DiagramVersioningUtils.isOfCurrentPapyrusVersion(item);
			}
		};
	}
}
