/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.seproxy.exception;

/**
 * Exception thrown when Channel Operations (open/close) failed in a
 * {@link org.eclipse.keyple.seproxy.ProxyReader}
 */
public class KeypleChannelStateException extends KeypleReaderException {

    /**
     * New exception to be thrown
     *
     * @param message : message to identify the exception and the context
     */
    public KeypleChannelStateException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level reader exception
     *
     * @param message : message to add some context to the exception
     * @param cause : lower level exception
     */
    public KeypleChannelStateException(String message, Throwable cause) {
        super(message, cause);
    }
}