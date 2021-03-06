/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.transaction;

import org.eclipse.keyple.seproxy.message.SeResponseSet;

/**
 * Class containing the {@link SeResponseSet} used from a default selection made at the
 * {@link org.eclipse.keyple.seproxy.event.ObservableReader} level.
 */
public class SelectionResponse {
    /** The {@link SeResponseSet} */
    private final SeResponseSet selectionSeResponseSet;

    public SelectionResponse(SeResponseSet selectionSeResponseSet) {
        this.selectionSeResponseSet = selectionSeResponseSet;
    }

    public SeResponseSet getSelectionSeResponseSet() {
        return selectionSeResponseSet;
    }
}
