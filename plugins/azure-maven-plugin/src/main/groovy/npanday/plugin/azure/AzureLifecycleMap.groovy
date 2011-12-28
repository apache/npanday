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

package npanday.plugin.azure;

import static LifecycleMappingBuilder.build as forType

import npanday.lifecycle.LifecycleMap
import npanday.lifecycle.LifecycleMappingBuilder

/**
 * Definition of the azure cloud service lifecycle.
 *
 * @author Lars Corneliussen
 */
class AzureLifecycleMap extends LifecycleMap
{
    void defineMappings(String npandayVersion) {
        def azure = "org.apache.npanday.plugins:azure-maven-plugin:$npandayVersion"
        def msdeploy = "org.apache.npanday.plugins:msdeploy-maven-plugin:$npandayVersion"

        def mv_install = "org.apache.maven.plugins:maven-install-plugin:install"
        def mv_deploy = "org.apache.maven.plugins:maven-deploy-plugin:deploy"

	    add(forType(npanday.ArtifactType.AZURE_CLOUD_SERVICE, {LifecycleMappingBuilder b ->
            b.prepare_package(
                "$azure:resolve-worker-roles",
                "$msdeploy:unpack-dependencies",
                "$azure:process-cloud-service-configuration"
            )
            b._package( "$azure:create-cloud-service-package" )
            b.install( mv_install )
			b.deploy( mv_deploy )
		}))
	}
}
