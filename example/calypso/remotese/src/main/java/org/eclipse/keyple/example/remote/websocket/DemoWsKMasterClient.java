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

import org.eclipse.keyple.example.remote.calypso.DemoThreads;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;

/**
 * Demo websocket The master device uses the websocket client whereas the slave device uses the
 * websocket server
 */
public class DemoWsKMasterClient {

    public static void main(String[] args) throws Exception {

        // Create the procotol factory
        TransportFactory factory = new WskFactory(false); // Web socket

        // Launch the server thread
        DemoThreads.startServer(false, factory);

        Thread.sleep(1000);

        // Launch the client thread
        DemoThreads.startClient(true, factory);
    }
}
