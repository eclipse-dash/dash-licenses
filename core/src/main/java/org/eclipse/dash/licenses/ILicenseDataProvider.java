/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.Collection;
import java.util.function.Consumer;

public interface ILicenseDataProvider {

	void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer);
	
	default int getWeight() {
		return 50;
	}
}
