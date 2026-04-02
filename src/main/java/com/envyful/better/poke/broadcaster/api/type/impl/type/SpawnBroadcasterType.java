package com.envyful.better.poke.broadcaster.api.type.impl.type;

import com.envyful.api.neoforge.world.UtilWorld;
import com.envyful.api.text.Placeholder;
import com.envyful.better.poke.broadcaster.BetterPokeBroadcaster;
import com.envyful.better.poke.broadcaster.api.type.impl.AbstractBroadcasterType;
import com.envyful.better.poke.broadcaster.api.util.BroadcasterUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class SpawnBroadcasterType extends AbstractBroadcasterType<SpawnEvent> {

    public SpawnBroadcasterType() {
        super("spawn", SpawnEvent.class, Pixelmon.EVENT_BUS);
    }

    @Override
    protected boolean isEvent(SpawnEvent spawnEvent) {
        var entity = spawnEvent.action.getOrCreateEntity();

        if (!(entity instanceof PixelmonEntity pixelmon)) {
            return false;
        }

        if (pixelmon.getOwner() != null) {
            return false;
        }

        if (pixelmon.isBossPokemon()) {
            return false;
        }

        return true;
    }

    @Override
    protected PixelmonEntity getEntity(SpawnEvent spawnEvent) {
        return (PixelmonEntity) spawnEvent.action.getOrCreateEntity();
    }

    @Override
    protected Placeholder asEventPlaceholder(SpawnEvent spawnEvent, PixelmonEntity pixelmon, ServerPlayer nearestPlayer) {
        String playerName = nearestPlayer == null ? "None" : nearestPlayer.getName().getString();

        return Placeholder.composition(
                BetterPokeBroadcaster.getConfig().getPlaceholderFormat().getPokemonPlaceholders(pixelmon.getPokemon()),
                Placeholder.simple(line -> line
                        .replace("%nearest_name%", playerName)
                        .replace("%player%", playerName)
                        .replace("%x%", pixelmon.getBlockX() + "")
                        .replace("%y%", pixelmon.getBlockY() + "")
                        .replace("%z%", pixelmon.getBlockZ() + "")
                        .replace("%world%", UtilWorld.getName(pixelmon.level()))
                        .replace("%pokemon%", pixelmon.getPokemonName())
                        .replace("%pokemon_lower%", pixelmon.getPokemonName().toLowerCase().replace(" ", ""))
                        .replace("%biome%", BiomeHelper.getLocalizedBiomeName(pixelmon.level().getBiome(pixelmon.blockPosition())).getString())
                ));
    }

    @Override
    public ServerPlayer findNearestPlayer(SpawnEvent event, PixelmonEntity entity, double range) {
        return (ServerPlayer) entity.level().getNearestPlayer(event.action.spawnLocation.cause, range);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPixelmonSpawn(SpawnEvent event) {
        BroadcasterUtil.handleEvent(event);
    }
}