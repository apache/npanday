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

/**
 * This script generates the plexus-configuration for registration 
 * of artifact handlers and lifecycles.
 * 
 * @author <a href="mailto:me@lcorneliussen.de">Lars Corneliussen</a>
 **/
import npanday.plugin.customlifecycle.CustomLifecycleMap
import npanday.lifecycle.LifecycleConfigurationGenerator

def plexus = new File(project.build.outputDirectory, 'META-INF/plexus')
plexus.mkdirs()

def componentsXmlFile = new File(plexus, 'components.xml');

LifecycleConfigurationGenerator
		.persistAllTypesAndLifecycles(CustomLifecycleMap, project.version, componentsXmlFile)
