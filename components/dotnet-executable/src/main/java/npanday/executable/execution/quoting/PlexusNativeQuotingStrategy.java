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

import npanday.executable.execution.ArgumentQuotingStrategy;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class PlexusNativeQuotingStrategy
    implements ArgumentQuotingStrategy
{
    public String quoteAndEscape(
        String source, char quoteChar, char[] escapedChars, char[] quotingTriggers, char escapeChar, boolean force )
    {
        return StringUtils.quoteAndEscape( source, quoteChar, escapedChars, quotingTriggers, escapeChar, force );
    }
}
