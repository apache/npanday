package org.apache.maven.dotnet.repository;

import java.net.URI;

public interface Requirement
{
    URI getUri();

    String getValue();

    public static class Factory
    {
        /**
         * Default constructor
         */
        private Factory()
        {
        }

        /**
         * Creates a default implementation of Requirement
         *
         * @return a default implementation of vendor info
         */
        public static Requirement createDefaultRequirement( final URI uri, final String value )
        {
            return new Requirement()
            {
                public URI getUri()
                {
                    return uri;
                }

                public String getValue()
                {
                    return value;
                }
            };
        }
    }
}
