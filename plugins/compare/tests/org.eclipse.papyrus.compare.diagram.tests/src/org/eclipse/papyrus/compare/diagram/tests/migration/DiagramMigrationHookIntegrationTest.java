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

import static org.eclipse.papyrus.compare.diagram.tests.migration.DiagramMigrationHookTest.collectAll;
import static org.eclipse.papyrus.compare.diagram.tests.migration.DiagramMigrationHookTest.getTestInput;
import static org.eclipse.papyrus.compare.diagram.tests.migration.DiagramMigrationHookTest.hasVisualID;
import static org.eclipse.papyrus.compare.diagram.tests.migration.DiagramMigrationHookTest.isCurrentVersion;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Conflict;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.ide.ui.internal.logical.ComparisonScopeBuilder;
import org.eclipse.emf.compare.ide.ui.logical.SynchronizationModel;
import org.eclipse.emf.compare.ide.ui.tests.workspace.TestProject;
import org.eclipse.emf.compare.ide.utils.StorageTraversal;
import org.eclipse.emf.compare.rcp.internal.extension.impl.EMFCompareBuilderConfigurator;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.uml2.ide.tests.util.ProfileTestUtil;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.compare.diagram.ide.ui.internal.DiagramMigrationHook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for the {@link DiagramMigrationHook} class.
 * 
 * @author Christian W. Damus
 */
@SuppressWarnings({"restriction", "nls" })
public class DiagramMigrationHookIntegrationTest {

	static final List<String> FILE_NAMES = ImmutableList.of("model.di", "model.uml", "model.notation");

	private TestProject project;

	private Map<String, IFile> files;

	/**
	 * Initializes me.
	 */
	public DiagramMigrationHookIntegrationTest() {
		super();
	}

	@Test
	public void hook() {
		IComparisonScope scope = getComparisonScope(false);

		// Views no longer have numeric "visual ID" types
		Iterable<View> views = collectAll((ResourceSet)scope.getLeft(), View.class);
		assumeThat(views, hasItem(any(View.class)));
		assertThat(views, everyItem(not(hasVisualID())));

		// Diagrams are up-to-date
		Iterable<Diagram> diagrams = Iterables.filter(views, Diagram.class);
		assumeThat(diagrams, hasItem(any(Diagram.class)));
		assertThat(diagrams, everyItem(isCurrentVersion()));
	}

	@Test
	public void noConflicts() {
		IComparisonScope scope = getComparisonScope(true);
		Comparison comparison = compare(scope);

		assertThat("Migration introduced different objects on each side that are in conflict",
				comparison.getConflicts(), not(hasItem(any(Conflict.class))));
	}

	//
	// Test framework
	//

	@Before
	public void createProject() throws Exception {
		files = new LinkedHashMap<>();
		project = new TestProject();

		for (String next : FILE_NAMES) {
			files.put(next, project.createFile(next, getTestInput(next)));
		}
	}

	@After
	public void deleteProject() throws CoreException, IOException {
		files = null;
		project.dispose();
	}

	IComparisonScope getComparisonScope(boolean threeWay) {
		List<String> uris = new ArrayList<>(3);
		for (IFile next : files.values()) {
			uris.add(URI.createPlatformResourceURI(next.getFullPath().toString(), true).toString());
		}

		// Let the comparison scope machinery load the resources with all available hooks
		final StorageTraversal traversal = ProfileTestUtil
				.createStorageTraversal(uris.toArray(new String[uris.size()]));

		// Use the same resources for all sides (if we compare, it's to find spurious conflicts)
		StorageTraversal origin = threeWay ? traversal : null;
		final SynchronizationModel syncModel = new SynchronizationModel(traversal, traversal, origin);

		return ComparisonScopeBuilder.create(syncModel, new NullProgressMonitor());
	}

	Comparison compare(IComparisonScope scope) {
		EMFCompare.Builder builder = EMFCompare.builder();
		EMFCompareBuilderConfigurator.createDefault().configure(builder);
		return builder.build().compare(scope);
	}
}
