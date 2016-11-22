AKREC Server
=================================

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/b0e50cadbc1e938dc293#?env%5Bspotlight.in.tum.de%5D=W3sia2V5IjoiU0VSVkVSX1VSTCIsInZhbHVlIjoiaHR0cHM6Ly9zcG90bGlnaHQuaW4udHVtLmRlIiwidHlwZSI6InRleHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6IkVYQU1QTEVfUkVNT1ZFX1RPS0VOIiwidmFsdWUiOiJZdW1teSIsInR5cGUiOiJ0ZXh0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJFWEFNUExFX0FERF9UT0tFTiIsInZhbHVlIjoiWXVtbXkiLCJ0eXBlIjoidGV4dCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiUFJPSkVDVF9JRCIsInZhbHVlIjoiNTgzNDU4OWM4ODY5NWQyMTdjMWVlZDFhIiwidHlwZSI6InRleHQiLCJlbmFibGVkIjp0cnVlfV0=)

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
