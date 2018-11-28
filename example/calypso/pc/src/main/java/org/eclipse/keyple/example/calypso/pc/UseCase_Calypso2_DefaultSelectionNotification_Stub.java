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


import static org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo.*;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.pc.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.ObservablePlugin.PluginObserver;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 2’ – Default Selection Notification (Stub)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Define a default selection of ISO 14443-4 Calypso PO and set it to an observable reader, on
 * SE detection in case the Calypso selection is successful, notify the terminal application with
 * the PO information, then the terminal follows by operating a simple Calypso PO transaction.</li>
 * <li><code>
 Default Selection Notification
 </code> means that the SE processing is automatically started when detected.</li>
 * <li>PO messages:
 * <ul>
 * <li>A first SE message to notify about the selected Calypso PO</li>
 * <li>A second SE message to operate the simple Calypso transaction</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Calypso2_DefaultSelectionNotification_Stub implements ReaderObserver {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso2_DefaultSelectionNotification_Stub.class);
    private StubReader poReader;
    private String poAid = "A0000004040125090101";
    private SeSelection seSelection;
    private ReadRecordsRespPars readEnvironmentParser;
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public class StubPluginObserver implements PluginObserver {
        /**
         * Method invoked in the case of a plugin event
         * 
         * @param event
         */

        @Override
        public void update(PluginEvent event) {
            logger.info("Event: {}", event.getEventType());
        }
    }

    public UseCase_Calypso2_DefaultSelectionNotification_Stub()
            throws KeypleBaseException, InterruptedException {

        /* Instantiate a PluginObserver to handle the stub reader insertion */
        StubPluginObserver m = new StubPluginObserver();

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the Stub plugin */
        StubPlugin stubPlugin = StubPlugin.getInstance();

        /* Assign StubPlugin to the SeProxyService */
        seProxyService.addPlugin(stubPlugin);

        /*
         * Add a class observer to start the monitoring thread needed to handle the reader insertion
         */
        ((ObservablePlugin) stubPlugin).addObserver(m);

        /* Plug the PO stub reader. */
        stubPlugin.plugStubReader("poReader");

        Thread.sleep(200);

        /*
         * Get a PO reader ready to work with Calypso PO.
         */
        poReader = (StubReader) (stubPlugin.getReader("poReader"));

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info(
                "=============== UseCase Calypso #2: AID based default selection ===================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        /*
         * Prepare a Calypso PO selection
         */
        seSelection = new SeSelection(poReader);

        /*
         * Setting of an AID based selection of a Calypso REV3 PO
         *
         * Select the first application matching the selection AID whatever the SE communication
         * protocol keep the logical channel open after the selection
         */

        /*
         * Calypso selection: configures a PoSelector with all the desired attributes to make the
         * selection and read additional information afterwards
         */
        PoSelector poSelector = new PoSelector(ByteArrayUtils.fromHex(poAid),
                SeSelector.SelectMode.FIRST, ChannelState.KEEP_OPEN, Protocol.ANY,
                PoSelector.RevisionTarget.TARGET_REV3, "AID: " + poAid);

        /*
         * Prepare the reading order and keep the associated parser for later use once the selection
         * has been made.
         */
        readEnvironmentParser = poSelector.prepareReadRecordsCmd(SFI_EnvironmentAndHolder,
                ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1, (byte) 0x00,
                String.format("EnvironmentAndHolder (SFI=%02X))", SFI_EnvironmentAndHolder));

        /*
         * Add the selection case to the current selection (we could have added other cases here)
         */
        seSelection.prepareSelection(poSelector);

        /*
         * Provide the SeReader with the selection operation to be processed when a PO is inserted.
         */
        ((ObservableReader) poReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        /* Set the current class as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(this);

        logger.info(
                "==================================================================================");
        logger.info(
                "= Wait for a PO. The default AID based selection with reading of Environment     =");
        logger.info(
                "= file is ready to be processed as soon as the PO is detected.                   =");
        logger.info(
                "==================================================================================");

        Thread.sleep(1000);

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        /* Wait a while. */
        Thread.sleep(100);

        logger.info("Insert stub PO.");
        poReader.insertSe(calypsoStubSe);

        /* Wait a while. */
        Thread.sleep(1000);

        logger.info("Remove stub PO.");
        poReader.removeSe();

        System.exit(0);
    }

    /**
     * Method invoked in the case of a reader event
     * 
     * @param event the reader event
     */
    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_MATCHED:
                if (seSelection.processDefaultSelection(event.getDefaultSelectionResponse())) {
                    MatchingSe selectedSe = seSelection.getSelectedSe();

                    logger.info("Observer notification: the selection of the PO has succeeded.");

                    /*
                     * Retrieve the data read from the parser updated during the selection process
                     */
                    byte environmentAndHolder[] =
                            (readEnvironmentParser.getRecords()).get((int) RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("Environment file data: {}",
                            ByteArrayUtils.toHex(environmentAndHolder));

                    /* Go on with the reading of the first record of the EventLog file */
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= 2nd PO exchange: reading transaction of the EventLog file.                     =");
                    logger.info(
                            "==================================================================================");

                    PoTransaction poTransaction =
                            new PoTransaction(poReader, (CalypsoPo) selectedSe);

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the transaction has been processed.
                     */
                    ReadRecordsRespPars readEventLogParser = poTransaction.prepareReadRecordsCmd(
                            SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                            (byte) 0x00, String.format("EventLog (SFI=%02X, recnbr=%d))",
                                    SFI_EventLog, RECORD_NUMBER_1));

                    /*
                     * Actual PO communication: send the prepared read order, then close the channel
                     * with the PO
                     */
                    try {
                        if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                            logger.info("The reading of the EventLog has succeeded.");

                            /*
                             * Retrieve the data read from the parser updated during the transaction
                             * process
                             */
                            byte eventLog[] =
                                    (readEventLogParser.getRecords()).get((int) RECORD_NUMBER_1);

                            /* Log the result */
                            logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));
                        }
                    } catch (KeypleReaderException e) {
                        e.printStackTrace();
                    }
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= End of the Calypso PO processing.                                              =");
                    logger.info(
                            "==================================================================================");
                } else {
                    logger.error(
                            "The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.");
                }
                break;
            case SE_INSERTED:
                logger.error(
                        "SE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.");
                break;
            case SE_REMOVAL:
                logger.info("The PO has been removed.");
                break;
            default:
                break;
        }
    }

    /**
     * main program entry
     */
    public static void main(String[] args) throws InterruptedException, KeypleBaseException {
        /* Create the observable object to handle the PO processing */
        UseCase_Calypso2_DefaultSelectionNotification_Stub m =
                new UseCase_Calypso2_DefaultSelectionNotification_Stub();
    }
}