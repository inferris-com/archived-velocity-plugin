# Inferris Proxy architecture
The frontend architecture (plugin) runs on the Velocity proxy server software, and is the initial point of interaction to the REST API microservice when the player connects and disconnects.

The plugin also boasts numerous systems and commands. It uses Redis PubSub to send updated player data to backend Paper servers to keep data in sync, and a Paper server can do the same to
ensure this plugin stays updated.

## Overview
When a player connects to the server, the plugin interacts with the RESTful API, which queries the Redis service. If no keys are found, it defaults to retrieving data from the MySQL database. Once data is obtained from either source, the plugin generates and caches the new player data, then submits it via a POST request to the API for storage in both the database and Redis.

### Systems
- REST API integration
- Many staff commands, such as like:
- - /channel
  - /vanish
  - /rank
  - /account
  - /report
  - /viewlogs
  - /friend
  - /shout, /announce
  - /nuke
  - /killinstances
- Friends system
