# Inferris Backend
Inferris's backend is a core foundation of the network system. Not to be confused with the core plugin's backend - this backend module
is responsible for powering the server's Waterfall proxy in a performant way, and caching data so other servers can transmit requests
to retrieve and deserialize that data without extra server overhead. The idea is transmitting data and listening for Inferris plugin requests
so it can respond with the correct result, such as returning the player registry in serialized form, or sending a server-wide messages for
the channel system.
