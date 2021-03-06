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
package org.eclipse.keyple.plugin.remotese.integration;

import static org.mockito.Mockito.doAnswer;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalClient;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NativeReaderServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(NativeReaderServiceTest.class);

    // Real objects
    TransportFactory factory;
    ObservablePlugin.PluginObserver stubPluginObserver;
    VirtualReaderService virtualReaderService;

    StubReader nativeReader;
    VirtualReader virtualReader;

    final String NATIVE_READER_NAME = "testStubReader";
    final String CLIENT_NODE_ID = "testClientNodeId";


    // Spy Object
    NativeReaderServiceImpl nativeReaderSpy;



    @Before
    public void setTup() throws Exception {
        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server)
        // only one client and one server
        factory = new LocalTransportFactory();

        stubPluginObserver = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent pluginEvent) {
                logger.debug("Default Stub Plugin Observer : {}", pluginEvent);
            }

        };

        logger.info("*** Bind Master Services");
        // bind Master services to server
        virtualReaderService = Integration.bindMaster(factory.getServer());

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        nativeReaderSpy = Integration.bindSlaveSpy(factory.getClient());

        nativeReader = Integration.createStubReader(NATIVE_READER_NAME, stubPluginObserver);

    }


    @After
    public void tearDown() throws Exception {

        logger.info("TearDown Test");

        StubPlugin stubPlugin = StubPlugin.getInstance();


        // delete stubReader
        stubPlugin.unplugReader(nativeReader.getName());

        Thread.sleep(500);

        // delete observer and monitor thread
        stubPlugin.removeObserver(stubPluginObserver);

        nativeReader.clearObservers();

        Thread.sleep(500);
    }



    /*
     * CONNECT METHOD
     */


    /**
     * Connect successfully a reader
     * 
     * @throws Exception
     */
    @Test
    public void testOKConnect() throws Exception {

        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        // assert that a virtual reader has been created
        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin()
                .getReaderByRemoteName(NATIVE_READER_NAME);

        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());
        Assert.assertEquals(1, nativeReader.countObservers());
        Assert.assertEquals(0, virtualReader.countObservers());

    }

    /**
     * Connect error : reader already exists
     * 
     * @throws Exception
     */
    @Test
    public void testKOConnectError() throws Exception {

        // first connectReader is successful
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        // assert an exception will be contained into keypleDto response
        doAnswer(Integration.assertContainsException()).when(nativeReaderSpy)
                .onDTO(ArgumentMatchers.<TransportDto>any());

        // should throw a DTO with an exception in master side KeypleReaderException
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);
    }

    /**
     * Connect error : impossible to send DTO
     * 
     * @throws Exception
     */
    @Test(expected = KeypleRemoteException.class)
    public void testKOConnectServerError() throws Exception {

        // bind Slave to faulty client
        nativeReaderSpy = Integration.bindSlaveSpy(new LocalClient(null));

        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);
        // should throw a KeypleRemoteException in slave side
    }

    /*
     * DISCONNECT METHOD
     */

    /**
     * Disconnect successfully a reader
     * 
     * @throws Exception
     */
    @Test
    public void testOKConnectDisconnect() throws Exception {

        // connect
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin()
                .getReaderByRemoteName(NATIVE_READER_NAME);

        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

        // disconnect
        nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);

        // assert that the virtual reader has been destroyed
        Assert.assertEquals(0, virtualReaderService.getPlugin().getReaders().size());
    }


    /**
     * Disconnect Error : reader not connected
     * 
     * @throws Exception
     */
    // @Test
    // public void testKODisconnectNotFoundError() throws Exception {
    //
    // // assert an exception will be contained into keypleDto response
    // doAnswer(Integration.assertContainsException()).when(nativeReaderSpy)
    // .onDTO(ArgumentMatchers.<TransportDto>any());
    //
    // // disconnect
    // nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);
    // // should throw exception in master side KeypleNotFound
    //
    // }


    /**
     * Disconnect error : impossible to send DTO
     * 
     * @throws Exception
     */
    @Test(expected = KeypleRemoteException.class)
    public void testKODisconnectServerError() throws Exception {

        // bind Slave to faulty client
        nativeReaderSpy = Integration.bindSlaveSpy(new LocalClient(null));

        nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);
        // should throw a KeypleRemoteException in slave side
    }


    /*
     * HELPERS
     */



}
