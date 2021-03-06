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
package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmTransmitTx extends RemoteMethodTx<SeResponseSet> {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitTx.class);

    private final SeRequestSet seRequestSet;
    private final String sessionId;
    private final String nativeReaderName;
    private final String virtualReaderName;
    private final String clientNodeId;


    public RmTransmitTx(SeRequestSet seRequestSet, String sessionId, String nativeReaderName,
            String virtualReaderName, String clientNodeId) {
        this.seRequestSet = seRequestSet;
        this.sessionId = sessionId;
        this.nativeReaderName = nativeReaderName;
        this.virtualReaderName = virtualReaderName;
        this.clientNodeId = clientNodeId;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                JsonParser.getGson().toJson(seRequestSet, SeRequestSet.class), true, this.sessionId,
                this.nativeReaderName, this.virtualReaderName, this.clientNodeId);
    }


    @Override
    public SeResponseSet parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {

        logger.trace("KeypleDto : {}", keypleDto);
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponseSet.class);
        }
    }


}
