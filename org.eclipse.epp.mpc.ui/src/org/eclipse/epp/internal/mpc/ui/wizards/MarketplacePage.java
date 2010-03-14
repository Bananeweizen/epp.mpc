/*******************************************************************************
 * Copyright (c) 2010 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.internal.mpc.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceDiscoveryStrategy;
import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceViewer.ContentType;
import org.eclipse.epp.mpc.ui.CatalogDescriptor;
import org.eclipse.equinox.internal.p2.discovery.AbstractDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogPage;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogViewer;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author Steffen Pingel
 */
public class MarketplacePage extends CatalogPage {

	private final MarketplaceCatalogConfiguration configuration;

	private CatalogDescriptor previousCatalogDescriptor;

	private boolean updated;

	private boolean switchLinkActivated;

	public MarketplacePage(MarketplaceCatalog catalog, MarketplaceCatalogConfiguration configuration) {
		super(catalog);
		this.configuration = configuration;
		setDescription("Select solutions to install. Press Finish to proceed with installation.\nPress the information button to see a detailed overview and a link to more information.");
		setTitle("Eclipse Marketplace Solutions");
	}

	@Override
	public void createControl(final Composite originalParent) {
		Composite parent = originalParent;
		boolean needSwitchMarketplaceLink = configuration.getCatalogDescriptors().size() > 1;
		if (needSwitchMarketplaceLink) {
			parent = new Composite(parent, SWT.NULL);
			GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 5).applyTo(parent);
		}

		TabFolder tabFolder = new TabFolder(parent, SWT.TOP);
		if (originalParent != parent) {
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tabFolder);
		}

		final TabItem searchTabItem = new TabItem(tabFolder, SWT.NULL);
		searchTabItem.setText("Search");
		final TabItem recentTabItem = new TabItem(tabFolder, SWT.NULL);
		recentTabItem.setText("Recent");
		final TabItem popularTabItem = new TabItem(tabFolder, SWT.NULL);
		popularTabItem.setText("Popular");
		final TabItem installedTabItem = new TabItem(tabFolder, SWT.NULL);
		installedTabItem.setText("Installed");

		super.createControl(tabFolder);

		searchTabItem.setControl(getControl());
		recentTabItem.setControl(getControl());
		popularTabItem.setControl(getControl());
		installedTabItem.setControl(getControl());

		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				MarketplaceViewer.ContentType contentType;
				if (e.item == searchTabItem) {
					contentType = ContentType.SEARCH;
				} else if (e.item == recentTabItem) {
					contentType = ContentType.RECENT;
				} else if (e.item == popularTabItem) {
					contentType = ContentType.POPULAR;
				} else if (e.item == installedTabItem) {
					contentType = ContentType.INSTALLED;
				} else {
					throw new IllegalStateException();
				}
				getViewer().setContentType(contentType);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		if (needSwitchMarketplaceLink) {
			Link link = new Link(parent, SWT.NULL);
			link.setText("<a>Switch Marketplace Catalog</a>");
			link.setToolTipText("Select an alternate catalog");
			link.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					switchMarketplaceLinkActivated();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);

				}
			});

			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(link);
		}

		setControl(parent == originalParent ? tabFolder : parent);
	}

	protected void switchMarketplaceLinkActivated() {
		switchLinkActivated = true;
		getWizard().getContainer().showPage(getWizard().getCatalogSelectionPage());
	}

	@Override
	public IWizardPage getPreviousPage() {
		if (!switchLinkActivated) {
			return null;
		}
		return super.getPreviousPage();
	}

	@Override
	public MarketplaceWizard getWizard() {
		return (MarketplaceWizard) super.getWizard();
	}

	@Override
	protected MarketplaceViewer getViewer() {
		return (MarketplaceViewer) super.getViewer();
	}

	@Override
	protected CatalogViewer doCreateViewer(Composite parent) {
		MarketplaceViewer viewer = new MarketplaceViewer(getCatalog(), this, getContainer(),
				getWizard().getConfiguration());
		viewer.setMinimumHeight(MINIMUM_HEIGHT);
		viewer.createControl(parent);
		return viewer;
	}

	@Override
	protected void doUpdateCatalog() {
		if (!updated) {
			updated = true;
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					if (!getControl().isDisposed() && isCurrentPage()) {
						getViewer().updateCatalog();
					}
				}
			});
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			CatalogDescriptor catalogDescriptor = configuration.getCatalogDescriptor();
			if (previousCatalogDescriptor == null || !previousCatalogDescriptor.equals(catalogDescriptor)) {
				previousCatalogDescriptor = catalogDescriptor;
				for (AbstractDiscoveryStrategy strategy : getCatalog().getDiscoveryStrategies()) {
					strategy.dispose();
				}
				getCatalog().getDiscoveryStrategies().clear();
				getCatalog().getDiscoveryStrategies().add(new MarketplaceDiscoveryStrategy(catalogDescriptor));
				updated = false;

				setTitle(catalogDescriptor.getLabel());
			}
		}
		super.setVisible(visible);
	}

	public Map<CatalogItem, Operation> getItemToOperation() {
		if (getViewer() == null) {
			return Collections.emptyMap();
		}
		return getViewer().getItemToOperation();
	}

	@Override
	public void setPageComplete(boolean complete) {
		if (complete) {
			complete = computeCanCompleteProvisioningOperation();
		}
		computeMessage();
		super.setPageComplete(complete);
	}

	private void computeMessage() {
		String message = null;
		int messageType = IMessageProvider.NONE;

		Map<CatalogItem, Operation> itemToOperation = getItemToOperation();
		if (!itemToOperation.isEmpty()) {
			Map<Operation, List<CatalogItem>> operationToItem = computeOperationToItem();

			if (operationToItem.size() == 1) {
				Entry<Operation, List<CatalogItem>> entry = operationToItem.entrySet().iterator().next();
				message = NLS.bind("{0} selected for {1}", entry.getValue().size() == 1 ? "one solution" : NLS.bind(
						"{0} solutions", entry.getValue().size()), entry.getKey().getLabel());
				messageType = IMessageProvider.INFORMATION;
			} else if (operationToItem.size() == 2 && operationToItem.containsKey(Operation.INSTALL)
					&& operationToItem.containsKey(Operation.CHECK_FOR_UPDATES)) {
				int count = 0;
				for (List<CatalogItem> items : operationToItem.values()) {
					count += items.size();
				}
				message = NLS.bind("{0} solutions selected for install or update", count);
				messageType = IMessageProvider.INFORMATION;
			} else if (operationToItem.size() > 1) {
				message = "Cannot install/update and remove solutions concurrently";
				messageType = IMessageProvider.ERROR;
			}
		}

		setMessage(message, messageType);
	}

	private Map<Operation, List<CatalogItem>> computeOperationToItem() {
		Map<CatalogItem, Operation> itemToOperation = getItemToOperation();
		Map<Operation, List<CatalogItem>> catalogItemByOperation = new HashMap<Operation, List<CatalogItem>>();
		for (Map.Entry<CatalogItem, Operation> entry : itemToOperation.entrySet()) {
			if (entry.getValue() == Operation.NONE) {
				continue;
			}
			List<CatalogItem> list = catalogItemByOperation.get(entry.getValue());
			if (list == null) {
				list = new ArrayList<CatalogItem>();
				catalogItemByOperation.put(entry.getValue(), list);
			}
			list.add(entry.getKey());
		}
		return catalogItemByOperation;
	}

	public boolean computeCanCompleteProvisioningOperation() {
		boolean complete = true;
		// can only perform one kind of operation (update, install or uninstall)
		Map<CatalogItem, Operation> itemToOperation = getItemToOperation();
		if (itemToOperation.isEmpty()) {
			complete = false;
		} else {
			Map<Operation, List<CatalogItem>> operationToItem = computeOperationToItem();
			if (operationToItem.isEmpty()) {
				complete = false;
			} else {
				if (operationToItem.size() > 1) {
					if (!(operationToItem.size() == 2 && operationToItem.containsKey(Operation.INSTALL) && operationToItem.containsKey(Operation.CHECK_FOR_UPDATES))) {
						complete = false;
					}
				}
			}
		}
		return complete;
	}
}
