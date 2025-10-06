package org.readutf.engine.feature.spawning;

import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.markers.Position;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.impl.arena.ArenaChangeEvent;
import org.readutf.engine.event.impl.game.GamePlayerAddEvent;
import org.readutf.engine.event.impl.game.GameServerJoinEvent;
import org.readutf.engine.event.impl.stage.StagePreChangeEvent;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.System;
import org.readutf.engine.team.GameTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;


public sealed class SpawningSystem<WORLD, ARENA extends Arena<WORLD, ?>, TEAM extends GameTeam> implements System {

    private static final Logger log = LoggerFactory.getLogger(SpawningSystem.class);

    private final Game<WORLD, ARENA, TEAM> game;
    private final @NotNull SpawnFinder spawnFinder;

    public SpawningSystem(Game<WORLD, ARENA, TEAM> game, @NotNull SpawnFinder spawnFinder) {
        this.game = game;
        this.spawnFinder = spawnFinder;
    }

    public void spawn(UUID playerId) {

        log.info("Spawning player {}", playerId);

        Position position = spawnFinder.find(playerId);
        Arena<WORLD, ?> arena = game.getArena();
        if (position == null) {
            log.warn("No spawn found for player {}", playerId);
            return;
        }
        if(arena == null) {
            log.warn("There is no active arena");
            return;
        }

        game.getPlatform().teleport(playerId, position, arena.getWorld());
    }

    public non-sealed class StageStart extends SpawningSystem {

        public StageStart(Game<?, ?, ?> game, @NotNull SpawnFinder spawnFinder) {
            super(game, spawnFinder);
        }

        @Override
        public @NotNull List<ListenerData> getListeners() {
            return List.of(ListenerData.typed(StagePreChangeEvent.class, listener));
        }

        private final TypedGameListener<StagePreChangeEvent> listener =
                event -> event.getGame().getPlayers().forEach(this::spawn);
    }

    public non-sealed static class ArenaChange extends SpawningSystem {

        public ArenaChange(Game<?, ?, ?> game, @NotNull SpawnFinder spawnFinder) {
            super(game, spawnFinder);
        }

        @Override
        public @NotNull List<ListenerData> getListeners() {
            return List.of(ListenerData.typed(ArenaChangeEvent.class, listener));
        }

        private final TypedGameListener<ArenaChangeEvent> listener =
                event -> event.getGame().getPlayers().forEach(this::spawn);
    }

    public non-sealed static class ServerJoin extends SpawningSystem {

        public ServerJoin(Game<?, ?, ?> game, @NotNull SpawnFinder spawnFinder) {
            super(game, spawnFinder);
        }

        @Override
        public @NotNull List<ListenerData> getListeners() {
            return List.of(ListenerData.typed(GameServerJoinEvent.class, listener));
        }

        private final TypedGameListener<GameServerJoinEvent> listener =
                event -> event.getGame().getPlayers().forEach(this::spawn);
    }
}
