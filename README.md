# EMODNet Query Tool Web Service

## About

This application serves as a cache for various emodnet GIS features. It is also able to 
calculate some statistics on point and polygon layers.

## Build

Assuming you have maven 3 installed and a Java11 installed run

    $ mvn clean install
    
 This will build the project and create a war file for deployment in a servlet container
 like Tomcat8.
 
## Running
 
 If you do not wish to deploy this app in a serlvet container it can be run stand-alone.
 
    $ java -jar ./server/target/query-tool-SNAPSHOT-1.0.jar
    
## Deployment
To deploy the webapp in a servlet container just copy the into the webapps 
directory of the servlet container.

    $ cp ./webapp/target/query-tool-webapp-SNAPSHOT-1.0.war /var/lib/tomcat8/webapps 
    
## API documentation
 
 Documentation on the REST interface is available from 
 http://localhost:8080/eqt/docs/index.html
