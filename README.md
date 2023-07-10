[![pipeline status](https://gitlab.com/senbox-org/optical-toolbox/badges/master/pipeline.svg)](https://gitlab.com/senbox-org/optical-toolbox/-/commits/master)
[![Quality Gate Status](https://sonarqube.snap-ci.ovh/api/project_badges/measure?project=eu.esa.opt%3Aoptical-toolbox&metric=alert_status&token=sqb_c72cab652839333ca3df7349feec4e6ac3021d7e)](https://sonarqube.snap-ci.ovh/dashboard?id=eu.esa.opt%3Aoptical-toolbox)
[![coverage report](https://gitlab.com/senbox-org/optical-toolbox/badges/master/coverage.svg)](https://gitlab.com/senbox-org/optical-toolbox/-/commits/master)

Optical Toolbox (OptTbx)
==========================

A toolbox for optical sensors like the MSI and OLCI and SLSTR instruments on board of ESA's Sentinel-2 and 3 satellites.

The project page of SNAP, and the toolbox can be found at http://step.esa.int.
There you can find tutorials, developer guides, a user forum and other interesting things.


Building the OptTbx from the source
-----------------------------------

The following gives a brief introduction how to build the optical Toolbox.
More information can be found in the [Developer Guide](https://senbox.atlassian.net/wiki/display/SNAP/Developer+Guide).

Download and install the required build tools

* Install Java 11 JDK and set JAVA_HOME accordingly. A distribution of OpenJDK is suggested.
  Several distributions are available, for example
    * [AdoptOpenJDK](https://adoptopenjdk.net) (recommended)
    * [Azul Zulu](https://www.azul.com/downloads/zulu-community)
    * [Amazon Corretto](https://aws.amazon.com/de/corretto)
* Install Maven and set MAVEN_HOME accordingly.
* Install git

Add $JAVA_HOME/bin, $MAVEN_HOME/bin to your PATH.

Clone the Optical Toolbox source code and related repositories into a directory referred to ${snap} from here on

    cd ${snap}
    git clone https://github.com/senbox-org/optical-toolbox.git

Build Optical Toolbox:

    cd ${snap}/optical-toolbox
    mvn install

Optionally checkout the code for snap-engine and snap-desktop

    cd ${snap}
    git clone https://github.com/senbox-org/snap-engine.git
    git clone https://github.com/senbox-org/snap-desktop.git

Build SNAP-Engine:

    cd ${snap}/snap-engine
    mvn install

Build SNAP-Desktop:

    cd ${snap}/snap-desktop
    mvn install

If unit tests are failing for one of the projects (which should not happen), you can use the following to skip the tests

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
   "${snap}/optical-toolbox/opttbx-kit/target/netbeans_clusters/optical"
   --patches
   "${snap}/snap-engine/$/target/classes;${snap}/optical-toolbox/$/target/classes"
   **Working directory:** ${snap}/snap-desktop/snap-application/target/snap/
   **Use classpath of module:** snap-main

Enjoy developing!


