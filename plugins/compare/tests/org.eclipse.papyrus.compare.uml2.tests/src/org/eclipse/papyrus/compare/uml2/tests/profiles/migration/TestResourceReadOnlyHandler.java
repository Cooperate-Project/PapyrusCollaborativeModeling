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
package org.eclipse.papyrus.compare.uml2.tests.profiles.migration;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.infra.core.resource.AbstractReadOnlyHandler;
import org.eclipse.papyrus.infra.core.resource.ReadOnlyAxis;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.base.Optional;

/**
 * Read-only handler that allows migration of test resources.
 *
 * @author Christian W. Damus
 */
public class TestResourceReadOnlyHandler extends AbstractReadOnlyHandler {
	private static boolean enabled;
	
	public TestResourceReadOnlyHandler(EditingDomain editingDomain) {
		super(editingDomain);
	}

	public Optional<Boolean> anyReadOnly(Set<ReadOnlyAxis> axes, URI[] uris) {
		Optional<Boolean> result = Optional.absent();
		
		if (enabled && axes.contains(ReadOnlyAxis.PERMISSION)) {
			for (URI next : uris) {
				if ("bundleresource".equals(next.scheme())) { //$NON-NLS-1$
					// Looks okay so far, unless it's a profile
					if (next.lastSegment().endsWith(".profile.uml")) { //$NON-NLS-1$
						result = Optional.of(Boolean.TRUE);
						break;
					} else {
						result = Optional.of(Boolean.FALSE);
					}
				}
			}
		}
		
		return result;
	}

	public Optional<Boolean> makeWritable(Set<ReadOnlyAxis> axes, URI[] uris) {
		// Not used in the tests, anyways
		return Optional.absent();
	}

	//
	// Nested types
	//
	
	/**
	 * A JUnit rule that enables the {@link TestResourceReadOnlyHandler} for
	 * the furation of its scope.
	 *
	 * @author Christian W. Damus
	 */
	public static final class Rule implements TestRule {
		public Statement apply(final Statement base, Description description) {
			return new Statement() {
				
				@Override
				public void evaluate() throws Throwable {
					boolean oldEnabled = enabled;
					enabled = true;
					
					try {
						base.evaluate();
					} finally {
						enabled = oldEnabled;
					}
				}
			};
		}
	}
}
