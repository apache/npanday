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
package npanday.dao;

/**
 * Class for accessing information about project dependencies.
 */
public class ProjectDependency
    extends Project
{
    /**
     * The scope: runtime, compile, test
     */
    private String scope;
    private String systemPath;

    /**
     * Returns the scope:  runtime, compile, test
     *
     * @return the scope:  runtime, compile, test
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * Sets the scope:  runtime, compile, test
     *
     * @param scope the scope that the artifact is used in
     */
    public void setScope( String scope )
    {
        this.scope = scope;
    }
    
    
    public String getSystemPath()
    {
        return systemPath;
    }
    
    public void setSystemPath(String systemPath)
    {
        this.systemPath = systemPath;
    }

}
