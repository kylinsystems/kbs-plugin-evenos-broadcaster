# KBS Plugin : Evenos Broadcaster Dashboard Panel

The fork from https://bitbucket.org/evenos-consulting/org.evenos.broadcaster, and converted to be installed via **KBS ObjectData Tool** 

Refer to https://wiki.idempiere.org/en/Plugin:_Broadcaster_Dashboard_Panel

## How to install

1. Install **KBS ObjectData Tool** (refer to http://wiki.idempiere.org/en/Plugin:_ObjectDataTool)

2. Install the plugin via Apache Felix Web Console
   1. Two bundles to be installed one by one.
   2. Bundle 1 : org.evenos.broadcaster (it is fragment bundle, please check it in Felix, try 'refresh package import' operation in console if status is not 'Fragment')
   3. Bundle 2 : org.evenos.broadcaster.events (ODT Package and ODT2Pack data)

## How to use

Desktop Home -> Broadcaster



