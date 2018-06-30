AKREC Server
=================================

Check out the <a href="https://documenter.getpostman.com/view/4318985/RW84rWWK" target="_blank">API documentation</a>.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.getpostman.com/run-collection/fc107bbaed1335885c4c)

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
