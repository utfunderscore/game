package org.readutf.engine.minestom.system.actionbar;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

@FunctionalInterface
public interface ActionBarProvider {

    public Component getText(Player player);

}
