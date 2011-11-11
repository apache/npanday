namespace NPanday.VisualStudio.Addin
{
    /// <summary>
    /// MSG_E_* = ERROR MSGS
    /// MSG_EF_* = ERROR MSGS W/ STRING.FORMAT
    /// MSG_L_* = LOG MSGS
    /// MSG_Q_* = QUESTIONS STATEMENT
    /// MSG_D_* = DISPLAY TEXT(Button, CommnadBar)
    /// MSG_C_* = Caption
    /// </summary>
    public static class Messages
    {
        public const string MSG_E_NOTIMPLEMENTED = "The method or operation is not implemented.";
        public const string MSG_L_NPANDAY_ALREADY_STARTED = "\nNPanday Addin Has Already Started.";
        public const string MSG_L_NPANDAY_ADDIN_STARTED = "\nNPanday Addin {0} Successfully Started (in {1:0.00} seconds).";
        public const string MSG_L_UNABLE_TO_REGISTER_ADD_ARTIFACT_MENU = "\nCould not register the menu for adding artifacts.";
        public const string MSG_L_UNABLE_TO_REGISTER_STOP_BUILD_MENU = "\nCould not register the menu for stopping maven builds.";
        public const string MSG_L_UNABLE_TO_REGISTER_NPANDAY_MENUS = "\nCould not register the default NPanday menus.";
        public const string MSG_L_UNABLE_TO_REGISTER_ALL_PROJECTS_MENU = "\nCould not register the menu for global actions on all projects.";
        public const string MSG_E_NPANDAY_REMOVE_DEPENDENCY_ERROR = "NPanday Remove Dependency Error:";
        public const string MSG_Q_STOP_MAVEN_BUILD = "Do you want to stop the NPanday Build?";
        public const string MSG_EF_NOT_A_PROJECT_POM = "Not A Project Pom Error: {0} is not a project Pom, the pom is a parent pom type.";
        public const string MSG_EF_NOT_THE_PROJECT_POM = "The Pom may not be the project's Pom: Project Name: {0} is not equal to Pom artifactId: {1}";
        public const string MSG_E_PARENTPOM_NOTFOUND = "parent-pom.xml Not Found";//from Parent pom.xml to paren-pom.xml
        public const string MSG_E_EXEC_ERROR = "NPanday Execution Error: ";
        public const string MSG_L_SHUTTING_DOWN_NPANDAY = "\nShutting Down NPanday Visual Studio Addin.";
        public const string MSG_L_SUCCESFULLY_SHUTDOWN = "\nNPanday Successfully Stopped.";//from ShutDown to Stopped
        public const string MSG_D_NPANDAY_BUILD_SYSTEM = "NPanday Build System";
        public const string MSG_T_NPANDAY_BUILDSYSTEM = "Executes the command for NPanday Addin";
        public const string MSG_C_ADD_REFERENCE = "Add &Reference...";
        public const string MSG_C_ADD_WEB_REFERENCE = "Add W&eb Reference...";
        public const string MSG_C_UPDATE_POM_WEB_REFERENCES = "Update POM Web References...";
        public const string MSG_C_CONFIGURE_MAVEN_REPO = "Configure Maven Repository...";
        public const string MSG_C_ADD_MAVEN_ARTIFACT = "Add Maven Artifact...";
        public const string MSG_C_CHANGE_MAVEN_SETTING_XML = "Change Maven settings.xml...";
        public const string MSG_C_SET_COMPILE_SIGN_ASSEMBLY_KEY = "Set NPanday Compile Sign Assembly Key...";
        public const string MSG_C_IMPORT_PROJECT = "Generate Solution's POM Information...";
        public const string MSG_C_STOP_MAVEN_BUILD = "Stop Maven Build";
        public const string MSG_C_MAVEN_PHASE = "Maven Phase";
        public const string MSG_C_CLEAN = "Clean";
        public const string MSG_C_BUILD = "Build [compile]";
        public const string MSG_C_TEST = "Test";
        public const string MSG_C_INSTALL = "Install";
        public const string MSG_C_DEPLOY = "Deploy";
        public const string MSG_C_CLEAN_ALLPROJECT = "All Projects: Clean";
        public const string MSG_C_TEST_ALLPROJECT = "All Projects: Test";
        public const string MSG_C_INSTALL_ALLPROJECT = "All Projects: Install";
        public const string MSG_C_BUILD_CURRENTPROJECT = "Current Project: Build [compile]";
        public const string MSG_C_INSTALL_CURRENTPROJECT = "Current Project: Install";
        public const string MSG_C_BUILD_ALLPROJECT = "All Projects: Build [compile]";
        public const string MSG_C_CLEAN_CURRENTPROJECT = "Current Project: Clean";
        public const string MSG_C_TEST_CURRENTPROJECT = "Current Project: Test";
        public const string MSG_C_RUNUNITTEST = "Run Unit Test/s";
        public const string MSG_C_COMPILEANDRUNTEST = "Compile and Run Unit Test/s";
        public const string MSG_Q_STOPCURRENTBUILD = "A NPanday Build is currently running, Do you want to stop the build and proceed to a new Build Execution?";
        public const string MSG_C_STOPNPANDAYBUILD = "Stop NPanday Build";
        public const string MSG_C_EXEC_ERROR = "Execution Error:";
        public const string MSG_C_ERROR = "Error";
        public const string MSG_C_ALL_PROJECTS = "All NPanday Projects";
        public const string MSG_C_CUR_PROJECT = "Current NPanday Project";
        public const string MSG_D_WEB_REF = "Web References";
        public const string MSG_D_SERV_REF = "Service References";
    }
}