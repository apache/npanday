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
package npanday.registry;

import java.io.InputStream;
import java.io.IOException;
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
     * Loads the configuration file and configuration properties. In the case below, the <code>inputStream</code>
     * contains the adapters.txt file and the <code>properties</code> holds the init-params. The init params should be
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
     * Since this method uses an <code>InputStream</code> parameter, the configuration file can be loaded off of the
     * local file system or from a specific URL located at an HTTP address.
     *
     * @param inputStream the configuration file
     * @param properties  the properties used to configure the repository
     * @throws NPandayRepositoryException thrown on interrupted I/O. Implementing class may also use this exception to throw
     *                     other exceptions like invalid properties.
     */
    void load( InputStream inputStream, Hashtable properties )
        throws NPandayRepositoryException;

    /**
     * @param repositoryRegistry
     */
    void setRepositoryRegistry( RepositoryRegistry repositoryRegistry );
    
    /**
     * Sets the URI of the file used to initialize the repository.
     * @param fileUri
     */
    void setSourceUri( String fileUri );

    /**
     * Reloads this repository based on the file uri.
     */
    void reload() throws IOException, NPandayRepositoryException;

}