/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;


import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.NameableConfigurable;

/**
 * ProxyReader interface
 * <ul>
 * <li>To operate the transmission of SeRequestSet, a specific local reader processes the sorted
 * list of SeRequest.</li>
 * <li>According to SeRequest protocolFlag and to the current status of the reader (RF protocol
 * involved / current ATR) the processing of a specific SeRequest could be skipped.</li>
 * <li>When processing a SeRequest</li>
 * <ul>
 * <li>if necessary a new logical channel is open (for a specific AID if defined)</li>
 * <li>and ApduRequest are transmited one by one</li>
 * </ul>
 * </ul>
 * Interface each {@link ReaderPlugin} should implement
 */
public interface ProxyReader extends NameableConfigurable, Comparable<ProxyReader> {

    /**
     * Gets the name.
     *
     * @return returns the ‘unique’ name of the SE reader for the selected plugin.
     */
    String getName();

    /**
     * Checks if is SE present.
     *
     * @return true if a Secure Element is present in the reader
     * @throws IOReaderException Exception of type IO Reader
     */
    boolean isSePresent() throws NoStackTraceThrowable;

    /**
     * Transmits a request to a SE application and get back the corresponding SE response o the
     * usage of this method is conditioned to the presence of a SE in the selected reader, this
     * method could also fail in case of IO error or wrong card state → some reader’s exception (SE
     * missing, IO error, wrong card state, timeout) have to be caught during the processing of the
     * SE request transmission.
     *
     * @param seApplicationRequest the se application request
     * @return the SE response
     * @throws IOReaderException Exception of type IO Reader
     */
    SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException;

    void addSeProtocolSetting(SeProtocolSetting seProtocolSetting);
}