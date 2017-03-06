/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Christian W. Damus (CEA) - bug 431953 (fix start-up of selective services to require only their dependencies)
 *   Christian W. Damus - bugs 436998, 485220
 *   Eike Stepper (CEA) - bug 466520
 *
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.core.controlmode.tests;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.papyrus.cdo.core.resource.CDOAwareModelSet;
import org.eclipse.papyrus.cdo.core.tests.AbstractPapyrusCDOTest;
import org.eclipse.papyrus.cdo.core.tests.ResourceSetFactory;
import org.eclipse.papyrus.infra.core.Activator;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResource;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResourceSet;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeManager;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeRequest;
import org.eclipse.papyrus.infra.services.resourceloading.IStrategyChooser;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Lists;

/**
 * Basic tests for controlled-resource support in CDO model repositories.
 */
public class CDOControlModeTest extends AbstractPapyrusCDOTest {

	private final List<Runnable> cleanupActions = Lists.newArrayListWithExpectedSize(2);

	private ServicesRegistry strategyRegistry;
	private int oldControlModeStrategy;

	public CDOControlModeTest() {
		super();
	}

	@Test
	public void testControlOneResource() {
		CDOResource res = createUMLModel();
		ResourceSet resourceSet = res.getResourceSet();

		Package root = getPackage(res);
		Package sub1 = root.getNestedPackages().get(0);

		save(res); // save now so that all model elements are already persistent

		control(sub1, "sub1.uml");
		save(res);

		// create a new view in which to re-load the model

		CDOView view = createView();
		CDOResource res2 = view.getResource(res.getPath());
		Package root2 = getPackage(res2);

		InternalEList<Package> subs = (InternalEList<Package>) root2.getNestedPackages();
		assertProxy(subs.basicGet(0), "sub1.uml");
	}

	@Test
	public void testControlOneResource_newlyAddedObjects() {
		CDOResource res = createUMLModel();
		Package root = getPackage(res);

		Package sub1 = root.getNestedPackages().get(0);

		control(sub1, "sub1.uml");

		save(res);

		// create a new view in which to re-load the model

		CDOView view = createView();
		CDOResource res2 = view.getResource(res.getPath());
		Package root2 = getPackage(res2);

		InternalEList<Package> subs = (InternalEList<Package>) root2.getNestedPackages();
		assertProxy(subs.basicGet(0), "sub1.uml");
	}

	@Test
	public void testUncontrolOneResource() {
		CDOResource res = createUMLModel();
		Package root = getPackage(res);

		Package sub1 = root.getNestedPackages().get(0);

		control(sub1, "sub1.uml");

		save(res);

		// create a new view in which to re-load the model

		CDOView view = createView();
		CDOResource res2 = view.getResource(res.getPath());
		Package root2 = getPackage(res2);

		InternalEList<Package> subs = (InternalEList<Package>) root2.getNestedPackages();
		assumeProxy(subs.basicGet(0), "sub1.uml");

		uncontrol(sub1);

		save(res);

		view = createView();
		res2 = view.getResource(res.getPath());
		root2 = getPackage(res2);

		subs = (InternalEList<Package>) root2.getNestedPackages();
		assertNotProxy(subs.basicGet(0));
	}

	//
	// Test framework
	//

	@Before
	public void setControlModeStrategy() throws Exception {
		final int MAGIC_NUMBER_OF_DONT_LOAD_ANYTHING_STRATEGY = 2;

		strategyRegistry = new ExtensionServicesRegistry(Activator.PLUGIN_ID);

		// We know that it has no dependencies
		strategyRegistry.startServicesByClassKeys(IStrategyChooser.class);
		IStrategyChooser chooser = strategyRegistry.getService(IStrategyChooser.class);

		oldControlModeStrategy = chooser.getCurrentStrategy();
		chooser.setStrategy(MAGIC_NUMBER_OF_DONT_LOAD_ANYTHING_STRATEGY);
	}

	@After
	public void restoreControlModeStrategy() throws Exception {
		if (strategyRegistry != null) {
			try {
				strategyRegistry.getService(IStrategyChooser.class).setStrategy(oldControlModeStrategy);
			} finally {
				try {
					strategyRegistry.disposeRegistry();
				} finally {
					strategyRegistry = null;
				}
			}
		}
	}

	@After
	public void cleanup() {
		for (Runnable next : cleanupActions) {
			next.run();
		}

		cleanupActions.clear();
	}

	void cleanupLater(Runnable runnable) {
		cleanupActions.add(runnable);
	}

	@ResourceSetFactory
	public ModelSet createModelSet() throws ServiceException {
		ModelSet result = null;

		// Since Mars, an IUncontrolledObjectsProvider service is required by the control-mode framework
		final ServicesRegistry services = new ExtensionServicesRegistry(Activator.PLUGIN_ID);

		try {
			cleanupLater(new Runnable() {

				@Override
				public void run() {
					try {
						services.disposeRegistry();
					} catch (ServiceException e) {
						// ignore (best-effort clean-up)
					}
				}
			});

			services.add(ModelSet.class, Integer.MAX_VALUE, new CDOAwareModelSet());
			services.startRegistry();

			result = services.getService(ModelSet.class);
		} catch (ServiceException e) {
			Bundle me = FrameworkUtil.getBundle(getClass());
			Platform.getLog(me).log(new Status(IStatus.ERROR, me.getSymbolicName(), "Exception in creating service registry for ModelSet", e));
			result = services.getService(ModelSet.class);
		}

		return result;
	}

	CDOResource createUMLModel() {
		CDOTransaction transaction = createTransaction();
		CDOResource result = transaction.createResource(getResourcePath("test.uml"));

		try {
			ModelSet model = ServiceUtilsForResourceSet.getInstance().getModelSet(transaction.getResourceSet());
			model.createModels(result.getURI());
		} catch (ServiceException e) {
			e.printStackTrace();
			fail("Failed to initialize Papyrus model: " + e.getLocalizedMessage());
		}

		result.getContents().add(createTestPackageTree());
		return result;
	}

	Package createTestPackageTree() {
		Package result = createPackage("test");

		Package sub1 = createPackage("sub1");
		result.getNestedPackages().add(sub1);
		sub1.getNestedPackages().add(createPackage("sub1sub1"));
		sub1.getNestedPackages().add(createPackage("sub1sub2"));
		result.getNestedPackages().add(createPackage("sub2"));

		return result;
	}

	Package createPackage(String name) {
		Package result = UMLFactory.eINSTANCE.createPackage();
		result.setName(name);
		result.setURI(String.format("http://www.eclipse.org/papyrus/test/%s/2013", name));
		return result;
	}

	Package getPackage(Resource resource) {
		return (Package) find(resource.getContents(), instanceOf(Package.class), null);
	}

	void save(Resource context) {
		try {
			ServiceUtilsForResource.getInstance().getModelSet(context).save(new NullProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed to save model: " + e.getLocalizedMessage());
		}
	}

	private ControlModeRequest request(EObject objectToControl, String fragmentName) {
		ControlModeRequest result = null;

		try {
			ModelSet modelSet = ServiceUtilsForEObject.getInstance().getModelSet(objectToControl);
			URI fragmentURI = objectToControl.eResource().getURI().trimSegments(1).appendSegment(fragmentName);
			result = new ControlModeRequest(modelSet.getTransactionalEditingDomain(), objectToControl, fragmentURI);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed to create ControlModeRequest: " + e.getLocalizedMessage());
		}

		return result;
	}

	URI control(EObject objectToControl, String fragmentName) {
		ControlModeRequest request = request(objectToControl, fragmentName);
		ICommand control = ControlModeManager.getInstance().getControlCommand(request);

		try {
			control.execute(new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail("Failed to execute control-mode command: " + e.getLocalizedMessage());
		}

		return request.getNewURI();
	}

	private ControlModeRequest request(EObject objectToControl) {
		ControlModeRequest result = null;

		try {
			ModelSet modelSet = ServiceUtilsForEObject.getInstance().getModelSet(objectToControl);
			result = new ControlModeRequest(modelSet.getTransactionalEditingDomain(), objectToControl);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed to create ControlModeRequest: " + e.getLocalizedMessage());
		}

		return result;
	}

	void uncontrol(EObject objectToControl) {
		ICommand uncontrol = ControlModeManager.getInstance().getUncontrolCommand(request(objectToControl));

		try {
			uncontrol.execute(new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail("Failed to execute control-mode command: " + e.getLocalizedMessage());
		}
	}

	void assertProxy(EObject object, String uriSubString) {
		assertThat("Not a proxy", object.eIsProxy(), is(true));
		assertThat(EcoreUtil.getURI(object).toString(), containsString(uriSubString));
	}

	void assumeProxy(EObject object, String uriSubString) {
		assumeThat("Not a proxy", object.eIsProxy(), is(true));
		assumeThat(EcoreUtil.getURI(object).toString(), containsString(uriSubString));
	}

	void assertNotProxy(EObject object) {
		assertThat("Object is a proxy", object.eIsProxy(), is(false));
	}

	void waitForUpdate(CDOView view, long updateTime) {
		assertThat("Timed out waiting for read-only view to update.", view.waitForUpdate(updateTime, 5000L), is(true));
	}
}
