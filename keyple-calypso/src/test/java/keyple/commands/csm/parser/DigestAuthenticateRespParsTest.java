/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.parser.DigestAuthenticateRespPars;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class DigestAuthenticateRespParsTest {

    @Test
    public void digestAuthenticateResp() {

        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(new byte[] {90, 00}, true);
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser =
                new DigestAuthenticateRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer reponseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(ByteBuffer.wrap(new byte[] {90, 00}), reponseActual);
    }
}