Optical Toolbox (OptTbx)
==========================

A toolbox for optical sensors like the MSI and OLCI and SLSTR instruments on board of ESA's Sentinel-2 and 3 satellites.

The project page of SNAP, and the toolboxes can be found at http://step.esa.int. 
There you can find tutorials, developer guides, a user forum and other interesting things.


Building the OptTbx from the source
-----------------------------------

The following gives a brief introduction how to build the optical Toolbox.
More information can be found in the [Developer Guide](https://senbox.atlassian.net/wiki/display/SNAP/Developer+Guide).


Download and install the required build tools

* Install Java 8 JDK and set JAVA_HOME accordingly. A distribution of OpenJDK is suggested. 
Several distributions are available, for example
  * [Azul Zulu](https://www.azul.com/downloads/zulu-community)  
  * [AdoptOpenJDK](https://adoptopenjdk.net)   
  * [Amazon Corretto](https://aws.amazon.com/de/corretto)	  
* Install Maven and set MAVEN_HOME accordingly. 
* Install git

Add $JAVA_HOME/bin, $MAVEN_HOME/bin to your PATH.

Clone the Optical Toolbox source code and related repositories into a directory referred to a ${snap} from here on

    cd ${snap}
    git clone https://github.com/senbox-org/snap-engine.git
    git clone https://github.com/senbox-org/snap-desktop.git
    git clone https://github.com/senbox-org/optical-toolbox.git
    
Build SNAP-Engine:

    cd ${snap}/snap-engine
    mvn install

Build SNAP-Desktop:

    cd ${snap}/snap-desktop
    mvn install

Build Sentinel-3 Toolbox:

    cd ${snap}/optical-toolbox
    mvn install
   
If unit tests are failing, you can use the following to skip the tests
   
    mvn clean
    mvn install -Dmaven.test.skip=true
	
Setting up IntelliJ IDEA
------------------------

1. Create an empty project with the ${snap} directory as project directory

2. Import the pom.xml files of snap-engine, snap-desktop and optical-toolbox as modules. Ensure **not** to enable
the option *Create module groups for multi-module Maven projects*. Everything can be default values.

3. Set the used JDK for the main project.

4. Use the following configuration to run SNAP in the IDE:

    **Main class:** org.esa.snap.nbexec.Launcher
    **VM parameters:** -Dsun.awt.nopixfmt=true -Dsun.java2d.noddraw=true -Dsun.java2d.dpiaware=false
    All VM parameters are optional
    **Program arguments:**
    --userdir
    "${snap}/optical-toolbox/target/userdir"
    --clusters
    "${snap}/optical-toolbox/optical-kit/target/netbeans_clusters/optical"
    --patches
    "${snap}/snap-engine/$/target/classes;${snap}/opttbx/$/target/classes"
    **Working directory:** ${snap}/snap-desktop/snap-application/target/snap/
    **Use classpath of module:** snap-main

Enjoy developing!


