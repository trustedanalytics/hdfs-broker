hdfs-broker
===========

Cloud foundry broker for HDFS.

# How to use it?
To use hdfs-broker, you need to build it from sources configure, deploy, create instance and bind it to your app. Follow steps described below. 

## Build 
Run command for compile and package.: 
```
mvn clean package
```

## Kerberos configuration
Broker automatically bind to an existing kerberos provide service. This will provide default kerberos configuration, for REALM and KDC host. Before deploy check:

- if kerberos service does not exists in your space, you can create it with command:
```
cf cups kerberos-service -p '{ "kdc": "kdc-host", "kpassword": "kerberos-password", "krealm": "kerberos-realm", "kuser": "kerberos-user" }'
```

- if kerberos-service exists in your space, you can update it with command:
```
cf uups kerberos-service -p '{ "kdc": "kdc-host", "kpassword": "kerberos-password", "krealm": "kerberos-realm", "kuser": "kerberos-user" }'
```

## Deploy 
Push broker binary code to cloud foundry (use cf client).:
```
cf push hdfs-broker -p target/hdfs-broker-*.jar -m 512M -i 1 --no-start
```

## Configure
For strict separation of config from code (twelve-factor principle), configuration must be placed in environment variables.
 
Hdfs-broker can be configured to work with hadoop in secure mode (authentication by Kerberos, value of hadoop.security.authentication property to "kerberos"),
called further **secure profile** configuration, or with hadoop in insecure mode (value of hadoop.security.authentication property to "simple"), 
**insecure profile** configuration.

Broker configuration params list (environment properties):
* obligatory :t
  * USER_PASSWORD - password to interact with service broker
* optional :
  * BASE_GUID - base id for catalog plan creation (uuid)
  * BROKER_PATH - path where broker related folders and data will be located on HDFS (default: /cf/broker). **Don't put / at the end.** After setting BROKER_PATH to ```/example/broker``` hdfs-broker will be configured to store service instances and bindings metadata at ```/example/broker/metadata```, and to provision directories for user's applications at ```/example/broker/userspace```.
  * CF_CATALOG_SERVICENAME - service name in cloud foundry catalog (default: hdfs)
  * CF_CATALOG_SERVICEID - service id in cloud foundry catalog (default: hdfs)
  * HADOOP_PROVIDED_PARAMS - list of hadoop configuration parameters exposed by service (json format, default: {})

### Injection of HDFS client configuration
HDFS client configuration must be set via HADOOP_PROVIDED_ZIP environment variable. Hadoop configuration has to be zip of directory containing hdfs configuration (*-site.xml files).

You can downlad it directly from CDH manager:
```
wget http://<cloudera_manager_host_name>:7180/cmf/services/3/client-config
```

You can prepare this configuration manually and use cf client,  
```
cf se hdfs-broker HADOOP_PROVIDED_ZIP `cat hdfs-clientconfig.zip | base64 | tr -d '\n'`
```

## Start  service broker application

Use cf client :
```
cf start  hdfs-broker
```

## Create new service instance 
  
Use cf client : 
```
cf create-service-broker hdfs-broker <user> <password> https://hdfs-broker.<platform_domain>
cf enable-service-access hdfs
cf cs hdfs shared hdfs-instance
```

## Binding broker instance

Broker instance can be bind with cf client :
```
cf bs <app> hdfs-instance
```
or by configuration in app's manifest.yml : 
```yaml
  services:
    - hdfs-instance
```

To check if broker instance is bound, use cf client : 
```
cf env <app>
```
and look for : 
```yaml
  "hdfs": [
   {
    "credentials": {
     "HADOOP_CONFIG_KEY": {
      "fs.defaultFS": "hdfs://ip-10-10-9-164.us-west-2.compute.internal:8020",
      "hadoop.security.authentication": "simple",
      "hadoop.security.authorization": "false"
     },
     "HADOOP_CONFIG_ZIP": {
      "description": "This is the encoded zip file of hadoop-configuration",
      "encoded_zip": "<base64 of configuration>"
     },
     "fs.defaultFS": "hdfs://ip-10-10-9-164.us-west-2.compute.internal:8020",
     "kerberos": {
      "kdc": "ip-10-10-9-198.us-west-2.compute.internal",
      "krealm": "US-WEST-2.COMPUTE.INTERNAL"
     },
     "uri": "hdfs://ip-10-10-9-164.us-west-2.compute.internal:8020/cf/broker/instances/46f285c5-638e-4e30-9d68-7690928a8a29/"
    },
    "label": "hdfs",
    "name": "hdfs",
    "plan": "shared",
    "tags": []
   }
  ]
```
in VCAP_SERVICES.

## Useful links

Cloud foundry resources that are helpful when troubleshooting service brokers : 
 * http://docs.cloudfoundry.org/services/api.html
 * http://docs.cloudfoundry.org/devguide/services/managing-services.html#update_service
 * http://docs.cloudfoundry.org/services/access-control.html

## On the app side

For spring applications use https://github.com/trustedanalytics/hadoop-spring-utils. 

For regular java applications use https://github.com/trustedanalytics/hadoop-utils. 
