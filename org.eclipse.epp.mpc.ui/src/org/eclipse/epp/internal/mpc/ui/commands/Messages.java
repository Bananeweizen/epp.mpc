/*******************************************************************************
 * Copyright (c) 2010 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.internal.mpc.ui.commands;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.epp.internal.mpc.ui.commands.messages"; //$NON-NLS-1$

	public static String MarketplaceWizardCommand_allCategories;

	public static String MarketplaceWizardCommand_allMarkets;

	public static String MarketplaceWizardCommand_CannotInstallRemoteLocations;

	public static String MarketplaceWizardCommand_CouldNotFindMarketplaceForSolution;

	public static String MarketplaceWizardCommand_eclipseMarketplace;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
