# watchr-jenkins
Watchr is a Java library that processes time-series data and generates HTML graphs.  It is primarily used to track performance of subroutines over time.

This project, watchr-jenkins, is a frontend for Watchr that provides the functionality of data processing and graph generation as a Jenkins plugin.
 
## Building the Plugin
You will need to first build watchr-core:

    git clone git@gitlab-ex.sandia.gov:SEMS/watchr-core.git watchr-core
    cd watchr-core
    mvn clean install

Once watchr-core is available in your local Maven repo, you can build watchr-jenkins:

    git clone git@gitlab-ex.sandia.gov:SEMS/jenkins_performance_plugin.git watchr-jenkins
    cd watchr-jenkins
    mvn clean install
 
After Maven finishes running, you should have a generated HPI file in a new "target" directory.

## Installing the Plugin
You'll need to manually install Watchr to your instance of Jenkins, since it's not in the official Jenkins plugin repos.
 - Navigate to the Plugin Manager in Jenkins.
 - Click on the Advanced tab.
 - Use the "Upload Plugin" file uploader to upload the HPI file generated in the previous section.
 
## Build Troubleshooting
**Non-resolvable parent POM for gov.sandia.watchr:watchr-jenkins**

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
 
Sandia’s pem file is available [**here.**](https://prod.sandia.gov/firefox/bc.pem)

Follow the process for installing the pem file described [**here.**](https://stackoverflow.com/questions/2138940/import-pem-into-java-key-store)

## Documentation

The documentation for watchr-core is [**available in markdown and rendered HTML format.**](https://gitlab-ex.sandia.gov/SEMS/watchr-core/-/tree/master/src/main/resources/docs)