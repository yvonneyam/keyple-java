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
package org.eclipse.keyple.example.calypso.pc;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.example.calypso.common.transaction.CalypsoClassicTransactionEngine;
import org.eclipse.keyple.example.calypso.pc.stub.se.*;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Stub {

    public class StubPluginObserver implements ObservablePlugin.PluginObserver {
        /**
         * Method invoked in the case of a plugin event
         * 
         * @param event
         */
        @Override
        public void update(PluginEvent event) {}
    }

    /**
     * main program entry
     *
     * @param args the program arguments
     * @throws InterruptedException thread exception
     */
    public static void main(String[] args) throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Stub.class);

        /* Instantiate a PluginObserver to handle the stub reader insertion */
        UseCase_Calypso1_ExplicitSelectionAid_Stub.StubPluginObserver m =
                new UseCase_Calypso1_ExplicitSelectionAid_Stub.StubPluginObserver();

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();

        StubPlugin stubPlugin = StubPlugin.getInstance();

        /* Get the instance of the PcscPlugin (Singleton pattern) */
        pluginsSet.add(stubPlugin);

        /* Assign StubPlugin to the SeProxyService */
        seProxyService.setPlugins(pluginsSet);

        /* Setting up the transaction engine (implements Observer) */
        CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

        /*
         * Add a class observer to start the monitoring thread needed to handle the reader insertion
         */
        ((ObservablePlugin) stubPlugin).addObserver(m);

        /*
         * Plug PO and SAM stub readers.
         */
        stubPlugin.plugStubReader("poReader");
        stubPlugin.plugStubReader("samReader");

        Thread.sleep(200);

        StubReader poReader = null, samReader = null;
        try {
            poReader = (StubReader) (stubPlugin.getReader("poReader"));
            samReader = (StubReader) (stubPlugin.getReader("samReader"));
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

        /* Both readers are expected not null */
        if (poReader == samReader || poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("SAM Reader  NAME = {}", samReader.getName());

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Assign readers to the Hoplink transaction engine */
        transactionEngine.setReaders(poReader, samReader);

        /* Create 'virtual' Hoplink and SAM SE */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();
        StubSecureElement samSE = new StubSamCalypsoClassic();

        /* Insert the SAM into the SAM reader */
        logger.info("Insert stub SAM SE.");
        samReader.insertSe(samSE);

        /* Set the transactionEngine as Observer of the PO reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait a little the time the thread starts. */
        Thread.sleep(10);

        logger.info("Insert stub PO SE.");
        poReader.insertSe(calypsoStubSe);

        /* Wait a while the time that the transaction ends. */
        Thread.sleep(1000);

        /* Remove SE */
        logger.info("Remove stub SAM and PO SE.");

        poReader.removeSe();
        samReader.removeSe();

        logger.info("END.");

        System.exit(0);
    }
}
