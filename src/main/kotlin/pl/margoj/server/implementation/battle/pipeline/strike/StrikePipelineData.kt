package pl.margoj.server.implementation.battle.pipeline.strike

import pl.margoj.server.implementation.battle.ability.fight.StrikeAbility
import pl.margoj.server.implementation.battle.pipeline.BattlePipelineData

class StrikePipelineData(val strikeAbility: StrikeAbility) : BattlePipelineData(strikeAbility)
{
    var armorPhysicalReduction = 0

    var blocked = false
    var blockedPhysicalAmount = 0

    var evaded = false

    var physicalDamage = 0

    override fun toString(): String
    {
        return "StrikePipelineData(strikeAbility=$strikeAbility, armorPhysicalReduction=$armorPhysicalReduction, blocked=$blocked, blockedPhysicalAmount=$blockedPhysicalAmount, evaded=$evaded, physicalDamage=$physicalDamage)"
    }
}