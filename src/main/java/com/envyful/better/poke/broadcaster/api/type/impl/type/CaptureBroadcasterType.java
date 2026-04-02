package com.envyful.better.poke.broadcaster.api.type.impl.type;

import com.envyful.api.text.Placeholder;
import com.envyful.better.poke.broadcaster.BetterPokeBroadcaster;
import com.envyful.better.poke.broadcaster.api.type.impl.AbstractBroadcasterType;
import com.envyful.better.poke.broadcaster.api.util.BroadcasterUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public class CaptureBroadcasterType extends AbstractBroadcasterType<CaptureEvent.SuccessfulCapture> {

    public CaptureBroadcasterType() {
        super("capture", CaptureEvent.SuccessfulCapture.class, Pixelmon.EVENT_BUS);
    }

    @Override
    protected boolean isEvent(CaptureEvent.SuccessfulCapture event) {
        return true;
    }

    @Override
    protected PixelmonEntity getEntity(CaptureEvent.SuccessfulCapture event) {
        return null;
    }

    @Override
    protected Placeholder asEventPlaceholder(CaptureEvent.SuccessfulCapture event, PixelmonEntity pixelmon, ServerPlayer nearestPlayer) {
        var pokemon = event.getPokemon();
        var player = event.getPlayer();
        var level = player != null ? player.level() : null;

        var x = player != null ? player.getBlockX() : 0;
        var y = player != null ? player.getBlockY() : 0;
        var z = player != null ? player.getBlockZ() : 0;

        var worldName = level != null ? com.envyful.api.neoforge.world.UtilWorld.getName(level) : "Unknown";
        var biomeName = level != null ? BiomeHelper.getLocalizedBiomeName(level.getBiome(player.blockPosition())).getString() : "Unknown";
        var playerName = player != null ? player.getName().getString() : "Unknown";

        return BetterPokeBroadcaster.getConfig().getPlaceholderFormat().getPokemonPlaceholders(pokemon,
                Placeholder.simple(line -> line
                        .replace("%nearest_name%", playerName)
                        .replace("%player%", playerName)
                        .replace("%x%", x + "")
                        .replace("%y%", y + "")
                        .replace("%z%", z + "")
                        .replace("%world%", worldName + "")
                        .replace("%pokemon%", pokemon.getSpecies().getName())
                        .replace("%pokemon_lower%", pokemon.getSpecies().getName().toLowerCase().replace(" ", ""))
                        .replace("%biome%", biomeName)));
    }

    @Override
    public ServerPlayer findNearestPlayer(CaptureEvent.SuccessfulCapture event, PixelmonEntity entity, double range) {
        return event.getPlayer();
    }

    @SubscribeEvent
    public void onCapture(CaptureEvent.SuccessfulCapture event) {
        BroadcasterUtil.handleEvent(event);
    }
}