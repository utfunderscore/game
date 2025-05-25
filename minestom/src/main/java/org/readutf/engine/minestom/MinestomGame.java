package org.readutf.engine.minestom;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.engine.Game;
import org.readutf.engine.GamePlatform;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEventManager;
import org.readutf.engine.task.GameScheduler;
import org.readutf.engine.team.GameTeam;
import org.readutf.engine.team.TeamSelector;

public class MinestomGame<POSITIONS extends BuildFormat> extends Game<Instance, Arena<Instance, POSITIONS>, GameTeam> {

    public MinestomGame(
            @NotNull GamePlatform<Instance> platform,
            @NotNull GameScheduler scheduler,
            @NotNull GameEventManager eventManager,
            @NotNull TeamSelector<GameTeam> teamSelector
    ) {
        super(platform, scheduler, eventManager, teamSelector);
    }
}
