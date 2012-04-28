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

package npanday.executable.execution.quoting;

import com.google.common.collect.Lists;
import npanday.executable.execution.ArgumentQuotingStrategy;
import npanday.executable.execution.switches.Switch;
import npanday.executable.execution.switches.SwitchFormat;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class CustomSwitchAwareQuotingStrategy
    implements ArgumentQuotingStrategy
{
    /**
     * Switches that shouldn't be escaped at all
     */
    List<String> ignores = Lists.newArrayList();

    /**
     * Switches that should be quoted normally (as if it wasn't a switch)
     */
    List<String> quoteNormally = Lists.newArrayList();

    SwitchFormat[] supportedSwitchFormats = {
        new SwitchFormat( '/', '=' ), new SwitchFormat( '-', '=' ), new SwitchFormat( '/', ':' ),
        new SwitchFormat( '-', ':' )
    };

    private boolean ignorePrequoted = false;

    public CustomSwitchAwareQuotingStrategy( )
    {

    }

    public CustomSwitchAwareQuotingStrategy( SwitchFormat[] switchFormats )
    {
        supportedSwitchFormats = switchFormats;
    }

    public String quoteAndEscape(
        String source, char quoteChar, char[] escapedChars, char[] quotingTriggers, char escapeChar, boolean force )
    {
        if (ignorePrequoted && (source.contains( "\"" ) || source.contains( "'" )))
        {
            return source;
        }

        for ( SwitchFormat format : supportedSwitchFormats )
        {
            if ( format.isMatchingSwitch( source ) )
            {
                Switch parsed = format.parse( source );

                if (ignores.contains( parsed.getName() )){
                    return source;
                }

                if (quoteNormally.contains( parsed.getName() )){
                  return innerQuoteAndEscape( source, quoteChar, escapedChars, quotingTriggers, escapeChar, force );
                }

                return format.generate(
                    parsed.getName(),
                    innerQuoteAndEscape(
                        parsed.getValue(), quoteChar, escapedChars, quotingTriggers, escapeChar, force
                    )
                );
            }
        }

        return innerQuoteAndEscape( source, quoteChar, escapedChars, quotingTriggers, escapeChar, force );
    }


    private String innerQuoteAndEscape( String source,
                                char quoteChar,
                                final char[] escapedChars,
                                final char[] quotingTriggers,
                                char escapeChar,
                                boolean force )
    {
        // based on org.codehaus.plexus.util.StringUtils.quoteAndEscape()
        // but does also escape quotes, if they are leading + trailing
        // hence, "a" becomes "\"a\""

        if ( source == null )
        {
            return null;
        }

        String escaped = StringUtils.escape( source, escapedChars, escapeChar );

        boolean quote = false;
        if ( force )
        {
            quote = true;
        }
        else if ( !escaped.equals( source ) )
        {
            quote = true;
        }
        else
        {
            for ( int i = 0; i < quotingTriggers.length; i++ )
            {
                if ( escaped.indexOf( quotingTriggers[i] ) > -1 )
                {
                    quote = true;
                    break;
                }
            }
        }

        if ( quote )
        {
            return quoteChar + escaped + quoteChar;
        }

        return escaped;
    }

    public void addIgnore( String switchName )
    {
           ignores.add( switchName );
    }

    public void addQuoteNormally( String switchName )
    {
           quoteNormally.add( switchName );
    }

    public void setIgnorePrequoted( )
    {
        ignorePrequoted = true;
    }
}

