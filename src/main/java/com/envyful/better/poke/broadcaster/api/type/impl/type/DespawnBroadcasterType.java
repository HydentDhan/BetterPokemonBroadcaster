package com.envyful.better.poke.broadcaster.api.type.impl.type;

import com.envyful.api.neoforge.world.UtilWorld;
import com.envyful.api.text.Placeholder;
import com.envyful.better.poke.broadcaster.BetterPokeBroadcaster;
import com.envyful.better.poke.broadcaster.api.type.impl.AbstractBroadcasterType;
import com.envyful.better.poke.broadcaster.api.util.BroadcasterUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DespawnBroadcasterType extends AbstractBroadcasterType<EntityLeaveLevelEvent> {

    private static final Set<UUID> CAPTURED_UUIDS = Collections.newSetFromMap(new LinkedHashMap<UUID, Boolean>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Boolean> eldest) {
            return size() > 100;
        }
    });

    public DespawnBroadcasterType() {
        super("despawn", EntityLeaveLevelEvent.class, NeoForge.EVENT_BUS);
        Pixelmon.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCaptureStart(CaptureEvent.StartCapture event) {
        if (event.getPokemon() != null) {
            CAPTURED_UUIDS.add(event.getPokemon().getUUID());
        }
    }

    @SubscribeEvent
    public void onCaptureFail(CaptureEvent.FailedCapture event) {
        if (event.getPokemon() != null) {
            CAPTURED_UUIDS.remove(event.getPokemon().getUUID());
        }
    }

    @Override
    protected boolean isEvent(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof PixelmonEntity pixelmon)) {
            return false;
        }

        if (pixelmon.isBossPokemon()) {
            return false;
        }

        if (pixelmon.tickCount < 100) {
            return false;
        }

        if (pixelmon.getOwnerUUID() != null || pixelmon.getPokemon().getOwnerPlayerUUID() != null) {
            return false;
        }

        if (CAPTURED_UUIDS.contains(pixelmon.getPokemon().getUUID())) {
            return false;
        }

        if (pixelmon.getHealth() <= 0.0F) {
            return false;
        }

        if (com.pixelmonmod.pixelmon.battles.BattleRegistry.getBattle(pixelmon) != null) {
            return false;
        }

        return event.getEntity().getRemovalReason() == Entity.RemovalReason.DISCARDED ||
                event.getEntity().getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK;
    }

    @Override
    protected PixelmonEntity getEntity(EntityLeaveLevelEvent event) {
        return (PixelmonEntity) event.getEntity();
    }

    @Override
    protected Placeholder asEventPlaceholder(EntityLeaveLevelEvent event, PixelmonEntity pixelmon, ServerPlayer nearestPlayer) {
        return BetterPokeBroadcaster.getConfig().getPlaceholderFormat().getPokemonPlaceholders(pixelmon.getPokemon(),
                Placeholder.simple(line -> line.replace("%nearest_name%", nearestPlayer == null ? "None" : nearestPlayer.getName().getString())
                        .replace("%x%", pixelmon.getBlockX() + "")
                        .replace("%y%", pixelmon.getBlockY() + "")
                        .replace("%z%", pixelmon.getBlockZ() + "")
                        .replace("%world%", UtilWorld.getName(pixelmon.level()) + "")
                        .replace("%pokemon%", pixelmon.getPokemonName())
                        .replace("%pokemon_lower%", pixelmon.getPokemonName().toLowerCase().replace(" ", ""))
                        .replace("%biome%", BiomeHelper.getLocalizedBiomeName(pixelmon.level().getBiome(pixelmon.blockPosition())).getString())));
    }

    @Override
    public ServerPlayer findNearestPlayer(EntityLeaveLevelEvent event, PixelmonEntity entity, double range) {
        return (ServerPlayer) entity.level().getNearestPlayer(entity, range);
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        BroadcasterUtil.handleEvent(event);
    }
}