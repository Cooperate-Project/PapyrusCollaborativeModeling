/*******************************************************************************
 * Copyright (c) 2015, 2017 EclipseSource Muenchen GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Dirix - initial API and implementation
 *     Christian W. Damus - bug 527638
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.saveparameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.ide.ui.internal.logical.ComparisonScopeBuilder;
import org.eclipse.emf.compare.ide.ui.logical.SynchronizationModel;
import org.eclipse.emf.compare.ide.ui.tests.workspace.TestProject;
import org.eclipse.emf.compare.ide.utils.StorageTraversal;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.uml2.ide.tests.util.ProfileTestUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.papyrus.compare.diagram.ide.ui.internal.DiagramMigrationHook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * This class tests the SaveParameterHook integration in EMF Compare by using the EMF Compare comparison
 * process to save and compare test models.
 * 
 * @author Stefan Dirix <sdirix@eclipsesource.com>
 */
@SuppressWarnings({"restriction", "nls" })
public class SaveParameterHookIntegrationTest {

	/**
	 * The bundle which contains this test.
	 */
	private static final String TEST_BUNDLE = "org.eclipse.papyrus.compare.diagram.tests";

	/**
	 * The path within this bundle to reach the data for this test.
	 */
	private static final String BASE_PATH = "src/org/eclipse/papyrus/compare/diagram/tests/saveparameter/data/";

	/**
	 * The name of the temporary project created to test the saving behavior of EMF Compare.
	 */
	private static final String TEST_PROJECT_NAME = "SaveParameterTestProject";

	/**
	 * The temporary project containing the original and modified files.
	 */
	private TestProject project;

	/**
	 * Tests if EMF Compare introduces binary changes when saving model files after the comparing process.
	 * 
	 * @throws Exception
	 *             If the files can not be copied or compared.
	 */
	@Test
	public void testSaveParametersIntegrationA1() throws Exception {
		final String localContainer = "a1/";
		final List<String> fileNames = Arrays.asList(new String[] {"model.uml", "model.notation" });
		assertNoChangeViaSave(localContainer, fileNames);
	}

	/**
	 * Tests if EMF Compare introduces binary changes when saving model files after the comparing process.
	 * 
	 * @throws Exception
	 *             If the files can not be copied or compared.
	 */
	@Test
	public void testSaveParametersIntegrationA2() throws Exception {
		final String localContainer = "a2/";
		final List<String> fileNames = Arrays
				.asList(new String[] {"model.di", "model.uml", "model.notation" });
		assertNoChangeViaSave(localContainer, fileNames);
	}

	/**
	 * Copies the files specified in {@code fileNames} twice into the temporary project. One set of files is
	 * then re-saved after the default comparing process of EMF Compare and binary compared to the original
	 * set of files to determine if EMF Compare introduced any changes.
	 * 
	 * @param localContainer
	 *            The container containing the files specified in {@code fileNames}. The container is relative
	 *            to {@link #BASE_PATH}.
	 * @param fileNames
	 *            The files to copy, save and compare which are located in the {@code localContainer}.
	 * @throws Exception
	 *             * @throws Exception If the files can not be copied or compared.
	 */
	private void assertNoChangeViaSave(final String localContainer, final Collection<String> fileNames)
			throws Exception {
		final Bundle bundle = Platform.getBundle(TEST_BUNDLE);

		final List<IFile> originalFiles = new ArrayList<IFile>(fileNames.size());
		final List<IFile> saveFiles = new ArrayList<IFile>(fileNames.size());
		final List<URI> originalResourceURIs = new ArrayList<>(fileNames.size());
		final List<String> saveResourceURIStrings = new ArrayList<>(fileNames.size());
		final Map<IFile, IFile> originalToSaveMap = new HashMap<>();

		// Create originals
		for (String fileName : fileNames) {
			final String path = BASE_PATH + localContainer + fileName;
			final URI fileURI = getFileUri(bundle.getEntry(path));
			final IFile originalFile = project.createFile("original/" + fileName, getInputStream(fileURI));
			final IFile saveFile = project.getProject().getFile(new Path("save/" + fileName));
			originalToSaveMap.put(originalFile, saveFile);

			originalFiles.add(originalFile);
			originalResourceURIs
					.add(URI.createPlatformResourceURI(originalFile.getFullPath().toString(), true));
		}

		// Run diagram migrations as needed, to avoid those introducing changes
		migrateDiagrams(originalResourceURIs);

		// Copy the migrated files. Be careful to maintain ordering
		project.createFolder("save");
		for (IFile originalFile : originalFiles) {
			IFile saveFile = originalToSaveMap.get(originalFile);
			saveFile.create(originalFile.getContents(true), true, null);
			saveFiles.add(saveFile);
			saveResourceURIStrings
					.add(URI.createPlatformResourceURI(saveFile.getFullPath().toString(), true).toString());
		}

		// build comparison scope which uses the internal resource sets of EMF Compare
		final StorageTraversal traversal = ProfileTestUtil
				.createStorageTraversal(saveResourceURIStrings.toArray(new String[0]));
		final SynchronizationModel syncModel = new SynchronizationModel(traversal, traversal, null);
		final IComparisonScope scope = ComparisonScopeBuilder.create(syncModel, new NullProgressMonitor());

		// save all resources touched by EMF Compare
		final ResourceSet resourceSet = (ResourceSet)scope.getLeft();
		for (Resource resource : resourceSet.getResources()) {
			resource.save(null);
		}

		// check if all files are still identical
		for (int i = 0; i < fileNames.size(); i++) {
			final IFile originalFile = originalFiles.get(i);
			final IFile saveFile = saveFiles.get(i);
			compareTextContent(originalFile, saveFile, true);
		}
	}

	/**
	 * Compares the textual content of both files. The content is only compared after removing all "carriage
	 * return" characters to allow the test to work properly independent of the given environment.
	 * 
	 * @param fileA
	 *            One of the files for the comparison.
	 * @param fileB
	 *            One of the files for the comparison.
	 * @return {@code true} if both files have the same textual content, {@code false} otherwise.
	 * @throws IOException
	 *             When there is an error reading one of the files.
	 */
	private void compareTextContent(IFile fileA, IFile fileB, boolean expectedEqual) throws IOException {
		final String textFileA = removeCRCharacters(getText(fileA));
		final String textFileB = removeCRCharacters(getText(fileB));

		if (expectedEqual) {
			assertEquals(textFileB, textFileA);
		} else {
			assertNotEquals(textFileB, textFileA);
		}
	}

	/**
	 * Returns a {@link String} with the same content but all "carriage return" characters removed.
	 * 
	 * @param text
	 *            The {@link String} from which all CR characters shall be removed.
	 * @return The given {@code text} without CR characters.
	 */
	private String removeCRCharacters(String text) {
		return text.replaceAll("\\r", "");
	}

	/**
	 * Reads and returns the content of the given {@link IFile} assuming it is a text file.
	 * 
	 * @param iFile
	 *            The text file which shall be read.
	 * @return The content of the given {@link IFile}.
	 * @throws IOException
	 *             When there is an error reading the file.
	 */
	private String getText(IFile iFile) throws IOException {
		final File file = iFile.getLocation().toFile();
		return Files.toString(file, Charsets.UTF_8);
	}

	/**
	 * Returns the {@link InputStream} for a given {@link URI}.
	 * 
	 * @param uri
	 *            The {@link URI} for which the {@link InputStream} is to be determined.
	 * @return The {@link InputStream} for the given {@link URI}.
	 * @throws IOException
	 *             If there is a problem obtaining an open input stream.
	 */
	private InputStream getInputStream(final URI uri) throws IOException {
		final URIConverter converter = new ExtensibleURIConverterImpl();
		return converter.createInputStream(uri);
	}

	/**
	 * Returns the {@link URI} for a given {@link URL}.
	 * 
	 * @param fileUrl
	 *            The {@link URL} for which the {@link URI} is to be determined.
	 * @return The {@link URI} for the given {@link URL}.
	 * @throws IOException
	 *             If an error occurs during the conversion.
	 */
	private URI getFileUri(final URL fileUrl) throws IOException {
		final URL fileLocation = FileLocator.toFileURL(fileUrl);
		return URI.createFileURI(fileLocation.getPath());
	}

	/**
	 * Initializes the temporary test project.
	 * 
	 * @throws CoreException
	 *             If an error occurs when (re-)creating the temporary test project.
	 */
	@Before
	public void setupProject() throws CoreException {
		project = new TestProject(TEST_PROJECT_NAME);
	}

	/**
	 * Disposes the temporary test project.
	 * 
	 * @throws CoreException
	 *             If an error occurs when disposing the temporary test project.
	 * @throws IOException
	 *             If an error occurs when disposing the temporary test project.
	 */
	@After
	public void disposeProject() throws CoreException, IOException {
		project.dispose();
	}

	/**
	 * Migrate the diagrams in a bunch of resources and save them.
	 * 
	 * @param resourceURIs
	 *            the resources in which to migrate diagrams
	 */
	void migrateDiagrams(Collection<URI> resourceURIs) {
		ResourceSet rset = new ResourceSetImpl();

		DiagramMigrationHook hook = new DiagramMigrationHook();
		for (URI next : resourceURIs) {
			rset.getResource(next, true);
		}

		hook.postLoadingHook(rset, resourceURIs);

		// Save the migrations. Note that the original test resources have the
		// redundant XMI types, so preserve those
		Map<Object, Object> saveOptions = new HashMap<>();
		saveOptions.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Boolean.TRUE);
		for (Resource next : rset.getResources()) {
			try {
				next.save(saveOptions);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Failed to save migrated diagram(s): " + e.getMessage());
			}
		}

		for (Resource next : rset.getResources()) {
			next.unload();
		}
		rset.getResources().clear();
	}
}
