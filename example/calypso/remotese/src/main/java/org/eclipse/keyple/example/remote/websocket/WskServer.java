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
package org.eclipse.keyple.example.remote.websocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web socket server
 */
class WskServer extends WebSocketServer implements ServerNode {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private DtoHandler dtoHandler;

    // only for when server is master
    private Boolean isMaster;
    private WebSocket masterWebSocket;
    final private String nodeId;

    public WskServer(InetSocketAddress address, Boolean isMaster, String nodeId) {
        super(address);

        logger.info("Create websocket server on address {}", address.toString());
        this.nodeId = nodeId;
        this.isMaster = isMaster;
    }

    /*
     * WebSocketServer
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("Web socket onOpen {} {}", conn, handshake);
        masterWebSocket = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {} {} {}", conn, code, reason, remote);
    }

    /**
     * Incoming message
     * 
     * @param conn : websocket connection used
     * @param message : incoming message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.trace("Web socket onMessage {} {}", conn, message);
        KeypleDto keypleDto = KeypleDtoHelper.fromJson(message);

        if (dtoHandler != null) {

            // LOOP pass DTO and get DTO Response is any
            TransportDto transportDto =
                    dtoHandler.onDTO(new WskTransportDTO(keypleDto, conn, this));

            if (isMaster) {
                // if server is master, can have numerous clients
                if (transportDto.getKeypleDTO().getSessionId() != null) {
                    sessionId_Connection.put(transportDto.getKeypleDTO().getSessionId(), conn);
                } else {
                    logger.debug("No session defined in message {}", transportDto);
                }
            }

            this.sendDTO(transportDto);
        } else {
            throw new IllegalStateException(
                    "Received a message but no DtoHandler is defined to process the message");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.debug("Web socket onError {} {}", conn, ex);

    }

    @Override
    public void onStart() {
        logger.info("Web socket server started");
    }


    /*
     * TransportNode
     */

    final private Map<String, Object> sessionId_Connection = new HashMap<String, Object>();

    private Object getConnection(String sessionId) {
        return sessionId_Connection.get(sessionId);
    }

    public void setDtoHandler(DtoHandler stubplugin) {
        this.dtoHandler = stubplugin;
    }


    @Override
    public void sendDTO(TransportDto transportDto) {
        logger.trace("sendDTO {} {}", KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));

        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {

            if (!isMaster) {
                // if server is client -> use the master web socket
                masterWebSocket.send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
            } else {
                // server is master, can have numerous slave clients
                if (((WskTransportDTO) transportDto).getSocketWeb() != null) {
                    logger.trace("Use socketweb included in TransportDto");
                    ((WskTransportDTO) transportDto).getSocketWeb()
                            .send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
                } else {
                    // if there is no socketweb defined in the transport dto
                    // retrieve the socketweb by the sessionId
                    if (transportDto.getKeypleDTO().getSessionId() == null) {
                        logger.warn("No sessionId defined in message, Keyple DTO can not be sent");
                    } else {
                        logger.trace("Retrieve socketweb from sessionId");
                        // retrieve connection object from the sessionId
                        Object conn = getConnection(transportDto.getKeypleDTO().getSessionId());
                        logger.trace("send DTO {} {}",
                                KeypleDtoHelper.toJson(transportDto.getKeypleDTO()), conn);
                        ((WebSocket) conn)
                                .send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
                    }
                }

            }
        }
    }

    /*
     * DTO Sender
     */
    @Override
    public void sendDTO(KeypleDto message) {
        logger.trace("Web socket sendDTO without predefined socket {}",
                KeypleDtoHelper.toJson(message));
        this.sendDTO(new WskTransportDTO(message, null));
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }


    @Override
    public void update(KeypleDto event) {
        sendDTO(event);
    }
}
