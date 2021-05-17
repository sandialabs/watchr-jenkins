# watchr-jenkins
Watchr is a Java library that processes time-series data and generates HTML graphs.  It is primarily used to track performance of subroutines over time.

This project, watchr-jenkins, is a frontend for Watchr that provides the functionality of data processing and graph generation as a Jenkins plugin.
 
## Building the Plugin
You will need to first build watchr-core:

    git clone https://github.com/sandialabs/watchr-core.git watchr-core
    cd watchr-core
    mvn clean install

Once watchr-core is available in your local Maven repo, you can build watchr-jenkins:

    git clone https://github.com/sandialabs/watchr-jenkins.git watchr-jenkins
    cd watchr-jenkins
    mvn clean install
 
After Maven finishes running, you should have a generated HPI file in a new "target" directory.

## Installing the Plugin
You'll need to manually install Watchr to your instance of Jenkins, since it's not in the official Jenkins plugin repos.
 - Navigate to the Plugin Manager in Jenkins.
 - Click on the Advanced tab.
 - Use the "Upload Plugin" file uploader to upload the HPI file generated in the previous section.

## Documentation

The documentation for watchr-core is [**available in markdown and rendered HTML format.**](https://github.com/sandialabs/watchr-core/tree/main/src/main/resources/docs)
