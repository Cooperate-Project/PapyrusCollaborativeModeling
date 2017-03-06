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
 *   Christian W. Damus (CEA) - bug 429242
 *   Christian W. Damus (CEA) - bug 430023
 *   Christian W. Damus (CEA) - bug 422257
 *   Eike Stepper (CEA) - bug 466520
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.core.tests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.emf.cdo.common.CDOCommonRepository;
import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.explorer.CDOExplorerUtil;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.explorer.repositories.CDORepository;
import org.eclipse.emf.cdo.internal.explorer.checkouts.CDOCheckoutImpl;
import org.eclipse.emf.cdo.internal.explorer.checkouts.OnlineCDOCheckout;
import org.eclipse.emf.cdo.internal.explorer.repositories.RemoteCDORepository;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.server.mem.MEMStoreUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.util.container.ContainerUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.io.IOUtil;
import org.eclipse.net4j.util.io.TMPUtil;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.transaction.TransactionException;
import org.eclipse.papyrus.cdo.internal.core.CDOUtils;
import org.eclipse.papyrus.junit.framework.classification.tests.AbstractPapyrusTest;
import org.eclipse.papyrus.junit.utils.rules.HouseKeeper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * This is the AbstractPapyrusCDOTest type. Enjoy.
 */
public abstract class AbstractPapyrusCDOTest extends AbstractPapyrusTest {

	private static final Pattern LEADING_SLASHES = Pattern.compile("^/+");

	@Rule
	public final HouseKeeper houseKeeper = new HouseKeeper();

	private IManagedContainer container;

	private CDORepository repository;

	private CDOCheckout checkout;

	public AbstractPapyrusCDOTest() {
		super();
	}

	@Before
	public void init() throws Exception {
		// Net4jUtil.prepareContainer(PluginContainer.INSTANCE);
		// JVMUtil.prepareContainer(PluginContainer.INSTANCE);

		File testFolder = TMPUtil.createTempFolder("cdotest-", "");
		org.eclipse.emf.cdo.internal.explorer.bundle.OM.initializeManagers(testFolder);
		houseKeeper.cleanUpLater(testFolder, new HouseKeeper.Disposer<File>() {
			@Override
			public void dispose(File testFolder) throws Exception {
				IOUtil.delete(testFolder);
			}
		});

		container = createServerContainer();
		InternalRepository remoteRepository = createRemoteRepository(container);
		String repositoryName = remoteRepository.getName();

		repository = createRepository(repositoryName);

		if (needCheckout()) {
			checkout = createCheckout(houseKeeper.getTestName());

			// ensure the test resource path
			CDOTransaction transaction = checkout.openTransaction();
			transaction.getOrCreateResourceFolder(getResourceFolder());
			transaction.commit();
			transaction.close();
		}
	}

	private CDORepository createRepository(String repositoryName) {
		Properties properties = new Properties();
		properties.setProperty(RemoteCDORepository.PROP_TYPE, CDORepository.TYPE_REMOTE);
		properties.setProperty(RemoteCDORepository.PROP_LABEL, repositoryName);
		properties.setProperty(RemoteCDORepository.PROP_NAME, repositoryName);
		properties.setProperty(RemoteCDORepository.PROP_CONNECTOR_TYPE, "jvm");
		properties.setProperty(RemoteCDORepository.PROP_CONNECTOR_DESCRIPTION, "default");

		CDORepository repository = CDOExplorerUtil.getRepositoryManager().addRepository(properties);
		repository.connect();
		return repository;
	}

	private CDOCheckout createCheckout(String checkoutName) {
		Properties properties = new Properties();
		properties.setProperty(OnlineCDOCheckout.PROP_TYPE, CDOCheckout.TYPE_ONLINE_HISTORICAL);
		properties.setProperty(OnlineCDOCheckout.PROP_LABEL, checkoutName);
		properties.setProperty(OnlineCDOCheckout.PROP_REPOSITORY, repository.getID());
		properties.setProperty(OnlineCDOCheckout.PROP_BRANCH_ID, Integer.toString(CDOBranch.MAIN_BRANCH_ID));
		properties.setProperty(OnlineCDOCheckout.PROP_TIME_STAMP, Long.toString(CDOBranchPoint.UNSPECIFIED_DATE));
		properties.setProperty(OnlineCDOCheckout.PROP_READ_ONLY, Boolean.toString(false));

		CDOCheckout checkout = CDOExplorerUtil.getCheckoutManager().addCheckout(properties);
		checkout.open();
		return checkout;
	}

	protected HouseKeeper.Disposer<IManagedContainer> containerDisposer() {
		return new HouseKeeper.Disposer<IManagedContainer>() {

			@Override
			public void dispose(IManagedContainer object) {
				LifecycleUtil.deactivate(object);
			}
		};
	}

	protected HouseKeeper.Disposer<CDOCheckout> checkoutDisposer() {
		return new HouseKeeper.Disposer<CDOCheckout>() {

			@Override
			public void dispose(CDOCheckout checkout) throws Exception {
				Exception exception = null;
				try {
					checkout.close();
				} catch (Exception e) {
					exception = e;
				}

				if (exception != null) {
					throw exception;
				}
			}
		};
	}

	protected Map<String, String> getRepositoryProperties() {
		Map<String, String> result = Maps.newHashMap();

		addRepositoryProperties(result);

		return result;
	}

	protected void addRepositoryProperties(Map<String, String> props) {
		props.put(Props.OVERRIDE_UUID, ""); // use the name as the UUID
		props.put(Props.SUPPORTING_AUDITS, "false");
		props.put(Props.SUPPORTING_BRANCHES, "false");
		props.put(Props.ID_GENERATION_LOCATION, CDOCommonRepository.IDGenerationLocation.STORE.toString());
	}

	protected IManagedContainer createServerContainer() {
		IManagedContainer result = houseKeeper.cleanUpLater(ContainerUtil.createContainer(), containerDisposer());

		prepareContainer(result);

		LifecycleUtil.activate(result);

		return result;
	}

	protected void prepareContainer(IManagedContainer container) {
		Net4jUtil.prepareContainer(container);
		CDONet4jUtil.prepareContainer(container);
		CDONet4jServerUtil.prepareContainer(container);
		JVMUtil.prepareContainer(container);
	}

	protected InternalRepository createRemoteRepository(IManagedContainer container) {
		InternalRepository result = (InternalRepository) CDOServerUtil.createRepository("MEM", MEMStoreUtil.createMEMStore(), getRepositoryProperties());

		CDOServerUtil.addRepository(container, result);
		LifecycleUtil.activate(JVMUtil.getAcceptor(container, "default"));
		return result;
	}

	protected CDOCheckout getCheckout() {
		return checkout;
	}

	protected CDOCheckoutImpl getInternalCheckout() {
		return (CDOCheckoutImpl) getCheckout();
	}

	protected CDOTransaction createTransaction() {
		CDOCheckout repo = getInternalCheckout();
		CDOTransaction result = repo.openTransaction(createResourceSet());
		return houseKeeper.cleanUpLater(result, viewDisposer(repo));
	}

	protected ResourceSet createTransaction(ResourceSet resourceSet) {
		CDOCheckout checkout = getCheckout();
		checkout.openTransaction(resourceSet);
		return resourceSet;
	}

	protected CDOView createView() {
		CDOCheckout repo = getInternalCheckout();
		CDOView result = repo.openView(true, createResourceSet());
		return houseKeeper.cleanUpLater(result, viewDisposer(repo));
	}

	protected void commit(ResourceSet resourceSet) {
		CDOTransaction transaction = (CDOTransaction) CDOUtil.getView(resourceSet);

		try {
			transaction.commit();
		} catch (ConcurrentAccessException e) {
			throw new TransactionException(e);
		} catch (CommitException e) {
			throw new TransactionException(e);
		}
	}

	protected void close(ResourceSet resourceSet) {
		CDOView view = CDOUtil.getView(resourceSet);
		LifecycleUtil.deactivate(view);
	}

	protected void close(CDOCheckout checkout, CDOView view) {
		ResourceSet rset = view.getResourceSet();

		// CDOResources don't implement unload(), but we can remove adapters from
		// all of the objects that we have loaded in this view
		CDOUtils.unload(view);

		for (Resource next : ImmutableList.copyOf(rset.getResources())) {
			next.unload();
		}

		if (checkout.isOpen()) {
			view.close();
		}

		rset.getResources().clear();
		rset.eAdapters().clear();

		// Clear the package registry (it may contain dynamic profile EPackages that we don't
		// want to leak in BasicExtendedMetaData instances attached to static EPackages)
		// Works around EMF bug 433108
		EPackage.Registry packageRegistry = rset.getPackageRegistry();
		if (packageRegistry != null) {
			packageRegistry.clear();
		}
	}

	protected HouseKeeper.Disposer<CDOView> viewDisposer(final CDOCheckout repository) {
		return new HouseKeeper.Disposer<CDOView>() {

			@Override
			public void dispose(CDOView object) {
				if (!object.isClosed()) {
					close(repository, object);
				}
			}
		};
	}

	private ResourceSet createResourceSet() {
		ResourceSet result = null;

		Method factory = getAnnotatedMethod(ResourceSetFactory.class);
		if (factory != null) {
			try {
				result = (ResourceSet) factory.invoke(this);
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
				fail("Failed to create resource set for test case: " + e.getTargetException().getLocalizedMessage());
			} catch (IllegalAccessException e) {
				fail("Resource-set factory method not accessible: " + factory.getName());
			}
		}

		if (result == null) {
			// default
			result = new ResourceSetImpl();
		}

		return result;
	}

	protected CDOTransaction getTransaction(ResourceSet resourceSet) {
		return cast(CDOUtil.getView(resourceSet), CDOTransaction.class);
	}

	protected final Method getAnnotatedMethod(Class<? extends Annotation> annotationType) {
		Method result = null;

		for (Method next : getClass().getMethods()) {
			if (next.isAnnotationPresent(annotationType)) {
				result = next;
				break;
			}
		}

		return result;
	}


	protected boolean needCheckout() {
		return true;
	}

	protected String getResourceFolder() {
		return String.format("/%s/%s", getClass().getSimpleName(), houseKeeper.getTestName());
	}

	protected String getResourcePath(String path) {
		java.util.regex.Matcher m = LEADING_SLASHES.matcher(path);
		if (m.find()) {
			path = path.substring(m.end());
		}
		return String.format("%s/%s", getResourceFolder(), path);
	}

	protected URI getTestResourceURI(String path) {
		return checkout.createResourceURI(getResourcePath(path));
		// return URI.createURI("cdo://" + repoUUID + getResourcePath(path), false);
	}

	protected URI getRepositoryURI() {
		return getInternalCheckout().getView().getRootResource().getURI();
	}

	protected URI getTestFolderURI() {
		// last segment adds the trailing separator
		return getRepositoryURI().appendSegment(getClass().getSimpleName()).appendSegment(houseKeeper.getTestName()).appendSegment("");
	}

	protected static <T> T cast(Object object, Class<T> type) {
		assertThat(object, instanceOf(type));
		return type.cast(object);
	}

	protected URI getTemplateResourceURI(String templateName, String resourceName, String extension) {
		return URI.createPlatformPluginURI(String.format("/org.eclipse.papyrus.cdo.core.tests/resources/%s/%s.%s", templateName, resourceName, extension), true);
	}

	protected void loadTemplate(String templateName, String resourceName, Resource... resource) {
		ResourceSet rset = houseKeeper.createResourceSet();

		// load all the templates
		Resource[] templates = new Resource[resource.length];
		for (int i = 0; i < templates.length; i++) {
			templates[i] = rset.getResource(getTemplateResourceURI(templateName, resourceName, resource[i].getURI().fileExtension()), true);
		}

		// resolve their cross-references
		for (int i = 0; i < templates.length; i++) {
			EcoreUtil.resolveAll(templates[i]);
		}

		// move them into the destination resources
		for (int i = 0; i < resource.length; i++) {
			resource[i].getContents().addAll(templates[i].getContents());
		}
	}

	protected <T extends EObject> T getMasterViewObject(T object) {
		CDOView view = getInternalCheckout().getView();
		return view.getObject(object);
	}

	public static <T extends Number & Comparable<T>> Matcher<T> lessThan(final T max) {
		return new BaseMatcher<T>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("< ").appendValue(max);
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object item) {
				return ((T) item).compareTo(max) < 0;
			}
		};
	}

	public static <T extends Number & Comparable<T>> Matcher<T> lessThanOrEqualTo(final T max) {
		return new BaseMatcher<T>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("<= ").appendValue(max);
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object item) {
				return ((T) item).compareTo(max) <= 0;
			}
		};
	}

	public static <T> Matcher<Iterable<T>> hasSize(final int size) {
		return new BaseMatcher<Iterable<T>>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("has size ").appendValue(size);
			}

			@Override
			public boolean matches(Object item) {
				return Iterables.size((Iterable<?>) item) == size;
			}
		};
	}
}
