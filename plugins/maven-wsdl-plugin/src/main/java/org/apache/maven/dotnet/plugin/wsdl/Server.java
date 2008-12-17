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
package npanday.plugin.wsdl;

/**
 * Holds server information of WSDL host.
 *
 * @author Shane Isbell
 */
public class Server
{

    private String id;

    private boolean hashPassword;

    private String hashAlg;

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public boolean isHashPassword()
    {
        return hashPassword;
    }

    public void setHashPassword( boolean hashPassword )
    {
        this.hashPassword = hashPassword;
    }

    public String getHashAlg()
    {
        return hashAlg;
    }

    public void setHashAlg( String hashAlg )
    {
        this.hashAlg = hashAlg;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final Server server = (Server) o;

        if ( !id.equals( server.id ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return id.hashCode();
    }

    public String toString()
    {
        return "id = " + id;
    }
}
