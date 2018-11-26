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

import java.util.SortedSet;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.eclipse.keyple.plugin.remotese.transport.TransportNode;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportDto;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class Integration {

    /**
     * Create a Virtual Reader Service
     * 
     * @param node
     * @param observer
     * @return
     */
    public static VirtualReaderService bindMaster(TransportNode node,
            Observable.Observer observer) {
        // Create Master services : virtualReaderService
        VirtualReaderService virtualReaderService =
                new VirtualReaderService(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        ReaderPlugin rsePlugin = virtualReaderService.getPlugin();
        ((Observable) rsePlugin).addObserver(observer);

        // Binds virtualReaderService to a
        virtualReaderService.bindDtoEndpoint(node);

        return virtualReaderService;
    }

    /**
     * Create a Native Reader Service
     * 
     * @param node
     * @return
     */
    public static NativeReaderServiceImpl bindSlave(TransportNode node) {
        // Binds node for outgoing KeypleDto
        NativeReaderServiceImpl nativeReaderService = new NativeReaderServiceImpl(node);

        // Binds node for incoming KeypleDTo
        nativeReaderService.bindDtoEndpoint(node);

        return nativeReaderService;
    }

    /**
     * Create a Spy Native Reader Service
     * 
     * @param node
     * @return
     */
    public static NativeReaderServiceImpl bindSlaveSpy(TransportNode node) {
        // Binds node for outgoing KeypleDto
        NativeReaderServiceImpl nativeReaderService = new NativeReaderServiceImpl(node);

        NativeReaderServiceImpl spy = Mockito.spy(nativeReaderService);

        // Binds node for incoming KeypleDTo
        spy.bindDtoEndpoint(node);

        return spy;
    }

    /**
     * Create a Stub reader
     * 
     * @param stubReaderName
     * @return
     * @throws InterruptedException
     * @throws KeypleReaderNotFoundException
     */
    public static StubReader createStubReader(String stubReaderName)
            throws InterruptedException, KeypleReaderNotFoundException {
        SeProxyService seProxyService = SeProxyService.getInstance();

        StubPlugin stubPlugin = StubPlugin.getInstance();
        seProxyService.addPlugin(stubPlugin);

        // add an observer to start the plugin monitoring thread
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {

            }
        });

        stubPlugin.plugStubReader(stubReaderName);

        Thread.sleep(100);

        // get the created proxy reader
        return (StubReader) stubPlugin.getReader(stubReaderName);
    }

    /**
     * Create a mock method for onDto() that checks that keypleDto contains an exception
     * 
     * @return
     */
    public static Answer<TransportDto> assertContainsException() {
        return new Answer<TransportDto>() {
            @Override
            public TransportDto answer(InvocationOnMock invocationOnMock) throws Throwable {
                TransportDto transportDto = invocationOnMock.getArgument(0);

                // assert that returning dto DOES contain an exception
                Assert.assertTrue(KeypleDtoHelper.containsException(transportDto.getKeypleDTO()));
                return new LocalTransportDto(KeypleDtoHelper.NoResponse(), null);
            }
        };
    }

}
