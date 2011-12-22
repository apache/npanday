package npanday.assembler;

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

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Provides the information to be included within the assembly. Class can be extended to add additional assembly info
 * parameters.
 *
 * @author Shane Isbell
 */
public class AssemblyInfo
{
    /**
     * Artifact version
     */
    private String version;
    
    /**
     * Informational version (used for snapshot)
     */
    private String informationalVersion;

    /**
     * Artifact description
     */
    private String description;

    /**
     * Artifact title
     */
    private String title;

    /**
     * Artifact company
     */
    private String company;

    /**
     * Artifact company
     */
    private String product;

    /**
     * Artifact copyright
     */
    private String copyright;

    /**
     * Artifact trademark
     */
    private String trademark;

    /**
     * Artifact culture
     */
    private String culture;

    /**
     * Artifact configuration
     */
    private String configuration;

    private String keyName;

    private File keyFile;

    private TargetFramework targetFramework;

    private Map<String, String> customStringAttributes;
    
    private static final Map<String, String> EMPTY_CUSTOM_STRING_ATTRIBUTES = Collections.emptyMap();;
    
    /**
     * Default constructor
     */
    public AssemblyInfo()
    {
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "Version: " ).append( version )
            .append( "\r\nInformationalVersion: " ).append( informationalVersion )
            .append( "\r\nDescription: " ).append( description )
            .append( "\r\nTitle: " ).append( title )
            .append( "\r\nCompany; " ).append( company )
            .append( "\r\nProduct: " ).append( product )
            .append( "\r\nCopyright: " ).append( copyright )
            .append( "\r\nTrademark: " ).append( trademark )
            .append( "\r\nCulture: " ).append( culture )
            .append( "\r\nConfiguration: " ).append( configuration )
            .append( "\r\nTargetFramework: " ).append( targetFramework );
        return sb.toString();
    }

    /**
     * Returns the key name.
     *
     * @return the key name
     */
    public String getKeyName()
    {
        return keyName;
    }

    public void setKeyName( String keyName )
    {
        this.keyName = keyName;
    }

    public File getKeyFile()
    {
        return keyFile;
    }

    public void setKeyFile( File keyFile )
    {
        this.keyFile = keyFile;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }
    
    public String getInformationalVersion()
    {
        return informationalVersion;
    }

    public void setInformationalVersion( String informationalVersion )
    {
        this.informationalVersion = informationalVersion;
    }


    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany( String company )
    {
        this.company = company;
    }

    public String getProduct()
    {
        return product;
    }

    public void setProduct( String product )
    {
        this.product = product;
    }

    public String getCopyright()
    {
        return copyright;
    }

    public void setCopyright( String copyright )
    {
        this.copyright = copyright;
    }

    public String getTrademark()
    {
        return trademark;
    }

    public void setTrademark( String trademark )
    {
        this.trademark = trademark;
    }

    public String getCulture()
    {
        return culture;
    }

    public void setCulture( String culture )
    {
        this.culture = culture;
    }

    public String getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( String configuration )
    {
        this.configuration = configuration;
    }

    public void setTargetFramework( TargetFramework targetFramework )
    {
        this.targetFramework = targetFramework;
    }

    public TargetFramework getTargetFramework()
    {
        return targetFramework;
    }

    public Map<String, String> getCustomStringAttributes()
    {
        return (customStringAttributes != null) ? customStringAttributes : EMPTY_CUSTOM_STRING_ATTRIBUTES;
    }

    public void setCustomStringAttributes(Map<String, String> attributes)
    {
        this.customStringAttributes = attributes;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final AssemblyInfo that = (AssemblyInfo) o;

        if ( company != null ? !company.equals( that.company ) : that.company != null )
        {
            return false;
        }
        if ( configuration != null ? !configuration.equals( that.configuration ) : that.configuration != null )
        {
            return false;
        }
        if ( copyright != null ? !copyright.equals( that.copyright ) : that.copyright != null )
        {
            return false;
        }
        if ( culture != null ? !culture.equals( that.culture ) : that.culture != null )
        {
            return false;
        }
        if ( description != null ? !description.equals( that.description ) : that.description != null )
        {
            return false;
        }
        if ( product != null ? !product.equals( that.product ) : that.product != null )
        {
            return false;
        }
        if ( title != null ? !title.equals( that.title ) : that.title != null )
        {
            return false;
        }
        if ( trademark != null ? !trademark.equals( that.trademark ) : that.trademark != null )
        {
            return false;
        }
        if ( version != null ? !version.equals( that.version ) : that.version != null )
        {
            return false;
        }
        if ( targetFramework != null ? !targetFramework.equals( that.targetFramework ) : that.targetFramework != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = ( version != null ? version.hashCode() : 0 );
        result = 29 * result + ( informationalVersion != null ? informationalVersion.hashCode() : 0 );
        result = 29 * result + ( description != null ? description.hashCode() : 0 );
        result = 29 * result + ( title != null ? title.hashCode() : 0 );
        result = 29 * result + ( company != null ? company.hashCode() : 0 );
        result = 29 * result + ( product != null ? product.hashCode() : 0 );
        result = 29 * result + ( copyright != null ? copyright.hashCode() : 0 );
        result = 29 * result + ( trademark != null ? trademark.hashCode() : 0 );
        result = 29 * result + ( culture != null ? culture.hashCode() : 0 );
        result = 29 * result + ( configuration != null ? configuration.hashCode() : 0 );
        result = 29 * result + ( targetFramework != null ? targetFramework.hashCode() : 0 );
        return result;
    }

    public static class TargetFramework
    {
        /**
         * Target framework name
         */
        private String frameworkName;

        /**
         * Target framework display name
         */
        private String frameworkDisplayName;

        public void setFrameworkName(String frameworkName)
        {
            this.frameworkName = frameworkName;
        }

        public String getFrameworkName()
        {
            return this.frameworkName;
        }

        public void setFrameworkDisplayName(String frameworkDisplayName)
        {
            this.frameworkDisplayName = frameworkDisplayName;
        }

        public String getFrameworkDisplayName()
        {
            return this.frameworkDisplayName;
        }

        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            final TargetFramework that = (TargetFramework) o;
            if ( frameworkName != null ? !frameworkName.equals( that.frameworkName ) : that.frameworkName != null )
            {
                return false;
            }
            if ( frameworkDisplayName != null ? !frameworkDisplayName.equals( that.frameworkDisplayName ) : that.frameworkDisplayName != null )
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int result;
            result = ( frameworkName != null ? frameworkName.hashCode() : 0 );
            result = 29 * result + ( frameworkDisplayName != null ? frameworkDisplayName.hashCode() : 0 );
            return result;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append( "FrameworkName: " ).append( frameworkName )
                .append( "\r\nFrameworkDisplayName: " ).append( frameworkDisplayName );
            return sb.toString();
        }

    }

}
