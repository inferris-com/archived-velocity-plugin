# Inferris Backend
Inferris's backend is a core foundation of the network system. The infrastructure relies on this. This backend module
runs on the BungeeCord gateway instance and is responsible for being the messenger for data requests and data transmissions.
The back-end utilizes 3 data layers: MySQL, Redis, and Caffeine caches. It serializes player data and transmits it through Bungee channels
and Jedis subs, and the front-end receives and deserializes the transmission.

## How it works
Brief description on how it works:
- When a player joins, it first checks to see if they are available in the Redis server. If they are, it caches them into a Caffeine cache.
- If the player is not available in Redis, it checks the MySQL database for information, and then puts them into Redis and then caches the player in a Caffeine cache.
- If not available in the database, it generates the player's data accordingly.
- Once done, it serializes the PlayerData object, which includes ranks, a registry, profile, etc., in raw JSON form.
- With Jedis subscriptions, it then transmits the JSON over the network. A server identification system, which involves the gateway and the Spigot server communicating to each other,
then identifies what Spigot server the player joins through, and transmits the data to that server accordingly.
- The front-end then receives the request, deserializes the PlayerData, and caches it accordingly.
- When a Spigot server needs to update the player, it does the same process vice-versa: it gives the back-end the updated cache, all while keeping Redis and caches synced.


or caching data so other servers can transmit requests
to retrieve and deserialize that data without extra server overhead. The idea is transmitting data and listening for Inferris plugin requests
so it can respond with the correct result, such as returning the player registry in serialized form, or sending a server-wide messages for
the channel system.
