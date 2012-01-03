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

package npanday.executable;

import org.apache.commons.exec.util.StringUtils;

import java.io.File;

/**
 * Use this in combination with {@link npanday.executable.execution.CommonsExecCommandExecutor} configured
 * in your executable-config.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class ArgUtils
{
    /**
     * Takes care of quoting individually per part. For example,
     * if you pass <code>"/go:", new File("c:\\with space")</code>, it will yield
     * <code>/go:"c:\with space"</code>
     */
    public static String combine( Object... objects )
    {
        StringBuilder command = new StringBuilder();
        for ( Object item : objects )
        {
            if (item == null){
                continue;
            }

            if ( item instanceof File )
            {
                command.append(
                    StringUtils.quoteArgument(
                        StringUtils.fixFileSeparatorChar( ( (File) item ).getAbsolutePath() )
                    )
                );
            }
            else if ( item instanceof String )
            {
                command.append( StringUtils.quoteArgument( (String) item ) );
            }
            else{
               command.append( StringUtils.quoteArgument( item.toString() ) );
            }
        }

        return command.toString();
    }
}
