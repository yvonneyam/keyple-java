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
package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;

/**
 * This class provides the dedicated constructor to build the Select File APDU commands.
 *
 */
public final class SelectFileCmdBuild extends PoCommandBuilder {

    private static final CalypsoPoCommands command = CalypsoPoCommands.SELECT_FILE;

    public enum SelectControl {
        MF, PATH_FROM_MF, PATH_FROM_CURRENT_DF
    }

    public enum SelectOptions {
        FCI, FCP
    }

    /**
     * Instantiates a new SelectFileCmdBuild.
     *
     * @param revision the PO revision
     */
    public SelectFileCmdBuild(PoRevision revision, SelectControl selectControl,
            SelectOptions selectOptions, byte[] selectData) {
        super(command, null);
        byte cla = PoRevision.REV2_4.equals(revision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = 0;
        switch (selectControl) {
            case MF:
                p1 = (byte) 0x00;
                break;
            case PATH_FROM_MF:
                p1 = (byte) 0x08;
            case PATH_FROM_CURRENT_DF:
                p1 = (byte) 0x02;
        }
        byte p2 = 0;
        switch (selectOptions) {
            case FCI:
                break;
            case FCP:
                p2 |= (byte) 0x04;
                break;
        }

        request = setApduRequest(cla, command, p1, p2, selectData, (byte) 0x00);
    }
}
