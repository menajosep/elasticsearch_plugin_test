# hatch-search-plugin

## Installation of the plugin

Ways to install the plugin:
* Passing the URL of the plugin (ZIP archive):
           bin/elasticsearch-plugin install
           http://mywoderfulserve.com/plugins/awesome-plugin.zip
* Using the install parameter with the GitHub repository of the plugin. The install parameter, that must be given, is formatted in this way:
           <username>/<repo>[/<version>]
* Manually(in case we can't have it online): the steps required to manually install a plugin are:
  * Copy your zip file in the plugins directory in your Elasticsearch home installation
  * If the directory named plugins doesn't exist, create it
  * Unzip the content of the plugin in the plugins directory
  * Remove the zip archive to clean up unused files

**If a plugin is corrupted or broken, the server doesn't start**

Nodes must be configured to fire up only if HATCH plugins are installed and available. To achieve this behavior, provide the plugin.mandatory directive in the elasticsearch.yml configuration file:

           plugin.mandatory: hatch

There are also some hints to remember while installing plugins.
Updating some plugins in a node environment can bring malfunction due to different plugin versions in different nodes. If you have a big cluster for safety, it's better to check for updates in a separate environment to prevent problems.
To prevent that updating an Elasticsearch version server could also break your custom binary plugins due to some internal API changes, in Elasticsearch 5.x the plugins need to have the same version of Elasticsearch server in their manifest.

## Removing a plugin

Ways to install the plugin:
* Automatically(using the plugins tool):
  1. Stop your running node to prevent exceptions caused due to removal of a file.
  1. Using the Elasticsearch plugin manager, which comes with its script wrapper (plugin).
           elasticsearch-plugin remove hatch
  1. Restart the server
* Manually(in case there are undeletable files):
  1. Go into the plugins directory
  1. Remove the directory with your plugin name
  
## Local test
Run local copy of elastic on docker:
```
docker run -p 9200:9200 -p 9300:9300 \
    -v /Users/jose.mena/dev/HATCH/hatch-search-plugin/target/:/plugin elasticsearch:5.1.1
```
