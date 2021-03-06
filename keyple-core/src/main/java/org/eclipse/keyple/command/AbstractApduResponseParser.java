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
package org.eclipse.keyple.command;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.message.ApduResponse;

/**
 * Base class for parsing APDU
 */
public abstract class AbstractApduResponseParser {

    /** the byte array APDU response. */
    protected ApduResponse response;

    protected static final Map<Integer, StatusProperties> STATUS_TABLE;
    static {
        HashMap<Integer, StatusProperties> m = new HashMap<Integer, StatusProperties>();
        m.put(0x9000, new StatusProperties(true, "Success"));
        STATUS_TABLE = m;
    }

    /** Indicates whether the ApduResponse has been provided or not */
    private boolean initialized;

    // Note: The conversion of all commands was done with:
    // Input regex: new byte\[\] \{\(byte\) 0x([0-9A-Za-z]{2})\, \(byte\) 0x([0-9A-Za-z]{2})\}
    // Output regex: 0x$1$2

    /**
     * Get the internal status table
     * 
     * @return Status table
     */
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response response to parse
     */
    @Deprecated
    public AbstractApduResponseParser(ApduResponse response) {
        this.response = response;
        initialized = true;
    }

    /**
     * Default constructor
     */
    public AbstractApduResponseParser() {
        initialized = false;
    }

    /**
     * Sets the Apdu response to parse
     * 
     * @param response the apdu response
     */
    public final void setApduResponse(ApduResponse response) {
        this.response = response;
        initialized = true;
    }

    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the apdu response.
     *
     * @return the ApduResponse instance.
     */
    public final ApduResponse getApduResponse() {
        if (!initialized) {
            throw new IllegalStateException("The parser has not been initialized.");
        }
        return response;
    }

    private int getStatusCode() {
        return response.getStatusCode();
    }

    private StatusProperties getPropertiesForStatusCode() {
        return getStatusTable().get(getStatusCode());
    }

    /**
     * Checks if is successful.
     *
     * @return if the status is successful from the statusTable according to the current status
     *         code.
     */
    public boolean isSuccessful() {
        if (!initialized) {
            throw new IllegalStateException("The parser has not been initialized.");
        }
        StatusProperties props = getPropertiesForStatusCode();
        return props != null && props.isSuccessful();
    }

    /**
     * Gets the status information.
     *
     * @return the ASCII message from the statusTable for the current status code.
     */
    public final String getStatusInformation() {
        StatusProperties props = getPropertiesForStatusCode();
        return props != null ? props.getInformation() : null;
    }


    /**
     * Status code properties
     */
    protected static class StatusProperties {

        /** The successful. */
        private final boolean successful;

        /** The information. */
        private final String information;

        /**
         * A map with the double byte of a status as key, and the successful property and ASCII text
         * information as data.
         *
         * @param successful set successful status
         * @param information additional information
         */
        public StatusProperties(boolean successful, String information) {
            this.successful = successful;
            this.information = information;
        }

        /**
         * Gets the successful.
         *
         * @return the successful
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * Gets the information.
         *
         * @return the information
         */
        String getInformation() {
            return information;
        }

    }
}
