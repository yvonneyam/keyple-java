/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

public class OpenSession10RespPars extends AbstractOpenSessionRespPars {

    public OpenSession10RespPars(ApduResponse response) {
        super(response, PoRevision.REV1_0);
    }

    @Override
    SecureSession toSecureSession(ByteBuffer apduResponseData) {
        return createSecureSession(apduResponseData);
    }

    public static SecureSession createSecureSession(ByteBuffer apduResponseData) {
        boolean previousSessionRatified = true;

        /**
         * In rev 1.0 mode, the response to the Open Secure Session command is as follows:
         * <p>
         * <code>CC CC CC CC CC [RR RR] [NN..NN]</code>
         * <p>
         * Where:
         * <ul>
         * <li><code>CC CC CC CC</code> = PO challenge</li>
         * <li><code>RR RR</code> = ratification bytes (may be absent)</li>
         * <li><code>NN..NN</code> = record data (29 bytes)</li>
         * </ul>
         *
         */
        if (apduResponseData.limit() == 4 || apduResponseData.limit() == 33) {
            previousSessionRatified = false;
        } else {
            previousSessionRatified = true;
        }

        /* KVC doesn't exist and is set to null for this type of PO */
        return new SecureSession(ByteBufferUtils.subIndex(apduResponseData, 1, 4),
                ByteBufferUtils.subIndex(apduResponseData, 4, 5), previousSessionRatified, false,
                null, null, apduResponseData);
    }
}
