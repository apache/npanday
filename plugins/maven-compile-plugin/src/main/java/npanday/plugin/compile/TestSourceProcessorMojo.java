package npanday.plugin.compile;
 
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

import npanday.PlatformUnsupportedException;
import npanday.assembler.AssemblerContext;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Copies test source files to target directory.
 *
 * @author Shane Isbell
 * @goal process-test-sources
 * @phase process-sources
 * @description Copies test source files to target directory.
 */
public class TestSourceProcessorMojo
    extends AbstractMojo
{
    /**
     * .NET Language. The default value is <code>C_SHARP</code>. Not case or white-space sensitive.
     *
     * @parameter expression="${language}" default-value = "C_SHARP"
     * @required
     */
    private String language;

    public void execute()
        throws MojoExecutionException
    {
        getLog().info(
            "NPANDAY-905-002: Copying test source files has been skipped (see NPANDAY-210) - We could allow filtering of "
                + "test sources here, though!"
        );
    }
}
