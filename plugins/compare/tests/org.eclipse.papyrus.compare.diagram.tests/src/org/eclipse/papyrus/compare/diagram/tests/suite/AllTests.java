/*******************************************************************************
 * Copyright (c) 2013, 2017 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *     Stefan Dirix - add ModelExtensionUtil, SaveParameterHook and URIAttachment tests
 *     Philip Langer - add IngoreDiFileModelElementsTest
 *     Martin Fleck - add MergeNonConflictingCascadingDifferencesFilterTest
 *******************************************************************************/
package org.eclipse.papyrus.compare.diagram.tests.suite;

import org.eclipse.emf.compare.ComparePackage;
import org.eclipse.emf.compare.diagram.internal.extensions.ExtensionsPackage;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmf.runtime.emf.core.resources.GMFResourceFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.papyrus.compare.diagram.tests.comparison.DiagramTooltipProviderTest;
import org.eclipse.papyrus.compare.diagram.tests.context.PapyrusContextUtilTest;
import org.eclipse.papyrus.compare.diagram.tests.difile.IgnoreDiFilePostProcessorTest;
import org.eclipse.papyrus.compare.diagram.tests.groups.PapyrusConflictsGroupProviderTests;
import org.eclipse.papyrus.compare.diagram.tests.merge.AssocMergeTest;
import org.eclipse.papyrus.compare.diagram.tests.merge.EdgeMergeTest;
import org.eclipse.papyrus.compare.diagram.tests.merge.NodeMergeTest;
import org.eclipse.papyrus.compare.diagram.tests.merge.sysml.MergeDiffInvolvingRefineDiffTest;
import org.eclipse.papyrus.compare.diagram.tests.modelextension.ModelExtensionUtilTest;
import org.eclipse.papyrus.compare.diagram.tests.saveparameter.SaveParameterHookIntegrationTest;
import org.eclipse.papyrus.compare.diagram.tests.saveparameter.SaveParameterHookTest;
import org.eclipse.papyrus.compare.diagram.tests.structuremergeviewer.actions.MergeNonConflictingCascadingFilterTest;
import org.eclipse.papyrus.compare.diagram.tests.uriattachment.URIAttachmentTest;
import org.eclipse.papyrus.infra.core.sashwindows.di.DiPackage;
import org.eclipse.papyrus.infra.core.sashwindows.di.util.DiResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite allows us to launch all tests for EMF Compare at once.
 * 
 * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
 */
@SuppressWarnings("restriction")
@RunWith(Suite.class)
@SuiteClasses({AssocMergeTest.class, EdgeMergeTest.class, NodeMergeTest.class, ModelExtensionUtilTest.class,
		SaveParameterHookTest.class, SaveParameterHookIntegrationTest.class, URIAttachmentTest.class,
		DiagramTooltipProviderTest.class, PapyrusConflictsGroupProviderTests.class,
		IgnoreDiFilePostProcessorTest.class, PapyrusContextUtilTest.class,
		MergeNonConflictingCascadingFilterTest.class, MergeDiffInvolvingRefineDiffTest.class, })
public class AllTests {

	@BeforeClass
	public static void fillEMFRegistries() {
		EPackage.Registry.INSTANCE.put(ComparePackage.eNS_URI, ComparePackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(ExtensionsPackage.eNS_URI, ExtensionsPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(NotationPackage.eNS_URI, NotationPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(DiPackage.eNS_URI, DiPackage.eINSTANCE);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("di", //$NON-NLS-1$
				new DiResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("notation", //$NON-NLS-1$
				new GMFResourceFactory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("uml", //$NON-NLS-1$
				new UMLResourceFactoryImpl());
	}

}