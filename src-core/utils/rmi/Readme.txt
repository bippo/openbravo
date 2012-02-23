Installation guide for RenderFO:

In the server side:

1.- Edit src/rmi/RenderFo.java, replace:
  -@basepath@: path where RenderFo is installed (log will be written there). A folder named "log" has to be created within that path
  -@RenderFoAddress@: IP address or public DNS name of RenderFO server

2.- Compile both classes (RenderFoI.java and RenderFo.java). 
  -Requirements:
    -jdk1.3 or higher
    -xalan.jar, fop.jar, comm.jar, log4j.jar and rmi/classes (.) in classpath
 
3.- Generate rmi infrastructure. In command line execute: rmic -d ./classes rmi.RenderFo
  It will generate stub and skel classes, to communicate rmi client and server
    -Requirements:
    -jdk1.3 or higher
    -xalan.jar, fop.jar, comm.jar and log4j.jar in classpath
  -Requirements:
    -jdk1.3 or higher and jdk/bin folder in execution path
    -xalan.jar, fop.jar, comm.jar, log4j.jar and rmi/classes (.) in classpath

4.- Create a jar file with generated classes: jar -cvf RenderFoRmi.jar *
  This jar file will be deployed in rmi client

5.- Running the server:
  5.1.- Command line
    start rmiregistry
    java -Djava.rmi.server.hostname=@RenderFoAddress@ -Djava.security.policy=@basepath@\java.policy -Djava.rmi.server.codebase=file:@basepath@\classes/ -cp @basepath@\lib\fop.jar;@basepath@\lib\avalon-framework-cvs-20020806.jar;@basepath@\lib\batik.jar;@basepath@\lib\xalan-2.4.1.jar;@basepath@\lib\xercesImpl-2.2.1.jar;@basepath@\lib\xml-apis.jar;@basepath@\lib\log4j-1.2.8.jar;@basepath@\classes; -Xmx512m -Xms256m rmi.RenderFo
  5.2.- As a service in a windows box. Using ServiceInstaller tool (replace $JAVA_HOME$ variable by its value):
    -5.2.1.- Install rmiRegistry service:
      SERVICE NAME: rmiRegistry
      DISPLAY NAME: rmiRegistry
      STARTUP: manual
      DEPENDENCIES: 
      EXECUTABLE: $JAVA_HOME$\rmiregistry.exe
      ARGUMENTS:
      WORKING DIRECTORY: $JAVA_HOME$

    -5.2.2.- Install RenderFo service:
      a) without proxy
      SERVICE NAME: rmiRenderFo
      DISPLAY NAME: rmiRenderFo
      STARTUP: manual
      DEPENDENCIES: rmiRegistry
      EXECUTABLE: $JAVA_HOME$\java.exe
      ARGUMENTS: -Djava.rmi.server.hostname=@RenderFoAddress@ -Djava.security.policy=c:\rmi\java.policy -Djava.rmi.server.codebase=file:@basepath@\classes/ -cp @basepath@\lib\fop.jar;@basepath@\lib\avalon-framework-cvs-20020806.jar;@basepath@\lib\batik.jar;@basepath@\lib\xalan-2.4.1.jar;@basepath@\lib\xercesImpl-2.2.1.jar;@basepath@\lib\xml-apis.jar;@basepath@\lib\log4j-1.2.8.jar;@basepath@\classes; -Xmx512m -Xms256m rmi.RenderFo
      WORKING DIRECTORY: $JAVA_HOME$

      a) with proxy
      SERVICE NAME: rmiRenderFo
      DISPLAY NAME: rmiRenderFo
      STARTUP: manual
      DEPENDENCIES: rmiRegistry
      EXECUTABLE: $JAVA_HOME$\java.exe
      ARGUMENTS: -Djava.rmi.server.hostname=@RenderFoAddress@ -Dhttp.proxyHost=@ProxyAddress@ -Dhttp.proxyPort=8080 -Djava.security.policy=@basepath@\java.policy -Djava.rmi.server.codebase=@basepath@\classes/ -cp @basepath@\lib\fop.jar;@basepath@\lib\avalon-framework-cvs-20020806.jar;@basepath@\lib\batik.jar;@basepath@\lib\xalan-2.4.1.jar;@basepath@\lib\xercesImpl-2.2.1.jar;@basepath@\lib\xml-apis.jar;@basepath@\lib\log4j-1.2.8.jar;@basepath@\classes; -Xmx512m -Xms256m rmi.RenderFo
      WORKING DIRECTORY: $JAVA_HOME$

In the client side (running in a tomcat server):
1.- Copy renderFoRmi.jar to an accessible path for the execution context (eg. if executing from tomcat, tomcat\common\lib)

2.- Edit web.xml file
  <context-param>
    <param-name>ServidorRenderFo</param-name>
    <param-value>@RenderFoAddress@</param-value>
  </context-param>