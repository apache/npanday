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
package org.apache.maven.dotnet.plugin.wsdl;

import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.PlatformUnsupportedException;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.ExecutionException;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Services for detecting the active platform for executables. With the new executable framework, this class may be
 * deprecated).
 *
 * @author Shane Isbell
 */
public interface PlatformDetector {
    /**
     * Sets the results that are included in the standard output for Microsoft.
     *
     * @param microsoftContainsString
     */
    void setMicrosoftContainsString(String microsoftContainsString);

    /**
     * Sets the results that are included in the standard output for Mono.
     *
     * @param monoContainsString
     */
    void setMonoContainsString(String monoContainsString);

    /**
     * Sets the results that are included in the standard output for DotGnu.
     *
     * @param gnuContainsString
     */
    void setGnuContainsString(String gnuContainsString);

    /**
     * Returns a Vendor instance for the given command and/or netHomePath. If the command is specified, you may need to
     * change the expected values of the set[gnu, mono, microsoft]ContainsString method(s) to match the expected output.
     * Both command and netHomePath cannot be null.
     *
     * @param command
     * @param netHomePath
     * @return a Vendor instance for the given command and/or netHomePath
     * @throws org.apache.maven.dotnet.PlatformUnsupportedException if the vendor cannot be matched
     */
    Vendor getVendorFor(String command, File netHomePath) throws PlatformUnsupportedException;

    public static class Factory {
        private Factory() {
        }

        public static PlatformDetector createDefaultPlatformDetector() {
            return new PlatformDetector() {

                /**
                 * String to be matched to standard output for Microsoft
                 */
                private String microsoftContainsString = "Microsoft";

                /**
                 * String to be matched to standard output for Mono.
                 */
                private String monoContainsString = "Mono";

                /**
                 * String to be matched to standard output for DotGnu.
                 */
                private String gnuContainsString = "Southern Storm";

                public void setMicrosoftContainsString(String microsoftContainsString) {
                    this.microsoftContainsString = microsoftContainsString;
                }

                public void setMonoContainsString(String monoContainsString) {
                    this.monoContainsString = monoContainsString;
                }

                public void setGnuContainsString(String gnuContainsString) {
                    this.gnuContainsString = gnuContainsString;
                }

                public Vendor getVendorFor(String command, File netHomePath) throws PlatformUnsupportedException {
                    String netHome = (netHomePath == null) ? null : netHomePath.getAbsolutePath();
                    if (isEmpty(command) && isEmpty(netHome)) {
                        throw new PlatformUnsupportedException("NMAVEN-042-000: Both command and netHome params cannot be null or empty");
                    } else if (!isEmpty(command) && isEmpty(netHome)) {
                        return getVendorForCommand(command);
                    } else if (isEmpty(command) && !isEmpty(netHome)) {
                        return getVendorFromPath(netHome);
                    } else if (!isEmpty(command) && !isEmpty(netHome)) {
                        try {
                            return getVendorFromPath(netHome);
                        } catch (PlatformUnsupportedException e) {

                            //log.debug(e);
                        }
                        try {
                            return getVendorForCommand(netHome + File.separator
                                    + "bin" + File.separator + command);
                        } catch (PlatformUnsupportedException e) {
                            throw new PlatformUnsupportedException("");
                        }
                    }
                    return null;
                }

                /**
                 * Determine the vendor based on executing the command and matching the standard output.
                 *
                 * @param command
                 * @return vendor instance
                 * @throws org.apache.maven.dotnet.PlatformUnsupportedException if the platform cannot be matched.
                 */
                private Vendor getVendorForCommand(String command) throws PlatformUnsupportedException {
                    CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
                    //commandExecutor.setLogger(logger);
                    try {
                        List<String> commands = new ArrayList<String>();
                        commandExecutor.executeCommand(command, commands);
                    } catch (ExecutionException e) {
                        throw new PlatformUnsupportedException("", e);
                    }
                    String results = commandExecutor.getStandardOut();
                    if (results.contains(microsoftContainsString)) {
                        return Vendor.MICROSOFT;
                    } else if (results.contains(monoContainsString)) {
                        return Vendor.MONO;
                    } else if (results.contains(gnuContainsString) || results.contains("cscc"))
                    {//cscc does not contain vendor name
                        return Vendor.DOTGNU;
                    } else {
                        throw new PlatformUnsupportedException("NMAVEN-042-001: Platform not supported: Results = "
                                + results);
                    }
                }

                /**
                 * Determines the vendor based on the path of the executable
                 *
                 * @param path
                 * @return vendor
                 * @throws PlatformUnsupportedException
                 */
                private Vendor getVendorFromPath(String path) throws PlatformUnsupportedException {
                    if (!new File(path).exists()) {
                        throw new PlatformUnsupportedException("NMAVEN-042-002: Unable to locate path: Path = " + path);
                    }

                    if (path.contains("Microsoft.NET")) {
                        return Vendor.MICROSOFT;
                    } else if (path.contains("Mono")) {
                        return Vendor.MONO;
                    } else if (path.contains("Portable.NET")) {
                        return Vendor.DOTGNU;
                    }
                    throw new PlatformUnsupportedException("NMAVEN-042-003: Platform not supported: Path " + path);
                }

                private boolean isEmpty(String value) {
                    return (value == null || value.trim().equals(""));
                }
            };
        }
    }
}
