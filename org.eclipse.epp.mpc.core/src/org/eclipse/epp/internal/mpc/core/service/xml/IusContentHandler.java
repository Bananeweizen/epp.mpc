/*******************************************************************************
 * Copyright (c) 2010 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      The Eclipse Foundation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.internal.mpc.core.service.xml;

import org.eclipse.epp.internal.mpc.core.service.Ius;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author David Green
 */
public class IusContentHandler extends UnmarshalContentHandler {

	private Ius model;

	@Override
	public void startElement(String uri, String localName, Attributes attributes) {
		if (localName.equals("ius")) { //$NON-NLS-1$
			model = new Ius();

		} else if (localName.equals("iu")) { //$NON-NLS-1$
			capturingContent = true;
		}
	}

	@Override
	public boolean endElement(String uri, String localName) throws SAXException {
		if (localName.equals("ius")) { //$NON-NLS-1$
			if (parentModel instanceof org.eclipse.epp.internal.mpc.core.service.Node) {
				((org.eclipse.epp.internal.mpc.core.service.Node) parentModel).setIus(model);
			}
			getUnmarshaller().setModel(model);
			model = null;
			getUnmarshaller().setCurrentHandler(parentHandler);
			if (parentHandler != null) {
				parentHandler.endElement(uri, localName);
			}
			return true;
		} else if (localName.equals("iu")) { //$NON-NLS-1$
			if (content != null) {
				model.getIu().add(content.toString());
				content = null;
			}
			capturingContent = false;
		}
		return false;
	}

}
