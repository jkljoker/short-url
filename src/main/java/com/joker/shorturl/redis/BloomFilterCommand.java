package com.joker.shorturl.redis;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum BloomFilterCommand implements ProtocolCommand {
    BF_ADD("BF.ADD"),
    BF_EXISTS("BF.EXISTS");

    private final byte[] raw;

    BloomFilterCommand(String command) {
        this.raw = SafeEncoder.encode(command);
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }
}
