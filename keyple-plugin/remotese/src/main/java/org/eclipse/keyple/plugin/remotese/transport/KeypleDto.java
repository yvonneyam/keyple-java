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
package org.eclipse.keyple.plugin.remotese.transport;

/**
 * Data Transfer Object used to common an API call from a Master Reader to a Slave Reader (and vice
 * versa)
 */
public class KeypleDto {

    // Slave Node Id
    private String clientNodeId;

    // Master reader session
    private String sessionId;

    // Slave reader name
    private String nativeReaderName;

    // Master reader name
    private String virtualReaderName;

    // API method to be called
    private final String action;

    // Arguments of the API (json)
    private final String body;

    // Is a request or a response
    private final Boolean isRequest;


    /**
     * Basic Constructor
     * 
     * @param action : API method to be called
     * @param body : Arguments of the API (json)
     * @param isRequest : Is a request or a response
     */
    public KeypleDto(String action, String body, Boolean isRequest) {
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }

    /**
     * Constructor with a Virtual Reader Session Id
     *
     * @param action : API method to be called
     * @param body : Arguments of the API (json)
     * @param isRequest : Is a request or a response
     * @param sessionId : Session Id of current Virtual Reader Session Id
     */
    public KeypleDto(String action, String body, Boolean isRequest, String sessionId) {
        this.sessionId = sessionId;
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }

    /**
     * Constructor with a Virtual Reader Session Id
     *
     * @param action : API method to be called
     * @param body : Arguments of the API (json)
     * @param isRequest : Is a request or a response
     * @param sessionId : Session Id of current Virtual Reader Session Id
     */
    public KeypleDto(String action, String body, Boolean isRequest, String sessionId,
            String nativeReaderName, String virtualReaderName, String clientNodeId) {
        this.sessionId = sessionId;
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
        this.nativeReaderName = nativeReaderName;
        this.virtualReaderName = virtualReaderName;
        this.clientNodeId = clientNodeId;
    }

    /*
     * Getters and Setters
     */

    public Boolean isRequest() {
        return isRequest;
    }

    public String getAction() {
        return action;
    }

    public String getBody() {
        return body;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getNodeId() {
        return clientNodeId;
    }

    public String getNativeReaderName() {
        return nativeReaderName;
    }

    public String getVirtualReaderName() {
        return virtualReaderName;
    }

    @Override
    public String toString() {
        return String.format(
                "KeypleDto : %s - isRequest : %s - native : %s - virtual : %s - clientNodeId : %s - sessionId : %s - body : %s",
                this.getAction(), this.isRequest(), this.getNativeReaderName(),
                this.getVirtualReaderName(), this.getNodeId(), this.getSessionId(), this.getBody());
    }
}
