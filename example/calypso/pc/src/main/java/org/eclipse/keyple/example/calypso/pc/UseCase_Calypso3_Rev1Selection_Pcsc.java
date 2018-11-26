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
import java.io.IOException;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 3’ – Rev1 Selection Atr (PC/SC)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Check if a B' protocol SE is in the reader, select a Calypso PO Rev1 (ATR selection), select
 * the DF RT (ticketing), operate a simple Calypso PO transaction (simple plain read, not involving
 * a Calypso SAM).</li>
 * <li><code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>PO messages:
 * <ul>
 * <li>A first SE message to do an ATR based selection and DF selection of the SE in the reader</li>
 * <li>A second SE message to operate the simple Calypso transaction</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Calypso3_Rev1Selection_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso3_Rev1Selection_Pcsc.class);
    private static String poAtrRegex = ".*";
    private static String poDfRtPath = "2000";


    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
         * CalypsoUtilities class.
         */
        ProxyReader poReader = CalypsoUtilities.getDefaultPoReader(seProxyService);

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info(
                "=============== UseCase Calypso #1: ATR based explicit selection (PO Rev1) ===========");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= 1st PO exchange: ATR based selection with reading of Environment file.         =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection(poReader);

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            PoSelector poSelector = new PoSelector(poAtrRegex, ChannelState.KEEP_OPEN, Protocol.ANY,
                    PoSelector.RevisionTarget.TARGET_REV1, "ATR: " + poAtrRegex);

            /*
             * Prepare the selection of the DF RT.
             */
            SelectFileRespPars selectFileRespPars = poSelector.prepareSelectFileDfCmd(
                    ByteArrayUtils.fromHex(poDfRtPath), "Select file: " + poDfRtPath);

            /*
             * Prepare the reading order and keep the associated parser for later use once the
             * selection has been made.
             */
            ReadRecordsRespPars readEnvironmentParser = poSelector.prepareReadRecordsCmd(
                    SFI_EnvironmentAndHolder, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                    (byte) 0x00,
                    String.format("EnvironmentAndHolder (SFI=%02X))", SFI_EnvironmentAndHolder));

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            CalypsoPo calypsoPo = (CalypsoPo) seSelection.prepareSelection(poSelector);

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */
            if (seSelection.processExplicitSelection()) {
                logger.info("The selection of the PO has succeeded.");

                logger.info("DF RT FCI: {}",
                        ByteArrayUtils.toHex(selectFileRespPars.getSelectionData()));

                /* Retrieve the data read from the parser updated during the selection process */
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

                PoTransaction poTransaction = new PoTransaction(poReader, calypsoPo);

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                ReadRecordsRespPars readEventLogParser = poTransaction.prepareReadRecordsCmd(
                        SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                        (byte) 0x00, String.format("EventLog (SFI=%02X, recnbr=%d))", SFI_EventLog,
                                RECORD_NUMBER_1));

                /*
                 * Actual PO communication: send the prepared read order, then close the channel
                 * with the PO
                 */
                if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                    logger.info("The reading of the EventLog has succeeded.");

                    /*
                     * Retrieve the data read from the parser updated during the transaction process
                     */
                    byte eventLog[] = (readEventLogParser.getRecords()).get((int) RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));
                }
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the Calypso PO processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
