package org.apache.maven.dotnet.executable.compiler;

public interface KeyInfo
{
    String getKeyFileUri();

    void setKeyFileUri(String keyFileUri);

    String getKeyContainerName();

    void setKeyContainerName(String keyContainerName);

    public static class Factory
    {
        /**
         * Constructor
         */
        private Factory()
        {
        }

        /**
         * Returns a default instance of key info.
         *
         * @return a default instance of key info
         */
        public static KeyInfo createDefaultKeyInfo()
        {
            return new KeyInfo()
            {

                private String keyFileUri;

                private String keyContainerName;

                public String getKeyFileUri() {
                    return keyFileUri;
                }

                public void setKeyFileUri(String keyFileUri) {
                    this.keyFileUri = keyFileUri;
                }

                public String getKeyContainerName() {
                    return keyContainerName;
                }

                public void setKeyContainerName(String keyContainerName) {
                    this.keyContainerName = keyContainerName;
                }

            };
        }
    }

}
