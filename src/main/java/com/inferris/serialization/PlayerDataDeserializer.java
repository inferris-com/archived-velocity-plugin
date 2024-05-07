package com.inferris.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.player.Channels;
import com.inferris.player.PlayerData;
import com.inferris.player.Profile;
import com.inferris.player.coins.Coins;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.server.Server;

import java.io.IOException;
import java.util.UUID;

public class PlayerDataDeserializer extends JsonDeserializer<PlayerData> {

    @Override
    public PlayerData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        //ObjectMapper objectMapper = createObjectMapper(new SerializationModule());

        JsonNode registryNode = objectMapper.readTree(jsonParser);

        UUID uuid = UUID.fromString(registryNode.get("uuid").asText());
        String username = registryNode.get("username").asText();
        Rank rank = objectMapper.treeToValue(registryNode.get("rank"), Rank.class);
        Profile profile = objectMapper.treeToValue(registryNode.get("profile"), Profile.class);
        Coins coins = objectMapper.treeToValue(registryNode.get("coins"), Coins.class);
        Channels channel = objectMapper.treeToValue(registryNode.get("channel"), Channels.class);
        VanishState vanishState = objectMapper.treeToValue(registryNode.get("vanishState"), VanishState.class);
        Server currentServer = objectMapper.treeToValue(registryNode.get("currentServer"), Server.class);

        return new PlayerData(uuid, username, rank, profile, coins, channel, vanishState, currentServer);
    }
}