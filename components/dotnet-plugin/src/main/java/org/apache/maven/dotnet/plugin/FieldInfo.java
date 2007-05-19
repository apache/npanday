package org.apache.maven.dotnet.plugin;

public interface FieldInfo
{

    String getName();

    Object getValue();

    /**
     * Provides factory methods for field infos.
     */
    public static class Factory
    {
        /**
         * Default constructor
         */
        private Factory()
        {
        }

        /**
         * Creates a default implementation of field info.
         */
        public static FieldInfo createFieldInfo( final String name, final Object value )
        {
            return new FieldInfo()
            {
                public String getName()
                {
                    return name;
                }

                public Object getValue()
                {
                    return value;
                }
            };
        }
    }
}
