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
package npanday.lifecycle

import npanday.ArtifactType

/**
 * Generates a component.xml that configures Plexus components
 * for both types and lifecycle mappings.
 * 
 * @author Lars Corneliussen
 */
class LifecycleConfigurationGenerator {
	
	String currentXml
	
	public LifecycleConfigurationGenerator() {
		currentXml = "<component-set/>"
	}
	
	public LifecycleConfigurationGenerator(String xml) {
		currentXml = xml
	}
	
	void saveTo(File file){
		file.text = groovy.xml.XmlUtil.serialize(currentXml)
	}
	
	void configureAllTypes() {
		List types = ArtifactType.values()
				.findAll{it != npanday.ArtifactType.NULL}
		
		configureTypes(types)
	}
	
	void configureTypes(List<ArtifactType> types) {
		Closure components = {
			types.each{ typeDef ->
				component{
					role 'org.apache.maven.artifact.handler.ArtifactHandler'
					'role-hint'(typeDef.packagingType)
					implementation 'org.apache.maven.artifact.handler.DefaultArtifactHandler'
					configuration{
						extension typeDef.extension
						type typeDef.packagingType
					}
				}
			}
		}
		
		appendComponents(components)
	}
	
	void configureMappings(List<LifecycleMapping> lifecycleMappings){
		Closure components = {
			lifecycleMappings.each{ LifecycleMapping mapping ->
				component{
					role 'org.apache.maven.lifecycle.mapping.LifecycleMapping'
					'role-hint' mapping.type.packagingType
					implementation 'org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping'
					configuration {
						phases {
							mapping.phases.each{LifecyclePhase st ->
								"${st.name}"("\n" 
								+ " " * 12
								+ st.goals.join(",\n" + " " * 12)
								+ "\n" + " "*10)
							}
						}
					}
				}
			}	
		}
		
		appendComponents(components)
	}
	
	void appendComponents(Closure additionalComponents){
		def originalText = currentXml
		// todo: somehow preserve comments
		def originalComponents = new XmlSlurper().parseText(originalText).components.component
		
		def xml = new groovy.xml.StreamingMarkupBuilder()
		def resultXml = xml.bind{
			'component-set' {
				components {
					mkp.yield originalComponents
					mkp.yield additionalComponents
				}
			}
		}
		currentXml = resultXml.toString()
	}
	
	static void persistAllTypesAndLifecycles(Class lifecycleMap, String npandayVersion, File componentsXmlFile) {
		def g = lifecycleMap.newInstance() as LifecycleMap
		
		def componentsXml = componentsXmlFile.text
		
		def generator = new LifecycleConfigurationGenerator(componentsXml)
		generator.configureAllTypes()
		generator.configureMappings(g.buildMap(npandayVersion))
		generator.saveTo(componentsXmlFile)
	}
	
}
