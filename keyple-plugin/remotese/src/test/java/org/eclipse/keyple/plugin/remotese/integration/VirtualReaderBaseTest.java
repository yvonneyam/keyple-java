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


import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Virtual Reader Service with stub plugin and hoplink SE
 */
public class VirtualReaderBaseTest {

    // fail :
    // transmitset : 0,1
    // transmit : 0,2

    @Rule
    public TestName name = new TestName();

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderBaseTest.class);

    // Real objects
    private TransportFactory factory;
    private ObservablePlugin.PluginObserver stubPluginObserver;
    private NativeReaderServiceImpl nativeReaderService;
    StubReader nativeReader;
    VirtualReader virtualReader;

    final String NATIVE_READER_NAME = "testStubReader";
    final String CLIENT_NODE_ID = "testClientNodeId";

    // Spy Object
    VirtualReaderService virtualReaderService;

    @Before
    public void setTup() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName());
        logger.info("------------------------------");

        // assert that there is no stub readers plugged already
        Assert.assertEquals(0, StubPlugin.getInstance().getReaders().size());

        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server). Only one client and one server bound together.
        factory = new LocalTransportFactory();

        stubPluginObserver = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent pluginEvent) {
                logger.debug("Default Stub Plugin Observer : {}", pluginEvent.getEventType());
            }

        };

        logger.info("*** Bind Master Services");
        // bind Master services to server
        virtualReaderService = Integration.bindMaster(factory.getServer());

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        nativeReaderService = Integration.bindSlave(factory.getClient());

        // configure and connect a Stub Native reader
        nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID, stubPluginObserver);

        // test virtual reader
        virtualReader = getVirtualReader();


    }

    @After
    public void tearDown() throws Exception {

        logger.info("TearDown Test");

        StubPlugin stubPlugin = StubPlugin.getInstance();

        stubPlugin.unplugReader(nativeReader.getName());

        Thread.sleep(500);

        nativeReader.clearObservers();

        stubPlugin.removeObserver(stubPluginObserver);

        Thread.sleep(500);

        logger.info("End of TearDown Test");
    }



    private StubReader connectStubReader(String readerName, String nodeId,
            ObservablePlugin.PluginObserver observer) throws Exception {
        // configure native reader
        StubReader nativeReader = (StubReader) Integration.createStubReader(readerName, observer);
        nativeReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        this.nativeReaderService.connectReader(nativeReader, nodeId);
        return nativeReader;
    }

    private void disconnectStubReader(StubReader nativeReader, String nodeId) throws Exception {
        this.nativeReaderService.disconnectReader(nativeReader, nodeId);
    }

    private VirtualReader getVirtualReader() throws Exception {
        Assert.assertEquals(1, this.virtualReaderService.getPlugin().getReaders().size());
        return (VirtualReader) this.virtualReaderService.getPlugin().getReaders().first();
    }

}
