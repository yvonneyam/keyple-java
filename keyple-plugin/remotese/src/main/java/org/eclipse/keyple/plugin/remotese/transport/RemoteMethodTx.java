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
package org.eclipse.keyple.plugin.remotese.transport;

import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to implement blocking and non blocking call to a Remote Method that sends a
 * response
 * 
 * @param <T> : type of the response
 */
public abstract class RemoteMethodTx<T> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteMethodTx.class);

    // response
    private T response;

    // exception thrown if any
    private KeypleRemoteException remoteException;

    // blocking mecanism
    private CountDownLatch lock;
    private RemoteMethodTxCallback<T> callback;

    private DtoSender sender;

    void setDto(DtoSender sender) {
        this.sender = sender;
    }

    /**
     * Internal method to set manually the keypleDto response To be called by the tx manager
     */
    public abstract T parseResponse(KeypleDto keypleDto) throws KeypleRemoteException;


    /**
     * Non blocking method to get results from the remote method call
     * 
     * @param callback
     */
    final public void asyncGet(RemoteMethodTxCallback<T> callback) throws KeypleRemoteException {
        this.callback = callback;
        sender.sendDTO(this.dto());
    }


    /**
     * Blocking method to get results from the remote method call. To be called by the client
     * 
     * @return T :
     * @throws KeypleRemoteException
     */
    final public T get() throws KeypleRemoteException {
        logger.debug("Blocking Get {}");

        Thread asyncGet = new Thread() {
            public void run() {
                try {
                    asyncGet(new RemoteMethodTxCallback<T>() {
                        @Override
                        public void get(T response, KeypleRemoteException exception) {
                            logger.debug("release lock");
                            lock.countDown();
                        }
                    });
                } catch (KeypleRemoteException e) {
                    logger.error("Exception while sending Dto");
                    lock.countDown();
                }
            }
        };

        try {

            lock = new CountDownLatch(1);
            asyncGet.start();
            lock.await();
            logger.debug("tread unlock in RemoteMethodTx");
            if (this.remoteException != null) {
                throw remoteException;
            } else {
                return response;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Thread locking in blocking transmitSet has encountered an exception", e);
        }
    }

    /**
     * Process the response contains in the keypleDto Response
     * 
     * @param keypleDto
     */
    void asyncSetResponse(KeypleDto keypleDto) {
        logger.debug("asyncSetResponse : {} - remoteException : {}", response, remoteException);
        try {
            this.response = parseResponse(keypleDto);
            this.callback.get(response, null);
        } catch (KeypleRemoteException e) {
            this.remoteException = e;
            this.callback.get(null, e);
        }
    }

    /**
     * Generates a Request Dto for this Remote Method call
     * 
     * @return
     */
    public abstract KeypleDto dto();


}
