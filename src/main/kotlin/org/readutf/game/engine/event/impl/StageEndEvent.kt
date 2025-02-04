package org.readutf.game.engine.event.impl

import org.readutf.game.engine.stage.GenericStage

class StageEndEvent(stage: GenericStage) : StageEvent(stage, stage.game)
