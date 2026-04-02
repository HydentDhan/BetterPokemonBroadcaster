package com.envyful.better.poke.broadcaster.api.type.impl.type;

import com.envyful.api.neoforge.world.UtilWorld;
import com.envyful.api.text.Placeholder;
import com.envyful.better.poke.broadcaster.BetterPokeBroadcaster;
import com.envyful.better.poke.broadcaster.api.type.impl.AbstractBroadcasterType;
import com.envyful.better.poke.broadcaster.api.util.BroadcasterUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Map;
import java.util.Objects;

public class DefeatBroadcasterType extends AbstractBroadcasterType<BattleEndEvent> {

    public DefeatBroadcasterType() {
        super("defeat", BattleEndEvent.class, Pixelmon.EVENT_BUS);
    }

    @Override
    protected boolean isEvent(BattleEndEvent event) {
        PixelmonEntity entity = this.getEntity(event);
        if (entity == null) {
            return false;
        }


        if (entity.isBossPokemon()) {
            return false;
        }

        BattleResults result = this.getResult(event, EntityType.PLAYER);
        if (result == null) {
            return false;
        }

        return result == BattleResults.VICTORY;
    }

    public BattleResults getResult(BattleEndEvent event, EntityType<?> entityType) {
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.getResults().entrySet()) {
            if (Objects.equals(entityType, entry.getKey().getEntity().getType())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    protected PixelmonEntity getEntity(BattleEndEvent event) {
        for (BattleParticipant battleParticipant : event.getResults().keySet()) {
            if (battleParticipant instanceof WildPixelmonParticipant wild) {
                return (PixelmonEntity) wild.getEntity();
            }
        }
        return null;
    }

    private ServerPlayer getBattlePlayer(BattleEndEvent event) {
        for (BattleParticipant participant : event.getResults().keySet()) {
            if (participant instanceof PlayerParticipant playerParticipant) {
                return playerParticipant.player;
            }
        }
        return null;
    }

    @Override
    protected Placeholder asEventPlaceholder(BattleEndEvent event, PixelmonEntity pixelmon, ServerPlayer nearestPlayer) {
        ServerPlayer actualPlayer = getBattlePlayer(event);
        String playerName = actualPlayer != null ? actualPlayer.getName().getString() : "Unknown";

        return BetterPokeBroadcaster.getConfig().getPlaceholderFormat().getPokemonPlaceholders(pixelmon.getPokemon(),
                Placeholder.simple(line -> line
                        .replace("%nearest_name%", playerName)
                        .replace("%player%", playerName)
                        .replace("%x%", pixelmon.getBlockX() + "")
                        .replace("%y%", pixelmon.getBlockY() + "")
                        .replace("%z%", pixelmon.getBlockZ() + "")
                        .replace("%world%", UtilWorld.getName(pixelmon.level()) + "")
                        .replace("%pokemon%", pixelmon.getPokemonName())
                        .replace("%pokemon_lower%", pixelmon.getPokemonName().toLowerCase().replace(" ", ""))
                        .replace("%biome%", BiomeHelper.getLocalizedBiomeName(pixelmon.level().getBiome(pixelmon.blockPosition())).getString())));
    }

    @Override
    public ServerPlayer findNearestPlayer(BattleEndEvent event, PixelmonEntity entity, double range) {
        ServerPlayer battlePlayer = getBattlePlayer(event);
        if (battlePlayer != null) {
            return battlePlayer;
        }
        return (ServerPlayer) entity.level().getNearestPlayer(entity, range);
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        BroadcasterUtil.handleEvent(event);
    }
}