package org.readutf.engine.team;

import java.util.UUID;
import org.readutf.engine.team.exception.TeamSelectException;

public interface TeamSelector<T extends GameTeam> {

    T getTeam(UUID playerId) throws TeamSelectException;
}
