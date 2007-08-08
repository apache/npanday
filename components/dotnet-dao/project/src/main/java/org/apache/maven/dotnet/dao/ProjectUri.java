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
package org.apache.maven.dotnet.dao;

/**
 * Enumeration of the project uri predicates.
 */
public enum ProjectUri
{
    GROUP_ID( "http://maven.apache.org/artifact/groupId", "groupId", false ),

    ARTIFACT_ID( "http://maven.apache.org/artifact/artifactId", "artifactId", false ),

    VERSION( "http://maven.apache.org/artifact/version", "versionValue", false ),

    ARTIFACT_TYPE( "http://maven.apache.org/artifact/artifactType", "artifactType", false ),

    CLASSIFIER( "http://maven.apache.org/artifact/classifier", "classifier", true ),

    IS_RESOLVED( "http://maven.apache.org/artifact/dependency/isResolved", "isResolved", true ),

    ARTIFACT( "http://maven.apache.org/Artifact", "artifact", false ),

    DEPENDENCY( "http://maven.apache.org/artifact/dependency", "dependency", true ),

    PARENT( "http://maven.apache.org/artifact/parent", "parent", true ),    

    VENDOR( "http://maven.apache.org/artifact/requirement/vendor", "vendor", false ),

    FRAMEWORK_VERSION( "http://maven.apache.org/artifact/requirement/frameworkVersion", "frameworkVersion", false );

    private String uri;

    private String bindingName;

    private boolean isOptional;

    ProjectUri( String uri, String bindingName, boolean isOptional )
    {
        this.uri = uri;
        this.bindingName = bindingName;
        this.isOptional = isOptional;
    }

    public String getPredicate()
    {
        return uri;
    }

    public String getObjectBinding()
    {
        return bindingName;
    }

    public boolean isOptional()
    {
        return isOptional;
    }

    public void setOptional( boolean isOptional )
    {
        this.isOptional = isOptional;
    }
}
