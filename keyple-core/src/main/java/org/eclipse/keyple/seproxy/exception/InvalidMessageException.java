/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exception;

import java.util.Collections;
import java.util.List;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;

public class InvalidMessageException extends IOReaderException {

    public enum Type {
        PO, CSM
    }

    private final Type type;
    private final List<ApduRequest> requests;
    private final List<ApduResponse> responses;

    public InvalidMessageException(String message, Type type, List<ApduRequest> requests,
            List<ApduResponse> responses) {
        super(message);
        this.type = type;
        this.requests = requests;
        this.responses = responses;
    }

    public InvalidMessageException(String message, ApduRequest req, ApduResponse resp) {
        this(message, null, Collections.singletonList(req), Collections.singletonList(resp));
    }

    public InvalidMessageException(String message, ApduResponse resp) {
        this(message, null, resp);
    }

    public Type getType() {
        return type;
    }

    public List<ApduRequest> getRequests() {
        return requests;
    }

    public List<ApduResponse> getResponses() {
        return responses;
    }
}