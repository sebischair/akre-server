AKREC Server
=================================

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/b0e50cadbc1e938dc293#?env%5Bspotlight.in.tum.de%5D=W3sidHlwZSI6InRleHQiLCJlbmFibGVkIjp0cnVlLCJrZXkiOiJTRVJWRVJfVVJMIiwidmFsdWUiOiJodHRwczovL3Nwb3RsaWdodC5pbi50dW0uZGUifSx7InR5cGUiOiJ0ZXh0IiwiZW5hYmxlZCI6dHJ1ZSwia2V5IjoiRVhBTVBMRV9SRU1PVkVfVE9LRU4iLCJ2YWx1ZSI6Ill1bW15In0seyJ0eXBlIjoidGV4dCIsImVuYWJsZWQiOnRydWUsImtleSI6IkVYQU1QTEVfQUREX1RPS0VOIiwidmFsdWUiOiJZdW1teSJ9LHsidHlwZSI6InRleHQiLCJlbmFibGVkIjp0cnVlLCJrZXkiOiJQUk9KRUNUX0lEIiwidmFsdWUiOiI1ODM0NTg5Yzg4Njk1ZDIxN2MxZWVkMWEifV0=)

## Core dependencies
Apache UIMA
MongoDB

## Deploying the application
Run run.bat
Ensure mongodb is running on port 27017
  User: "guest" with password: "guest" has access to "akrec" collection

## Configuration
1. Rename `application.local.conf.back` to `application.local.conf`
1. Fill in database credentials
1. `morphia.db.name` is the database that stores SyncPipes related information
1. `morphia.amelie.db.name` is the database that sotres projects, issues, etc.

## Docker support
1. Change the database configurations according to the instructions in `application.local.conf`
2. Start the complete application stack using `docker-compose up`

### To deploy all related subprojects related to AMELIE
1. Copy docker-compose-all.yml
2. Rename it to docker-compose.yml
3. Start the complete application stack using `docker-compose up`

## MongoDB settings with auth

### Create a guest user for syncpipes collection
> use akrec

> db.createUser( { user: "guest", pwd: "guest", roles: [ { role: "clusterAdmin", db: "admin" }, { role: "userAdminAnyDatabase", db: "admin" }, {role: "readWrite", db: "admin"}, "readWrite" ] } );

### Mongo Config file for mongo service
```json
systemLog:
    destination: file
    path: c:\data\log\mongod.log
storage:
    dbPath: c:\data\db
security:
    authorization: enabled
```
