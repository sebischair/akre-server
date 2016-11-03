This is a new Play
=================================

## Core dependencies  
Apache UIMA  
MongoDB  

## Deploying the application
Run run.bat  
Ensure mongodb is running on port 27017  
  User: "guest" with password: "guest" has access to "akrec" collection
  
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
