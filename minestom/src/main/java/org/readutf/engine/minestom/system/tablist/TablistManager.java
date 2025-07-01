package org.readutf.engine.minestom.system.tablist;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.feature.System;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TablistManager implements System {

    private static final TablistManager tablistManager = new TablistManager();

    public static TablistManager getTablistManager() {
        return tablistManager;
    }

    private TablistManager() {
        MinecraftServer.getGlobalEventHandler().addListener(playerSpawnEvent);
    }

    private final EventListener<PlayerSpawnEvent> playerSpawnEvent = EventListener.builder(PlayerSpawnEvent.class)
            .handler(event -> {
                for (Player onlinePlayer :
                        MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    updateTablist(onlinePlayer);
                }
            })
            .build();

    @ApiStatus.Experimental
    private final @NotNull EventListener<PlayerPacketOutEvent> tablistUpdate = EventListener.builder(
                    PlayerPacketOutEvent.class)
            .handler(packetOutEvent -> {
                if (!(packetOutEvent.getPacket() instanceof PlayerInfoUpdatePacket infoUpdatePacket)) return;
                EnumSet<PlayerInfoUpdatePacket.@NotNull Action> actions = infoUpdatePacket.actions();
                if (actions.contains(PlayerInfoUpdatePacket.Action.UPDATE_LISTED)
                        || actions.contains(PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER)) {
                    Player viewer = packetOutEvent.getPlayer();
                    updateTablist(viewer);
                }
            })
            .build();

    public void updateTablist(Player viewer) {

        List<PlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            entries.add(getEntry(viewer, onlinePlayer));
        }

        PlayerInfoUpdatePacket packet = new PlayerInfoUpdatePacket(
                EnumSet.of(
                        PlayerInfoUpdatePacket.Action.UPDATE_LISTED, PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER),
                entries);
        viewer.sendPacket(packet);
    }

    public PlayerInfoUpdatePacket.Entry getEntry(Player viewer, Player player) {
        final PlayerSkin skin = player.getSkin();
        List<PlayerInfoUpdatePacket.Property> prop = skin != null
                ? List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()))
                : List.of();

        return new PlayerInfoUpdatePacket.Entry(
                player.getUuid(),
                player.getUsername(),
                prop,
                getVisibility(viewer, player),
                player.getLatency(),
                player.getGameMode(),
                player.getDisplayName(),
                null,
                0);
    }

    public boolean getVisibility(Player viewer, Player player) {
        Game<?, ?, ?> viewerGame = GameManager.getGameByPlayer(viewer.getUuid());
        if (viewerGame == null) {
            return true; // If no game is found, assume visibility
        }
        Game<?, ?, ?> playerGame = GameManager.getGameByPlayer(player.getUuid());
        if (playerGame == null) {
            return false;
        }

        if (viewerGame.equals(playerGame)) {
            TablistSystem tablistSystem = viewerGame.getSystem(TablistSystem.class);
            if (tablistSystem == null) {
                return true; // Default visibility if no visibility system is present
            } else {
                return tablistSystem.getVisibilityHandler().isVisibleToPlayer(viewer.getUuid(), player.getUuid());
            }
        }

        return false; // Default visibility for players not in the same game
    }
}
