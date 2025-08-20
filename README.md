# jesharepoint
Just Enough Sharepoint Library - Just enough Sharepoint functionality for LBU apps

## Purpose
This Java library provides Java applications and interface onto MS Sharepoint sites. At its core is an API onto OData v3 and this is wrapped by Java classes to support a small proportion of [Sharepoint data types](https://learn.microsoft.com/en-us/previous-versions/office/developer/sharepoint-rest-reference/jj860569(v=office.15)). **This is very far from a complete implementation - it implements Just Enough functionality to support the needs of Leeds Beckett University's applications.** 

## Why not just use Apache library Olingo?
Olingo fully supports OData v2 but Sharepoint doesn't have an OData v2 interface. Olingo is working on an OData v4 implementation but that is not stable and Sharepoint's OData v4 interface lacks good documentation and there is little advice available from the developer community. We decided to make use of Sharepoint's OData v3 interface but that will probably never be supported by Olingo.

## What's supported?
* Only a few OData primitive data types are supported. (String, int32, boolean)
* Only some basic actions with Sharepoint files, folders and groups are supported.

## Forking
If you want to create a Java application to use the OData v3 interface to work with a Sharepoint site you might save time by forking this project and extending it. Add just enough functionality for your needs.
