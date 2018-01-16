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
package org.eclipse.papyrus.compare.uml2.tests.profiles.migration;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.compare.uml2.internal.hook.migration.ModelSetWrapper;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link ModelSetWrapper} class.
 *
 * @author Christian W. Damus
 */
@SuppressWarnings({"nls", "boxing" })
public class ModelSetWrapperTest {

	private static final URI STANDARD_PROFILE_URI = URI.createURI(UMLResource.STANDARD_PROFILE_URI);

	private ResourceSet fixture;

	/**
	 * Initializes me.
	 */
	public ModelSetWrapperTest() {
		super();
	}

	@Test
	public void resourceWrapper() {
		Resource resource = fixture.getResource(STANDARD_PROFILE_URI, true);

		assertThat(resource, isProxy());
		assertThat(resource.getURI(), is(STANDARD_PROFILE_URI));
		assertThat(resource.isLoaded(), is(true));
		assertThat(resource.getContents(), CoreMatchers.<EObject> hasItem(instanceOf(Profile.class)));
	}

	@Test
	public void notifications() {
		final ListMultimap<URI, String> invocations = ArrayListMultimap.create();

		class TestAdapter extends AdapterImpl {
			private Pattern invocationPattern = Pattern.compile("(.+?)( x(\\d+))?");

			@Override
			public boolean isAdapterForType(Object type) {
				if (getTarget() instanceof Resource) {
					recordInvocation(getTarget(), "isAdapter");
				}
				return type == ModelSetWrapperTest.class;
			}

			void recordInvocation(Object context, String label) {
				if (context instanceof Notification) {
					recordInvocation(((Notification)context).getNotifier(), label);
				} else if (context instanceof URI) {
					recordInvocation((URI)context, label);
				} else if (context instanceof Resource) {
					recordInvocation(((Resource)context).getURI(), label);
				} else {
					recordInvocation(null, label);
				}
			}

			void recordInvocation(URI context, String label) {
				if (!invocations.containsKey(context)) {
					invocations.put(context, label);
				} else {
					final List<String> these = invocations.get(context);
					int lastIndex = these.size() - 1;
					String last = these.get(lastIndex);
					java.util.regex.Matcher m = invocationPattern.matcher(last);

					if (!m.matches() || !m.group(1).equals(label)) {
						these.add(label);
					} else if (m.group(3) != null) {
						// Increment the count
						int count = Integer.parseInt(m.group(3));
						these.set(lastIndex, String.format("%s x%s", label, count + 1));
					} else {
						// Start the count
						these.set(lastIndex, String.format("%s x2", label));
					}
				}
			}

			@Override
			public void notifyChanged(Notification msg) {
				if (msg.getNotifier() instanceof Resource) {
					if (msg.getEventType() == Notification.REMOVING_ADAPTER) {
						recordInvocation(msg, "notifyChanged: removingAdapter");
					} else {
						switch (msg.getFeatureID(Resource.class)) {
							case Resource.RESOURCE__IS_LOADED:
								if (msg.getNewBooleanValue()) {
									recordInvocation(msg, "loaded");
								} else {
									recordInvocation(msg, "unloaded");
								}
								break;
							case Resource.RESOURCE__CONTENTS:
							case Resource.RESOURCE__IS_TRACKING_MODIFICATION:
							case Resource.RESOURCE__IS_MODIFIED:
								recordInvocation(msg, "notifyChanged: "
										+ resourceFeature(msg.getFeatureID(Resource.class)));
								break;
							default:
								// Pass
								break;
						}
					}
					assertThat(msg.getNotifier(), isProxy());
				} else if (msg.getNotifier() instanceof ResourceSet) {
					handleResourceSet(msg);
				}
			}

			@Override
			public void setTarget(Notifier newTarget) {
				// only track resources
				if (newTarget instanceof Resource) {
					recordInvocation(newTarget, "setTarget");
					assertThat(newTarget, isProxy());
					super.setTarget(newTarget);
				}
			}

			@Override
			public void unsetTarget(Notifier oldTarget) {
				// only track resources
				if (oldTarget instanceof Resource) {
					recordInvocation(oldTarget, "unsetTarget");
					assertThat(oldTarget, isProxy());
					super.unsetTarget(oldTarget);
				}
			}

			protected void handleResourceSet(Notification msg) {
				switch (msg.getFeatureID(ResourceSet.class)) {
					case ResourceSet.RESOURCE_SET__RESOURCES:
						switch (msg.getEventType()) {
							case Notification.ADD:
								((Resource)msg.getNewValue()).eAdapters().add(this);
								break;
							case Notification.ADD_MANY:
								for (Object next : (Collection<?>)msg.getNewValue()) {
									((Resource)next).eAdapters().add(this);
								}
								break;
							case Notification.REMOVE:
								((Resource)msg.getOldValue()).eAdapters().remove(this);
								break;
							case Notification.REMOVE_MANY:
								for (Object next : (Collection<?>)msg.getOldValue()) {
									((Resource)next).eAdapters().remove(this);
								}
								break;
							case Notification.SET:
								((Resource)msg.getOldValue()).eAdapters().remove(this);
								((Resource)msg.getNewValue()).eAdapters().add(this);
								break;
							default:
								// Pass
								break;
						}
						break;
					default:
						// Pass
						break;
				}

			}
		}

		try {
			fixture.eAdapters().add(new TestAdapter());

			Resource resource = fixture.getResource(STANDARD_PROFILE_URI, true);
			assumeThat(resource, isProxy());

			Adapter adapter = EcoreUtil.getExistingAdapter(resource, ModelSetWrapperTest.class);
			assertThat(adapter, notNullValue());
			assertThat(resource.eAdapters(), hasItem(adapter));

			resource.unload();
			fixture.getResources().remove(resource);

			// The adapter removes itself from the resource when the resource is
			// removed from the set
			assertThat(resource.eAdapters(), not(hasItem(adapter)));
		} catch (UndeclaredThrowableException e) {
			for (Throwable unwrap = e.getUndeclaredThrowable(); unwrap != null; unwrap = unwrap.getCause()) {
				if (unwrap instanceof Error) {
					throw (Error)unwrap; // Usually AssertionError
				}
			}
			throw e; // Re-throw
		}

		assertThat(invocations.get(STANDARD_PROFILE_URI), is(Arrays.asList( //
				"setTarget", //
				"notifyChanged: isTrackingModification", // because ModelSet does that
				"notifyChanged: isModified x2", //
				"notifyChanged: contents", // loaded all at once
				"notifyChanged: isModified", //
				"loaded", //
				"notifyChanged: isModified x3", // unloading
				"notifyChanged: contents", // unloaded all at once
				"unloaded", //
				"notifyChanged: removingAdapter", //
				"unsetTarget" //
		)));
	}

	//
	// Test framework
	//

	@Before
	public void createFixture() {
		ResourceSet rset = new ResourceSetImpl();
		fixture = new ModelSetWrapper(rset);
	}

	@After
	public void destroyFixture() {
		ModelSetWrapper wrapper = (ModelSetWrapper)fixture;
		for (Resource next : wrapper.getResources()) {
			next.unload();
		}
		wrapper.getResources().clear();
		wrapper.detach();
	}

	Matcher<Object> isProxy() {
		return new BaseMatcher<Object>() {
			public void describeTo(Description description) {
				description.appendText("is a Java proxy");
			}

			public boolean matches(Object item) {
				return item != null && Proxy.isProxyClass(item.getClass());
			}
		};
	}

	static String resourceFeature(int featureID) {
		switch (featureID) {
			case Resource.RESOURCE__CONTENTS:
				return "contents";
			case Resource.RESOURCE__ERRORS:
				return "errors";
			case Resource.RESOURCE__IS_LOADED:
				return "isLoaded";
			case Resource.RESOURCE__IS_MODIFIED:
				return "isModified";
			case Resource.RESOURCE__IS_TRACKING_MODIFICATION:
				return "isTrackingModification";
			case Resource.RESOURCE__RESOURCE_SET:
				return "resourceSet";
			case Resource.RESOURCE__TIME_STAMP:
				return "timestamp";
			case Resource.RESOURCE__URI:
				return "uri";
			case Resource.RESOURCE__WARNINGS:
				return "warnings";
			default:
				return "<other>";
		}
	}
}
