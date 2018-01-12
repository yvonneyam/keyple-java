package org.keyple.commands.calypso.po.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.po.parser.GetDataFciRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class GetDataRespParsTest {

    @Test
    public void digestInitRespPars() {
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(new byte[] { 90, 00 }, true, new byte[] { 90, 00 });
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser = new GetDataFciRespPars(seResponse.getApduResponses().get(0));

        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(new byte[] { 90, 00 }, reponseActual);
    }
}
