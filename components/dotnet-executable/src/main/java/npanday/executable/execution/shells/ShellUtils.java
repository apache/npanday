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

package npanday.executable.execution.shells;

import npanday.executable.execution.ArgumentQuotingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * <i>Workaround for https://jira.codehaus.org/browse/PLXUTILS-147</i>
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class ShellUtils
{
    public static List buildCommandLine(
        ArgumentQuotingStrategy quotingStrategy, String executable, String[] arguments, String executionPreamble,
        boolean quotedExecutableEnabled, char[] executableEscapeChars, char executableQuoteChar,
        boolean quotedArgumentsEnabled, char[] argumentEscapeChars, char argumentQuoteChar,
        char[] quotingTriggerChars )
    {
        List commandLine = new ArrayList();
        StringBuffer sb = new StringBuffer();

        if ( executable != null )
        {
            String preamble = executionPreamble;
            if ( preamble != null )
            {
                sb.append( preamble );
            }

            if ( quotedExecutableEnabled )
            {
                sb.append(
                    quotingStrategy.quoteAndEscape(
                        executable, executableQuoteChar, executableEscapeChars, quotingTriggerChars, '\\', false
                    )
                );
            }
            else
            {
                sb.append( executable );
            }
        }
        for ( int i = 0; i < arguments.length; i++ )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " " );
            }

            if ( quotedArgumentsEnabled )
            {
                sb.append(
                    quotingStrategy.quoteAndEscape(
                        arguments[i], argumentQuoteChar, argumentEscapeChars, quotingTriggerChars, '\\', false
                    )
                );
            }
            else
            {
                sb.append( arguments[i] );
            }
        }

        commandLine.add( sb.toString() );

        return commandLine;
    }
}
