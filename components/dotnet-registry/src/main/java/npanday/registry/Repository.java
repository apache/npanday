package npanday.registry;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

/**
 * The interface for repositories managed by the <code>RepositoryRegistry</code>. The implementing classes should
 * provide the methods for accessing the domain specific data.
 *
 * @author Shane Isbell
 */

public interface Repository
{
    /**
     * Loads a configuration file or resource. In the case below, the <code>source</code>
     * contains the <code>Uri</code> for a file or resource and the <code>properties</code> holds the init-params. The init params should be
     * used to specialize the repository configuration.  The example below shows that you can add new properties
     * to <code>MyRepository</code> but not delete them.
     * <pre>
     * &lt;registry-config&gt;
     * &lt;repositories&gt;
     * &lt;repository&gt;
     * &lt;repository-name&gt;adapter&lt;/repository-name&gt;
     * &lt;repository-class&gt;org.jvending.sample.MyRepository&lt;/repository-class&gt;
     * &lt;repository-config&gt;${basedir}/adapters.txt&lt;/repository-config&gt;
     * &lt;init-param&gt;
     * &lt;param-name&gt;add&lt;/param-name&gt;
     * &lt;param-value&gt;true&lt;/param-value&gt;
     * &lt;/init-param&gt;
     * &lt;init-param&gt;
     * &lt;param-name&gt;delete&lt;/param-name&gt;
     * &lt;param-value&gt;false&lt;/param-value&gt;
     * &lt;/init-param&gt;
     * &lt;/repository&gt;
     * &lt;/repositories&gt;
     * &lt;/registry-config&gt;
     * </pre>
     * <p/>
     *
     * @param source
     * @throws NPandayRepositoryException thrown on interrupted I/O. Implementing class may also use this exception to throw
     *                                    other exceptions like invalid properties.
     */
    void load( URL source )
        throws NPandayRepositoryException;

    /**
     * Removes all added sources and clears out the contents.
     * @throws OperationNotSupportedException
     */
    void clearAll()
        throws OperationNotSupportedException;

    /**
     * Reloads this repository based on all provided sources.
     */
    void reloadAll()
        throws IOException, NPandayRepositoryException, OperationNotSupportedException;

    /**
     * The properties configured in the registry.
     */
    void setProperties( Hashtable props );
}