package org.readutf.game.engine.event.impl

import org.readutf.game.engine.stage.GenericStage

class StageStartEvent(stage: GenericStage, previousStage: GenericStage?) : StageEvent(stage, stage.game)
