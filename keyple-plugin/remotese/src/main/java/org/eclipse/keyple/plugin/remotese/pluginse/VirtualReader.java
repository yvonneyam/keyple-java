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
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.Map;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteReaderException;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodTxEngine;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.transaction.SelectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Reader Behaves like the Remote Reader it emulates
 */
public final class VirtualReader extends AbstractObservableReader {

    private final VirtualReaderSession session;
    private final String remoteName;
    private final RemoteMethodTxEngine rmTxEngine;

    private static final Logger logger = LoggerFactory.getLogger(VirtualReader.class);

    /**
     * Called by {@link RemoteSePlugin} Creates a new virtual reader
     * 
     * @param session Reader Session that helps communicate with
     *        {@link org.eclipse.keyple.plugin.remotese.transport.TransportNode}
     * @param nativeReaderName local name of the native reader on slave side
     */
    VirtualReader(VirtualReaderSession session, String nativeReaderName,
            RemoteMethodTxEngine rmTxEngine) {
        super(RemoteSePlugin.PLUGIN_NAME, "remote-" + nativeReaderName);
        this.session = session;
        this.remoteName = nativeReaderName;
        this.rmTxEngine = rmTxEngine;
    }

    /**
     * TODO change this to handle the right transmission mode
     *
     * @return the current transmission mode
     */
    public TransmissionMode getTransmissionMode() {
        logger.error("getTransmissionMode is not implemented yet");
        return null;
    }

    /**
     * Name of the Native Reader
     *
     * @return local name of the native reader (on slave device)
     */
    public String getNativeReaderName() {
        return remoteName;
    }

    public VirtualReaderSession getSession() {
        return session;
    }

    public RemoteMethodTxEngine getRmTxEngine() {
        return rmTxEngine;
    }


    @Override
    public boolean isSePresent() {
        logger.error("isSePresent is not implemented yet");
        return false;// not implemented
    }

    /**
     * Blocking TransmitSet
     * 
     * @param seRequestSet : SeRequestSet to be transmitted
     * @return seResponseSet : SeResponseSet
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    public SeResponseSet processSeRequestSet(SeRequestSet seRequestSet)
            throws IllegalArgumentException, KeypleReaderException {

        RmTransmitTx transmit = new RmTransmitTx(seRequestSet, session.getSessionId(),
                this.getNativeReaderName(), this.getName(), null);
        try {
            rmTxEngine.register(transmit);
            return transmit.get();
        } catch (KeypleRemoteException e) {
            e.printStackTrace();
            throw (KeypleReaderException) e.getCause();
        }
    }

    /**
     * Blocking Transmit
     * 
     * @param seRequest
     * @return seResponse
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    public SeResponse processSeRequest(SeRequest seRequest)
            throws IllegalArgumentException, KeypleReaderException {
        try {
            return this.processSeRequestSet(new SeRequestSet(seRequest)).getSingleResponse();
        } catch (KeypleRemoteReaderException e) {
            // throw the cause of the RemoteReaderException (a KeypleReaderException)
            throw (KeypleReaderException) e.getCause();
        }
    }


    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        logger.error("addSeProtocolSetting is not implemented yet");

    }

    /*
     * PACKAGE PRIVATE
     */

    /**
     * When an event occurs on the Remote LocalReader, notify Observers
     * 
     * @param event
     */
    void onRemoteReaderEvent(final ReaderEvent event) {
        final VirtualReader thisReader = this;

        logger.debug(" EVENT {} ", event.getEventType());
        // notify observers in a separate thread
        new Thread() {
            public void run() {
                if (thisReader.countObservers() > 0) {
                    thisReader.notifyObservers(event);
                } else {
                    logger.debug(
                            "An event was received but no observers are declared into VirtualReader : {} {}",
                            thisReader.getName(), event.getEventType());
                }
            }
        }.start();

    }


    /**
     *
     * HELPERS
     */


    @Override
    public Map<String, String> getParameters() {
        logger.error("getParameters is not implemented yet");
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {
        logger.error("setParameter is not implemented yet");
    }

    @Override
    public void setDefaultSelectionRequest(SelectionRequest selectionRequest,
            NotificationMode notificationMode) {
        logger.error("setDefaultSelectionRequest is not implemented yet");
    }


}
