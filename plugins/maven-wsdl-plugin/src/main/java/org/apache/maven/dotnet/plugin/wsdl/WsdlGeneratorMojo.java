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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Proxy;
import org.apache.maven.dotnet.vendor.Vendor;
import org.apache.maven.dotnet.executable.CommandExecutor;
import org.apache.maven.dotnet.executable.ExecutionException;
import org.apache.maven.dotnet.PlatformUnsupportedException;

import java.util.List;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * Generates WSDL class
 *
 * @author Shane Isbell
 * @goal wsdl
 * @phase process-sources
 */
public class WsdlGeneratorMojo extends AbstractMojo {

    /**
     * The directory to place the generated binding classes.
     *
     * @parameter expression="${outputDirectory}" default-value = "${project.build.directory}${file.separator}build-sources"
     * @required
     */
    private String outputDirectory;

    /**
     * Paths (or URLs) of the WSDL files.
     *
     * @parameter expression="${paths}"
     * @required
     */
    private String[] paths;

    /**
     * Language of the WSDL binding. Default value is CS (CSHARP).
     *
     * @parameter expression="${language}" default-value="CS"
     */
    private String language;

    /**
     * Turns on type sharing feature. Not supported for MONO.
     *
     * @parameter expression="${sharetypes}" default-value="false"
     */
    private boolean sharetypes;

    /**
     * Displays extra information when the sharetypes switch is specified. Not supported for MONO.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Generates fields instead of properties.
     *
     * @parameter expression="${fields}" default-value="false"
     */
    private boolean fields;

    /**
     * Generates explicit order identifiers on all particle members. Not supported for MONO.
     *
     * @parameter expression = "${order}" default-value = "false"
     */
    private boolean order;

    /**
     * Generates the INotifyPropertyChanged interface to enable data binding. Not supported for MONO.
     *
     * @parameter expression="${enableDataBinding}" default-value="false"
     */
    private boolean enableDataBinding;

    /**
     * Namespace of the WSDL binding. Default value is ${project.groupId}
     *
     * @parameter expression="${namespace}" default-value="${project.groupId}"
     */
    private String namespace;

    /**
     * Override the default protocol.
     *
     * @parameter expression="${protocol}"
     */
    private String protocol;

    /**
     * The server to retrieve the WSDL from.
     *
     * @parameter
     */
    private org.apache.maven.dotnet.plugin.wsdl.Server server;

    /**
     * The proxy server
     *
     * @parameter
     */
    private org.apache.maven.dotnet.plugin.wsdl.Proxy proxy;

    /**
     * Server values from the settings.xml file.
     *
     * @parameter expression="${settings.servers}"
     * @required
     */
    private List<Server> servers;

    /**
     * Server values from the settings.xml file.
     *
     * @parameter expression="${settings.proxies}"
     * @required
     */
    private List<Proxy> proxies;

    /**
     * @parameter expression="${netHome}"
     */
    private String netHome;

    /**
     * Generates server implementation
     *
     * @parameter expression="${serverInterface}"  default-value ="false"
     */
    private boolean serverInterface;

    /**
     * Tells the plugin to ignore options not appropriate to the vendor.
     *
     * @parameter expresion ="${ignoreUnusedOptions}" default-value="false"
     */
    private boolean ignoreUnusedOptions;

    public void execute() throws MojoExecutionException {
        Vendor vendor = getCompilerVendor();
        List<String> commands = getCommandsFor(vendor);

        getLog().debug("NMAVEN-1300-000: Commands = " + commands.toString());
        CommandExecutor commandExecutor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        //commandExecutor.setLog(getLog());
        try {
            commandExecutor.executeCommand(getExecutableFor(vendor, netHome), commands);
            for (String path : paths) {
                getLog().info("NMAVEN-1300-008: Generated WSDL: File = " + outputDirectory + File.separator +
                        getFileNameFor(path));
            }

        } catch (ExecutionException e) {
            //TODO: This is a hack to get around the fact that MONO returns a result=1 on warnings and MS returns a result=1 on errors.
            //I don't want to fail on MONO warning here.
            if ((vendor.equals(Vendor.MONO) && commandExecutor.getResult() > 1)
                    || vendor.equals(Vendor.MICROSOFT))
                throw new MojoExecutionException("NMAVEN-1300-001: Result = " + commandExecutor.getResult(), e);
        }
    }

    public String getExecutableFor(Vendor vendor, String home) {
        String executable = (vendor.equals(Vendor.MICROSOFT)) ? "wsdl" : "wsdl2";
        return (!isEmpty(home)) ? home + File.separator + "bin" + File.separator + executable : executable;
    }

    public List<String> getCommandsFor(Vendor vendor) throws MojoExecutionException {
        String commandFlag = vendor.equals(Vendor.MICROSOFT) ? "/" : "-";

        List<String> commands = new ArrayList<String>();
        populateServerCommands(commands, commandFlag);
        populateProxyCommands(commands, commandFlag);
        commands.add(commandFlag + "language:" + language);
        commands.add(commandFlag + "namespace:" + namespace);
        commands.add(commandFlag + "fields:" + fields);
        if (!isEmpty(protocol)) commands.add(commandFlag + "protocol:" + protocol);

        for (String path : paths) {
            commands.add(commandFlag + "out:" + outputDirectory + File.separator +
                    getFileNameFor(path));
        }

        for (String path : paths) {
            commands.add(new File(path).getAbsolutePath());
        }

        if (vendor.equals(Vendor.MONO)) {
            if (serverInterface) commands.add("-server");
            if ((fields || enableDataBinding || order || sharetypes || verbose)) {
                if (!ignoreUnusedOptions)
                    throw new MojoExecutionException("NMAVEN-1300-005: Illegal Option(s) for Mono");
                else
                    getLog().warn("NMAVEN-1300-002: Your pom.xml contains an option that is not supported by MONO: Your application" +
                            " artifact will differ dependening on compiler/platform and may have different behavior.");
            }
        } else {
            if (serverInterface) commands.add("/server");
            if (enableDataBinding) commands.add("/enableDataBinding");
            if (sharetypes) commands.add("/sharetypes");
            if (verbose) commands.add("/verbose");
            if (order) commands.add("/order");
        }
        return commands;
    }

    private boolean isURL(String path) {
        try {
            new URL(path);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    private String getFileNameFor(String path) {
        String wsdlName;
        if (isURL(path)) {
            wsdlName = path.substring(path.lastIndexOf("/"), path.length());
        } else {
            if (path.contains("/")) {
                wsdlName = path.substring(path.lastIndexOf("/"), path.length());
            } else if (path.contains("\\")) {
                wsdlName = path.substring(path.lastIndexOf("\\"), path.length());
            } else {
                wsdlName = path;
            }
        }
        return wsdlName.substring(0, wsdlName.lastIndexOf(".")) + "." + language.toLowerCase();
    }

    private void populateProxyCommands(List<String> commands, String commandFlag) throws MojoExecutionException {
        if (proxy != null) {
            Proxy mProxy = getProxyFor(proxy.getId());
            if (mProxy != null) {
                getLog().debug("NMAVEN-1300-003: Found proxy: ID = " + mProxy.getId());
                String username = mProxy.getUsername();
                String password = mProxy.getPassword();
                boolean isHashed = proxy.isHashPassword();
                if (!isEmpty(password) && isHashed) {
                    String alg = proxy.getHashAlg();
                    if (isEmpty(alg)) alg = "SHA1";
                    try {
                        password = new String(MessageDigest.getInstance(alg).digest(password.getBytes()));
                    } catch (NoSuchAlgorithmException e) {
                        throw new MojoExecutionException("NMAVEN-1300-004: No Such Algorithm for hashing the password: " +
                                "Algorithm = " + alg, e);
                    }
                }
                String proxyHost = mProxy.getHost();
                int proxyPort = mProxy.getPort();
                String proxyProtocol = mProxy.getProtocol();

                StringBuffer proxyUrl = new StringBuffer();
                if (!isEmpty(proxyProtocol)) proxyUrl.append(proxyProtocol).append("://");
                if (!isEmpty(proxyHost)) proxyUrl.append(proxyHost);
                if (proxyPort > 0) proxyUrl.append(":").append(proxyPort);
                if (proxyUrl.length() != 0) commands.add(commandFlag + "proxy:" + proxyUrl.toString());
                if (!isEmpty(username)) commands.add(commandFlag + "proxyusername:" + username);
                if (!isEmpty(password)) commands.add(commandFlag + "proxypassword:" + password);
            }
        }
    }

    private void populateServerCommands(List<String> commands, String commandFlag) throws MojoExecutionException {
        if (server != null) {
            Server mServer = getServerFor(server.getId());
            if (mServer != null) {
                String username = mServer.getUsername();
                String password = mServer.getPassword();
                boolean isHashed = server.isHashPassword();
                if (!isEmpty(password) && isHashed) {
                    String alg = server.getHashAlg();
                    if (isEmpty(alg)) alg = "SHA1";
                    try {
                        password = new String(MessageDigest.getInstance(alg).digest(password.getBytes()));
                    } catch (NoSuchAlgorithmException e) {
                        throw new MojoExecutionException("NMAVEN-1300-005: No Such Algorithm for hashing the password: " +
                                "Algorithm = " + alg, e);
                    }
                }
                if (!isEmpty(username)) commands.add(commandFlag + "username:" + username);
                if (!isEmpty(password)) commands.add(commandFlag + "password:" + password);
            }
        }
    }

    private Vendor getCompilerVendor() throws MojoExecutionException {
        Vendor vendor;
        PlatformDetector platformDetector = PlatformDetector.Factory.createDefaultPlatformDetector();
        if (isEmpty(netHome)) {
            try {
                vendor = platformDetector.getVendorFor("wsdl", null);
            } catch (PlatformUnsupportedException e) {
                throw new MojoExecutionException("NMAVEN-1300-009", e);
            }
        } else {
            File file = new File(netHome);
            if (!file.exists()) {
                throw new MojoExecutionException("NMAVEN-1300-006: Unable to locate netHome - make sure that it exists:" +
                        " Home = " + netHome);
            }
            try {
                vendor = platformDetector.getVendorFor(null, new File(file.getAbsolutePath() + File.separator
                        + "bin" + File.separator + "wsdl"));
            } catch (PlatformUnsupportedException e) {
                throw new MojoExecutionException("NMAVEN-1300-010", e);
            }

        }
        getLog().info("NMAVEN-1300-007: WSDL Vendor found: " + vendor.getVendorName());
        return vendor;
    }

    private Proxy getProxyFor(String id) {
        for (Proxy proxy : proxies) {
            if (proxy.getId().trim().equals(id.trim())) return proxy;
        }
        return null;
    }

    private Server getServerFor(String id) {
        for (Server server : servers) {
            if (server.getId().trim().equals(id.trim())) return server;
        }
        return null;
    }

    private boolean isEmpty(String value) {
        return (value == null || value.trim().equals(""));
    }
}
