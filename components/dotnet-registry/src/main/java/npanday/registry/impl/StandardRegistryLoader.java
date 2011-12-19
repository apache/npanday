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
package npanday.registry.impl;

import com.google.common.base.Preconditions;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.RegistryLoader;
import npanday.registry.Repository;
import npanday.registry.RepositoryLoader;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * The default loader for the registry-config.xml file.
 *
 * @author Shane Isbell
 * @plexus.component
 *   role="npanday.registry.RegistryLoader"
 */
public class StandardRegistryLoader
    extends AbstractLogEnabled
    implements RegistryLoader
{
    /**
     * Internal list of <code>RepositoryObject</code>s
     */
    private List repositories = new ArrayList();

    private Hashtable repoMap = new Hashtable();

    private RepositoryLoader repositoryLoader;

    /**
     * Loads the registry-config file
     *
     * @param inputStream inputstream containing registry-config file
     * @throws java.io.IOException
     */
    public final void loadRegistry( InputStream inputStream )
        throws IOException, NPandayRepositoryException
    {

        KXmlParser parser = new KXmlParser();
        try
        {
            parser.setInput( inputStream, null );
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( e.toString() );
        }
        try
        {
            parser.nextTag();
            parser.require( XmlPullParser.START_TAG, null, "registry-config" );
            parser.nextTag();
            parser.require( XmlPullParser.START_TAG, null, "repositories" );

            while ( parser.nextTag() == XmlPullParser.START_TAG )
            {
                parser.require( XmlPullParser.START_TAG, null, "repository" );
                RepositoryObject rep = getRepositoryObject( parser );
                repositories.add( rep );                
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new IOException( "NPANDAY-083-000: Message = " + e.toString() );
        }
        loadIntoRegistry();
    }

    public final Hashtable getRepositories()
    {
        return repoMap;
    }


    /**
     * Resolves system variables within the path
     *
     * @param fileName name of the configuration file
     * @return path of the file with resolved system variables. Default value is '.'
     */
    private String toPath( String fileName )
    {
        byte[] path = fileName.getBytes();
        int length = path.length;
        StringBuffer env = new StringBuffer();
        StringBuffer filePath = new StringBuffer();
        for ( int i = 0; i < length; )
        {
            if ( i >= length - 2 )
            {
                filePath.append( (char) path[i++] );
            }
            else if ( path[i] == 36 )
            {
                if ( path[++i] == 123 )
                {
                    i++;
                    while ( i < length - 1 && path[i] != 125 )
                    {
                        env.append( (char) path[i++] );
                    }
                    if ( path[i] == 125 )
                    {
                        i++;
                    }
                }
                else
                {
                    i--;
                    i--;
                }
                String pathEnv = System.getProperty( env.toString().trim(), "." );
                filePath.append( pathEnv.toString() );
            }
            else
            {
                filePath.append( (char) path[i++] );
            }
        }//end for:i
        String str = filePath.toString();
        if (!str.startsWith("/")) {
            str = str.replaceAll("/", "\\\\");
            str = str.replaceAll("\\\\:", ":");
            str = str.replaceAll("\\\\\\\\", "\\\\");
        }
        return str;
    }

    /**
     * Loads all of the repositories into the registry
     *
     * @throws IOException
     */
    private void loadIntoRegistry()
        throws IOException, NPandayRepositoryException
    {
        Preconditions.checkNotNull( repositoryLoader, "NPANDAY-083-001: Repository Loader does not exist" );

        for ( Iterator i = repositories.iterator(); i.hasNext(); )
        {
            RepositoryObject repositoryObject = (RepositoryObject) i.next();
            String repositoryName = repositoryObject.getRepositoryName();
            String className = repositoryObject.getRepositoryClass();
            String fileName = repositoryObject.getRepositoryConfig();

            if (getLogger().isDebugEnabled()){
                getLogger().debug( "NPANDAY-083-002: Loading repository '" + repositoryName + "'");
            }

            //instantiate class based on info in the registry-config file
            Repository repository =
                repositoryLoader.loadRepository( toPath( fileName ), className, repositoryObject.getInitParams() );

            if ( repository != null )
            {
                repoMap.put( repositoryName, repository );

                if (getLogger().isDebugEnabled()){
                    getLogger().debug( "NPANDAY-083-003: Loaded repository '" + repositoryName + "': " + repository);
                }
            }
            else{
                getLogger().warn( "NPANDAY-083-003: Loader for repository '" + repositoryName + "' returned <null>");
            }
        }
    }

    /**
     * Constructs a <code>RepositoryObject</code> from the registry-config file
     *
     * @param parser
     * @return <code>RepositoryObject</code>
     * @throws IOException
     * @throws XmlPullParserException
     */
    private RepositoryObject getRepositoryObject( KXmlParser parser )
        throws IOException, XmlPullParserException
    {
        RepositoryObject repositoryObject = new RepositoryObject();
        Hashtable initParams = new Hashtable();
        for ( int i = 0; parser.nextTag() == XmlPullParser.START_TAG; i++ )
        {
            switch ( i )
            {
                case 0:
                    parser.require( XmlPullParser.START_TAG, null, "repository-name" );
                    repositoryObject.setRepositoryName( parser.nextText() );
                    break;
                case 1:
                    parser.require( XmlPullParser.START_TAG, null, "repository-class" );
                    repositoryObject.setRepositoryClass( parser.nextText() );
                    break;
                case 2:
                    parser.require( XmlPullParser.START_TAG, null, "repository-config" );
                    repositoryObject.setRepositoryConfig( parser.nextText() );
                    break;
                default:
                    parser.require( XmlPullParser.START_TAG, null, "init-param" );

                    String paramName = null;
                    String paramValue = null;
                    for ( int j = 0; parser.nextTag() == XmlPullParser.START_TAG; j++ )
                    {

                        switch ( j )
                        {
                            case 0:
                                parser.require( XmlPullParser.START_TAG, null, "param-name" );
                                paramName = parser.nextText();
                                break;
                            case 1:
                                parser.require( XmlPullParser.START_TAG, null, "param-value" );
                                paramValue = parser.nextText();
                                break;
                            default:
                                throw new IOException();
                        }
                    }//end params
                    if ( paramName != null && paramValue != null )
                    {
                        initParams.put( paramName, paramValue );
                    }
            }//end all tags
            repositoryObject.setInitParams( initParams );
        }

        return repositoryObject;
    }

    public void setRepositoryLoader( RepositoryLoader repositoryLoader )
    {
        this.repositoryLoader = repositoryLoader;
    }

    /**
     * Value Object for Repository Information
     */
    private class RepositoryObject
    {

        /**
         * Name of the repository
         */
        private String repositoryName;

        /**
         * package and class name of the repository
         */
        private String repositoryClass;

        /*Path and name of the repository config file*/
        private String repositoryConfig;

        /**
         * Initialization parameters of the repository
         */
        private Hashtable initParams;

        /**
         * Empty Constructor
         */
        RepositoryObject()
        {
        }

        /**
         * Constructor
         *
         * @param repositoryName   name of the repository
         * @param repositoryClass  path and name of the repository config file
         * @param repositoryConfig Path and name of the repository config fil
         */
        RepositoryObject( String repositoryName, String repositoryClass, String repositoryConfig )
        {
            this.repositoryName = repositoryName;
            this.repositoryClass = repositoryClass;
            this.repositoryConfig = repositoryConfig;
        }

        String getRepositoryName()
        {
            return repositoryName;
        }

        void setRepositoryName( String repositoryName )
        {
            this.repositoryName = repositoryName;
        }

        String getRepositoryClass()
        {
            return repositoryClass;
        }

        void setRepositoryClass( String repositoryClass )
        {
            this.repositoryClass = repositoryClass;
        }

        String getRepositoryConfig()
        {
            return repositoryConfig;
        }

        void setRepositoryConfig( String repositoryConfig )
        {
            this.repositoryConfig = repositoryConfig;
        }

        Hashtable getInitParams()
        {
            return initParams;
        }

        void setInitParams( Hashtable initParams )
        {
            this.initParams = initParams;
        }

        /**
         * Classes are equal if they have the same values for class, config and name
         */
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof RepositoryObject ) )
            {
                return false;
            }

            final RepositoryObject repositoryObject = (RepositoryObject) o;

            if ( !repositoryClass.equals( repositoryObject.repositoryClass ) )
            {
                return false;
            }
            if ( !repositoryConfig.equals( repositoryObject.repositoryConfig ) )
            {
                return false;
            }
            if ( !repositoryName.equals( repositoryObject.repositoryName ) )
            {
                return false;
            }

            return true;
        }

        /**
         * Classes have identical hash code if they have the same values for class, config and name
         */
        public int hashCode()
        {
            int result;
            result = repositoryName.hashCode();
            result = 29 * result + repositoryClass.hashCode();
            result = 29 * result + repositoryConfig.hashCode();
            return result;
        }

        public String toString()
        {
            return "Name = " + repositoryName + ", Class = " + repositoryClass + ", Config = " + repositoryConfig;
        }
    }

}
