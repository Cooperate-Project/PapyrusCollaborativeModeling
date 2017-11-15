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
package org.eclipse.papyrus.compare.diagram.tests;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.ide.EMFCompareIDEPlugin;
import org.eclipse.emf.compare.ide.hook.IResourceSetHook;
import org.eclipse.emf.compare.ide.internal.hook.ResourceSetHookRegistry;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.emf.core.resources.GMFResourceFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

@SuppressWarnings({"nls", "restriction" })
public class DiagramInputDataWithResourceSetHooks extends DiagramInputData {

	private ResourceSetHookRegistry hooks = EMFCompareIDEPlugin.getDefault().getResourceSetHookRegistry();

	@Override
	protected Resource loadFromClassLoader(String string) throws IOException {
		final URL url = getClass().getResource(string);
		final URI uri = URI.createURI(url.toString(), true);

		final Collection<URI> uris = ImmutableSet.of(uri.trimFileExtension().appendFileExtension("di"),
				uri.trimFileExtension().appendFileExtension("uml"),
				uri.trimFileExtension().appendFileExtension("notation"));

		ResourceSet resourceSet = createResourceSet(uris);
		getSets().add(resourceSet);

		if (!EMFPlugin.IS_RESOURCES_BUNDLE_AVAILABLE) {
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("uml",
					new UMLResourceFactoryImpl());
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("notation",
					new GMFResourceFactory());
			EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
			EPackage.Registry.INSTANCE.put(NotationPackage.eNS_URI, NotationPackage.eINSTANCE);
		}

		for (URI next : uris) {
			if (resourceSet.getURIConverter().exists(next, null)) {
				resourceSet.getResource(next, true);
			}
		}

		postLoad(resourceSet);

		EcoreUtil.resolveAll(resourceSet);

		return resourceSet.getResource(uri, false);
	}

	protected ResourceSet createResourceSet(Collection<URI> resourceURIs) {
		ResourceSet result = new ResourceSetImpl();

		preLoad(result, resourceURIs);

		return result;
	}

	protected void preLoad(ResourceSet rset, Collection<URI> uris) {
		for (IResourceSetHook hook : hooks.getResourceSetHooks()) {
			if (hook.isHookFor(uris)) {
				hook.preLoadingHook(rset, uris);
			}
		}
	}

	protected void postLoad(ResourceSet rset) {
		Collection<URI> uris = getURIs(rset);

		for (IResourceSetHook hook : hooks.getResourceSetHooks()) {
			if (hook.isHookFor(uris)) {
				hook.postLoadingHook(rset, uris);
			}
		}
	}

	Collection<URI> getURIs(ResourceSet rset) {
		return newArrayList(transform(rset.getResources(), new Function<Resource, URI>() {
			public URI apply(Resource input) {
				return input.getURI();
			}
		}));
	}
}
