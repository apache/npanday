package npanday.plugin.msdeploy.create;

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

import java.util.List;

/**
 * Picks up a prepared package folder and packages it using the MSDeploy command line tool
 * and the <a href="http://technet.microsoft.com/en-us/library/dd569034(v=ws.10).aspx">Web Deploy contentPath
 * Provider</a>.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @phase package
 * @goal create-content-package
 */
public class CreateContentPackageMojo
    extends AbstractCreatePackageMojo
{
    @Override
    protected List<Package> prepareIterationItems()
    {
        List<Package> packages = super.prepareIterationItems();

        for ( Package pkg : packages )
        {
            pkg.setSourceProvider( "contentPath" );
        }

        return packages;
    }
}
