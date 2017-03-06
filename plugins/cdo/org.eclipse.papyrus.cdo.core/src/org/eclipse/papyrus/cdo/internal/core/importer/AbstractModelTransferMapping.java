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
 *   Eike Stepper (CEA) - bug 466520
 *****************************************************************************/
package org.eclipse.papyrus.cdo.internal.core.importer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.cdo.core.importer.IModelTransferConfiguration;
import org.eclipse.papyrus.cdo.core.importer.IModelTransferListener;
import org.eclipse.papyrus.cdo.core.importer.IModelTransferMapping;
import org.eclipse.papyrus.cdo.core.importer.IModelTransferMappingListener;
import org.eclipse.papyrus.cdo.core.importer.IModelTransferNode;
import org.eclipse.papyrus.cdo.core.importer.ModelTransferListenerAdapter;
import org.eclipse.papyrus.cdo.internal.core.Activator;
import org.eclipse.papyrus.cdo.internal.core.l10n.Messages;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This is the AbstractModelTransferMapping type. Enjoy.
 */
public abstract class AbstractModelTransferMapping implements IModelTransferMapping {

	private final IModelTransferConfiguration config;

	private final Map<IModelTransferNode, IPath> mappings = Maps.newHashMap();

	private CDOCheckout checkout;

	private final CopyOnWriteArrayList<IModelTransferMappingListener> listeners = new CopyOnWriteArrayList<IModelTransferMappingListener>();

	public AbstractModelTransferMapping(IModelTransferConfiguration config) {
		super();

		this.config = config;

		config.addModelTransferListener(createConfigurationListener());
	}

	@Override
	public IModelTransferConfiguration getConfiguration() {
		return config;
	}

	@Override
	public void mapTo(IModelTransferNode source, IPath path) {
		if (!Objects.equal(getMapping(source), path)) {
			mappings.put(source, path);

			fireMappingChanged(source);
		}
	}

	@Override
	public IPath getMapping(IModelTransferNode node) {
		return mappings.get(node);
	}

	@Override
	public CDOCheckout getCheckout() {
		return checkout;
	}

	@Override
	public void setCheckout(CDOCheckout checkout) {
		if (checkout != this.checkout) {
			this.checkout = checkout;

			fireCheckoutChanged();
		}
	}

	@Override
	public Diagnostic validate() {
		BasicDiagnostic result = new BasicDiagnostic();

		if (validateRepository(result)) {
			for (IModelTransferNode node : getConfiguration().getModelsToTransfer()) {
				validateMapping(node, result);
			}

			validateUniqueMappings(result);
		}

		fireProblemsEvent(result);

		return result;
	}

	protected abstract boolean validateMapping(IModelTransferNode node, DiagnosticChain diagnostics);

	protected void validateUniqueMappings(DiagnosticChain diagnostics) {
		Set<IPath> paths = Sets.newHashSet();

		for (IModelTransferNode next : getConfiguration().getModelsToTransfer()) {
			IPath mapping = getMapping(next);

			if ((mapping != null) && !paths.add(mapping)) {
				diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, Activator.PLUGIN_ID, 0, NLS.bind(Messages.AbstractModelTransferMapping_0, mapping), new Object[] { next }));
				break;
			}
		}
	}

	protected boolean validateRepository(DiagnosticChain diagnostics) {
		boolean result = true;

		if (getCheckout() == null) {
			diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, Activator.PLUGIN_ID, 0, Messages.AbstractModelTransferMapping_1, null));
			result = false;
		} else if (!getCheckout().isOpen()) {
			diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, Activator.PLUGIN_ID, 0, NLS.bind(Messages.AbstractModelTransferMapping_2, getCheckout().getLabel()), new Object[] { getCheckout() }));
			result = false;
		}

		return result;
	}

	@Override
	public void addModelTransferMappingListener(IModelTransferMappingListener listener) {
		listeners.addIfAbsent(listener);
	}

	@Override
	public void removeModelTransferMappingListener(IModelTransferMappingListener listener) {
		listeners.remove(listener);
	}

	protected void fireProblemsEvent(Diagnostic problems) {
		if (problems.getSeverity() > Diagnostic.OK) {
			for (IModelTransferMappingListener next : listeners) {
				try {
					next.modelTransferMappingProblemsOccurred(problems);
				} catch (Exception e) {
					Activator.log.error("Uncaught exception in model transfer mapping listener.", e); //$NON-NLS-1$
				}
			}
		}
	}

	protected void fireMappingChanged(IModelTransferNode node) {
		for (IModelTransferMappingListener next : listeners) {
			try {
				next.modelTransferMappingChanged(node);
			} catch (Exception e) {
				Activator.log.error("Uncaught exception in model transfer mapping listener.", e); //$NON-NLS-1$
			}
		}
	}

	protected void fireCheckoutChanged() {
		for (IModelTransferMappingListener next : listeners) {
			try {
				next.modelTransferRepositoryChanged(this);
			} catch (Exception e) {
				Activator.log.error("Uncaught exception in model transfer mapping listener.", e); //$NON-NLS-1$
			}
		}
	}

	private IModelTransferListener createConfigurationListener() {
		return new ModelTransferListenerAdapter() {

			@Override
			public void modelsToTransferChanged(IModelTransferConfiguration configuration) {
				computeDefaultMappings(configuration);
			}
		};
	}

	protected void computeDefaultMappings(IModelTransferConfiguration configuration) {
		// pass
	}
}
