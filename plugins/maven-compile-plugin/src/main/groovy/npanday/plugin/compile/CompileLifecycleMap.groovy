/* 
 * Copyright 2010 NPanday
 * 
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
package npanday.plugin.compile;

import org.apache.maven.lifecycle.Lifecycle;
import npanday.lifecycle.LifecycleMapping;
import npanday.lifecycle.LifecyclePhase;
import npanday.lifecycle.LifecycleMap;
import npanday.ArtifactType;

import npanday.lifecycle.LifecycleMappingBuilder;

/**
 * The lifecycles defined by the maven-compile-plugin..
 * 
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 */
class CompileLifecycleMap extends LifecycleMap
{
	void defineMappings(String npandayVersion) {

      	def mv_install = "org.apache.maven.plugins:maven-install-plugin:install"
        def mv_deploy = "org.apache.maven.plugins:maven-deploy-plugin:deploy"

        def np_generate_settings = "org.apache.npanday.plugins:NPanday.Plugin.Settings.JavaBinding:$npandayVersion:generate-settings"
        def np_compile_init = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:initialize"
        def np_resolve = "org.apache.npanday.plugins:maven-resolver-plugin:$npandayVersion:resolve"
        def np_generate_assemblyinfo = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:generate-assembly-info"
        def np_compile_process_sources = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:process-sources"
        def np_compile_process_test_sources = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:process-test-sources"
        def np_resgen_copy = "org.apache.npanday.plugins:maven-resgen-plugin:$npandayVersion:copy-resources"
        def np_resgen_generate = "org.apache.npanday.plugins:maven-resgen-plugin:$npandayVersion:generate"
        def np_resgen_resx = "org.apache.npanday.plugins:maven-resgen-plugin:$npandayVersion:generate-existing-resx-to-resource"
        def np_compile = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:compile"
        def np_test_compile = "org.apache.npanday.plugins:maven-compile-plugin:$npandayVersion:testCompile"
        def np_test = "org.apache.npanday.plugins:maven-test-plugin:$npandayVersion:test"

		def default_validate = [np_compile_init, np_resolve, np_generate_settings]
		def default_generate_sources = [np_generate_assemblyinfo]
		def default_process_resources = [np_resgen_copy, np_resgen_generate, np_resgen_resx]
		def default_process_sources = [np_compile_process_sources, np_compile_process_test_sources]
		def default_install = ["org.apache.npanday.plugins:maven-install-plugin:$npandayVersion:install", mv_install]
		
		forTypes( [ArtifactType.DOTNET_LIBRARY, ArtifactType.LIBRARY] ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forType( ArtifactType.NAR ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b._package ("org.apache.npanday.plugins:maven-webapp-plugin:$npandayVersion:package")
			b.deploy ("org.apache.npanday.plugins:maven-webapp-plugin:$npandayVersion:deploy")
		}
		forTypes( [ArtifactType.DOTNET_MODULE, ArtifactType.MODULE] ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b._package ("org.apache.npanday.plugins:maven-link-plugin:$npandayVersion:package")
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forType( ArtifactType.ASP ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (np_compile_process_sources, np_compile_process_test_sources, "org.apache.npanday.plugins:maven-aspx-plugin:$npandayVersion:copy-dependency")
			b.process_resources (default_process_resources)
			b.compile (np_compile, "org.apache.npanday.plugins:maven-aspx-plugin:$npandayVersion:compile")
			b.test_compile (np_test_compile)
			b.test (np_test)
			b._package ("org.apache.npanday.plugins:maven-aspx-plugin:$npandayVersion:package")
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forTypes( [ArtifactType.DOTNET_EXECUTABLE, ArtifactType.EXE, ArtifactType.WINEXE] ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forTypes( [ArtifactType.DOTNET_MAVEN_PLUGIN, ArtifactType.NETPLUGIN] ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b.install (default_install)
			b._package ("org.apache.npanday.plugins:maven-mojo-generator-plugin:$npandayVersion:generate-bindings")
            b.deploy (mv_deploy)
		}
		forType( ArtifactType.VISUAL_STUDIO_ADDIN ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forType( ArtifactType.SHARP_DEVELOP_ADDIN ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b.install (default_install)
			b.deploy (mv_deploy)
		}

		forTypes( [ArtifactType.DOTNET_EXECUTABLE_CONFIG, ArtifactType.EXECONFIG] ) {
			LifecycleMappingBuilder b->
			b.install (default_install)
			b.deploy (mv_deploy)
		}
	}
	
	void forType(ArtifactType type, Closure phases){
		add(LifecycleMappingBuilder.build(type, phases))
	}

	void forTypes(List types, Closure phases){
		types.each {
			ArtifactType type->
			add(LifecycleMappingBuilder.build(type, phases))
        }
	}
}
