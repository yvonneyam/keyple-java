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
package org.eclipse.keyple.plugin.remotese.common.json;


import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;

public class SampleFactory {

    public static KeypleBaseException getAStackedKeypleException() {
        return new KeypleReaderException("Keyple Reader Exception", new IOException("IO Error",
                new IOException("IO Error2", new RuntimeException("sdfsdf"))));
    }

    public static KeypleBaseException getASimpleKeypleException() {
        return new KeypleReaderException("Keyple Reader Exception");
    }


    public static SeRequestSet getASeRequestSet_ISO14443_4() {
        String poAid = "A000000291A000000191";

        List<ApduRequest> poApduRequestList;
        poApduRequestList = Arrays.asList(new ApduRequest(ByteArrayUtils.fromHex("9000"), true));

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(selector, poApduRequestList, ChannelState.CLOSE_AFTER,
                ContactlessProtocols.PROTOCOL_ISO14443_4, null);

        return new SeRequestSet(seRequest);

    }


    public static SeRequestSet getASeRequestSet() {
        String poAid = "A000000291A000000191";

        List<ApduRequest> poApduRequestList;
        poApduRequestList = Arrays.asList(new ApduRequest(ByteArrayUtils.fromHex("9000"), true));

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(poApduRequestList, ChannelState.CLOSE_AFTER);

        return new SeRequestSet(seRequest);

    }

    public static SeRequest getASeRequest_ISO14443_4() {
        String poAid = "A000000291A000000191";

        List<ApduRequest> poApduRequestList;
        poApduRequestList = Arrays.asList(new ApduRequest(ByteArrayUtils.fromHex("9000"), true));

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(selector, poApduRequestList, ChannelState.CLOSE_AFTER,
                ContactlessProtocols.PROTOCOL_ISO14443_4, null);
        return seRequest;

    }

    public static SeRequest getASeRequest() {
        String poAid = "A000000291A000000191";

        List<ApduRequest> poApduRequestList;
        poApduRequestList = Arrays.asList(new ApduRequest(ByteArrayUtils.fromHex("9000"), true));

        SeRequest.Selector selector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));

        SeRequest seRequest = new SeRequest(poApduRequestList, ChannelState.CLOSE_AFTER);
        return seRequest;

    }

    public static SeRequestSet getCompleteRequestSet() {
        String poAid = "A000000291A000000191";



        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(new ApduRequest(ByteArrayUtils.fromHex("9000"), true));

        SeRequest.Selector aidSelector = new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid));
        SeRequest.Selector atrSelector = new SeRequest.AtrSelector("/regex/");

        SeRequest seRequest = new SeRequest(aidSelector, poApduRequestList,
                ChannelState.CLOSE_AFTER, ContactlessProtocols.PROTOCOL_ISO14443_4, null);

        SeRequest seRequest2 = new SeRequest(atrSelector, poApduRequestList, ChannelState.KEEP_OPEN,
                ContactsProtocols.PROTOCOL_ISO7816_3, null);

        Set<SeRequest> seRequests = new HashSet<SeRequest>();
        seRequests.add(seRequest);
        seRequests.add(seRequest2);

        return new SeRequestSet(seRequests);


    }

    public static SeResponseSet getCompleteResponseSet() {
        List<SeResponse> seResponses = new ArrayList<SeResponse>();

        ApduResponse apdu =
                new ApduResponse(ByteArrayUtils.fromHex("9000"), new HashSet<Integer>());
        ApduResponse apdu2 =
                new ApduResponse(ByteArrayUtils.fromHex("9000"), new HashSet<Integer>());

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(apdu);
        apduResponses.add(apdu2);

        seResponses.add(new SeResponse(true, new SelectionStatus(null, apdu, true), apduResponses));
        seResponses.add(new SeResponse(true, new SelectionStatus(null, apdu, true), apduResponses));

        return new SeResponseSet(seResponses);


    }



}
