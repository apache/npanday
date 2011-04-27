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
	def mv_install = "org.apache.maven.plugins:maven-install-plugin:install" 
	def mv_deploy = "org.apache.maven.plugins:maven-deploy-plugin:deploy" 
	
	def np_generate_settings = "npanday.plugin:NPanday.Plugin.Settings.JavaBinding:generate-settings" 
	def np_compile_init = "npanday.plugin:maven-compile-plugin:initialize"
	def np_resolve = "npanday.plugin:maven-resolver-plugin:resolve" 
	def np_generate_assemblyinfo = "npanday.plugin:maven-compile-plugin:generate-assembly-info" 
	def np_compile_process_sources = "npanday.plugin:maven-compile-plugin:process-sources" 
	def np_compile_process_test_sources = "npanday.plugin:maven-compile-plugin:process-test-sources" 
	def np_resgen_copy = "npanday.plugin:maven-resgen-plugin:copy-resources" 
	def np_resgen_generate = "npanday.plugin:maven-resgen-plugin:generate" 
	def np_resgen_resx = "npanday.plugin:maven-resgen-plugin:generate-existing-resx-to-resource" 
	def np_compile = "npanday.plugin:maven-compile-plugin:compile" 
	def np_test_compile = "npanday.plugin:maven-compile-plugin:testCompile" 
	def np_test = "npanday.plugin:maven-test-plugin:test" 
	def np_convert = "npanday.plugin:maven-repository-plugin:convert-artifact"
	
	void defineMappings() {
		
		def default_validate = [np_compile_init, np_resolve, np_generate_settings]
		def default_generate_sources = [np_generate_assemblyinfo]
		def default_process_resources = [np_resgen_copy, np_resgen_generate, np_resgen_resx]
		def default_process_sources = [np_compile_process_sources, np_compile_process_test_sources]
		def default_install = [np_convert, 'npanday.plugin:maven-install-plugin:install', mv_install]
		
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
			b._package ('npanday.plugin:maven-webapp-plugin:package')
			b.deploy ('npanday.plugin:maven-webapp-plugin:deploy')
		}
		forTypes( [ArtifactType.DOTNET_MODULE, ArtifactType.MODULE] ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.process_sources (default_process_sources)
			b.process_resources (default_process_resources)
			b.compile (np_compile)
			b.test_compile (np_test_compile)
			b.test (np_test)
			b._package ('npanday.plugin:maven-link-plugin:package')
			b.install (default_install)
			b.deploy (mv_deploy)
		}
		forType( ArtifactType.ASP ) {
			LifecycleMappingBuilder b->
			b.validate (default_validate)
			b.generate_sources (default_generate_sources)
			b.process_sources (np_compile_process_sources, np_compile_process_test_sources, 'npanday.plugin:maven-aspx-plugin:copy-dependency')
			b.process_resources (default_process_resources)
			b.compile (np_compile, 'npanday.plugin:maven-aspx-plugin:compile')
			b.test_compile (np_test_compile)
			b.test (np_test)
			b._package ('npanday.plugin:maven-aspx-plugin:package')
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
			b.package ('npanday.plugin:maven-mojo-generator-plugin:generate-bindings')
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
