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
import java.util.Properties;

/**
 * This class is a simple facade for <code>java.util.properties</code>. Repositories that use an underlying properties
 * file (name, value pairs) can extend from this class and add additional domain specific methods. If the extending
 * class provides methods for adding additional properties after a loadRegistry, the getValue method may need to be
 * re-implemented to handle synchronization.
 * <p/>
 * <pre>
 * RepositoryRegistry.loadFromFile("./sample-config.xml");
 * PropertyRepository repository = (PropertyRepository) RepositoryRegistry.find("adapter");
 * String value = repository.getValue("myprop");    `
 * </pre>
 *
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component
 *   role="npanday.registry.PropertyRepository"
 */
public class PropertyRepository
    implements Repository
{

    /**
     * Internal reference for properties
     */
    protected Properties properties = new Properties();

    /**
     * Accessor for properties
     *
     * @param name the name of the property
     * @return String   value for the given name
     */
    public String getValue( String name )
    {
        return properties.getProperty( name );
    }

    public void load( URL source )
        throws NPandayRepositoryException
    {
        try
        {
            properties.load( source.openStream() );
        }
        catch( IOException e )
        {
            throw new NPandayRepositoryException( "NPANDAY-088-000: Unable to load properties file from " + source, e);
        }
    }

     /**
     * @see Repository#clearAll()
     */
    public void clearAll()
        throws OperationNotSupportedException
    {
        throw new OperationNotSupportedException(  );
    }

    /**
     * @see Repository#reloadAll()
     */
    public void reloadAll()
        throws IOException, OperationNotSupportedException
    {
        throw new OperationNotSupportedException(  );
    }

    /**
     * The properties configured in the registry.
     */
    public void setProperties( Hashtable props )
    {
        // we don't need any props
    }
}
