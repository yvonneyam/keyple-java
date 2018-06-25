/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;

import java.util.SortedSet;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractObservablePlugin extends AbstractLoggedObservable<AbstractPluginEvent>
        implements ReadersPlugin {

    /**
     * The plugin name (must be unique)
     */
    private final String name;

    /**
     * The list of readers
     */
    protected SortedSet<AbstractObservableReader> readers = null;

    /**
     * Plugin constructor<br/>
     * Force the definition of a name through the use of super method.
     * 
     * @param name
     */
    protected AbstractObservablePlugin(String name) {
        this.name = name;
    }

    /**
     * Gets the reader name
     * 
     * @return the reader name string
     */
    public final String getName() {
        return name;
    }

    /**
     * Retrieve the current readers list.<br/>
     * Gets the list for the native method the first time (null)<br/>
     * Returns the current list after.<br/>
     * The list may be updated in background in the case of a threaded plugin
     * {@link AbstractThreadedObservablePlugin}
     * 
     * @return
     */
    public final SortedSet<AbstractObservableReader> getReaders() {
        if (readers == null) {
            try {
                readers = getNativeReaders();
            } catch (IOReaderException e) {
                // TODO add log
                e.printStackTrace();
            }
        }
        return readers;
    }

    /**
     * Gets a list of native readers from the native methods
     * 
     * @return the list of AbstractObservableReader objects.
     * @throws IOReaderException
     */
    protected abstract SortedSet<AbstractObservableReader> getNativeReaders()
            throws IOReaderException;

    /**
     * Gets the specific reader whose is provided as an argument.
     * 
     * @param name
     * @return the AbstractObservableReader object (null if not found)
     * @throws IOReaderException
     */
    protected abstract AbstractObservableReader getNativeReader(String name)
            throws IOReaderException;

    /**
     * Compare the name of the current ReadersPlugin to the name of the ReadersPlugin provided in
     * argument
     * 
     * @param plugin
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ReadersPlugin plugin) {
        return this.getName().compareTo(plugin.getName());
    }

    public interface PluginObserver extends Observer {
        void update(AbstractPluginEvent event);
    }
}
