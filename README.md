# Watchr
Watchr is a Jenkins plugin that reads build-generated performance reports and displays the data contained in these performance reports as a series of graphs.  This plugin is ideal to use for tracking the performance of subroutines over many individual builds.
 
## Building the Plugin
 - Check out this repo.
 - At the root of your repo, run "mvn clean install" (without quotes).  Note that because this is a Mavenized project, you will need Maven 3.X to build this project.
 - If you are using findbugs and it prevents you from building, add -Dfindbugs.skip=true (if using Powershell, then write as -D"findbugs.skip"=true) to the end of your Maven command.
 - After Maven finishes running, you should have a generated HPI file in a new "target" directory.

## Installing the Plugin
You'll need to manually install Watchr to your instance of Jenkins, since it's not in the official Jenkins plugin repos.
 - Navigate to the Plugin Manager in Jenkins.
 - Click on the Advanced tab.
 - Use the "Upload Plugin" file uploader to upload the HPI file generated in the previous section.
 
## Build Troubleshooting
**Non-resolvable parent POM for gov.sandia.sems.jenkins:sems-jpp-plugin**

If you receive this error, and Maven fails to reach any of its repos, the culprit is usually Sandia's network proxy.  Go to conf/settings.xml in your Maven install directory, and add the following section:

    <proxies>
        <proxy>
            <active>true</active>
            <protocol>http</protocol>
            <host>wwwproxy.sandia.gov</host>
            <port>80</port>
        </proxy>
    </proxies>

**sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target**

If you receive this error message, you need to put the Sandia bc.pem file into your Java certificate keystore.
 
Sandia’s pem file is available here:  https://prod.sandia.gov/firefox/bc.pem

Follow the process for installing the pem file described here:  https://stackoverflow.com/questions/2138940/import-pem-into-java-key-store