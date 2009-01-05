package org.apache.maven.plugin.vstudio;

public class SubType
{

    private static String __SubTypeCode = "Code";

    private static String __SubTypeNull = "";

    private static String __SubTypeForm = "Form";

    private static String __SubTypeComponent = "Component";

    private static String __SubTypeAspxCodeBehind = "ASPXCodeBehind";

    private String _type = null;

    private SubType( String subtype )
    {
        _type = subtype;
    }

    public static final SubType Code = new SubType( __SubTypeCode );

    public static final SubType Null = new SubType( __SubTypeNull );

    public static final SubType Form = new SubType( __SubTypeForm );

    public static final SubType AspxCodeBehind = new SubType( __SubTypeAspxCodeBehind );

    public static final SubType Component = new SubType( __SubTypeComponent );

    public String toString()
    {
        return _type;
    }

    public boolean equals( Object arg0 )
    {
        if ( arg0 instanceof SubType && ( (SubType) arg0 )._type.equals( this._type ) )
        {
            return true;
        }
        else if ( arg0 instanceof SubType && ( (SubType) arg0 )._type.equals( "" ) && this._type.equals( "" ) )
        {
            return true;
        }
        return false;
    }
}
