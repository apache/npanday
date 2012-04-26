package npanday.plugin.libraryimporter
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, version 2.0 (the
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


;


import npanday.lifecycle.LifecyclePrinter
import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo

/**
 * Lists the types and lifecycles this plugin configures when used as an extension.
 *
 * @author Lars Corneliussen
 * @goal describe
 */
class DescribeMojo
    extends GroovyMojo
{
     /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

	void execute() {
		LifecyclePrinter.printAll(npanday.plugin.customlifecycle.CustomLifecycleMap, project.version, {getLog().info(it)})
	}
}
