package pl.margoj.server.implementation.map

import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.metadata.MetadataElement
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.objects.MapObject
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import pl.margoj.mrf.map.serialization.MapData
import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.api.map.PvPStatus
import pl.margoj.server.api.map.Town
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.npc.NpcType
import java.io.File

data class TownImpl(val server: ServerImpl, override val numericId: Int, override val id: String, override val name: String, override val width: Int, override val height: Int, override val collisions: Array<BooleanArray>, val metadata: Collection<MetadataElement>, val objects: Collection<MapObject<*>>, val image: File) : Town
{
    override lateinit var inventory: MapInventoryImpl
    private var npcs_ = ArrayList<Npc>()
    val npc: Collection<Npc> get() = this.npcs_

    @Suppress("LoopToCallChain")
    val margonemCollisionsString: String
        get()
        {
            val collisionsChain = BooleanArray(this.width * this.height)

            for (x in 0..(this.width - 1))
            {
                for (y in 0..(this.height - 1))
                {
                    collisionsChain[x + y * this.width] = this.collisions[x][y]
                }
            }

            val out = StringBuilder()

            var collisionsIndex = 0

            while (collisionsIndex < collisionsChain.size)
            {
                var zerosMultiplier = 0

                zeros_loop@
                while (true)
                {
                    for (zerosShift in 0..5)
                    {
                        if (collisionsIndex + zerosShift >= collisionsChain.size || collisionsChain[collisionsIndex + zerosShift])
                        {
                            break@zeros_loop
                        }
                    }
                    collisionsIndex += 6
                    zerosMultiplier++
                }

                if (zerosMultiplier > 0)
                {
                    while (zerosMultiplier > 27)
                    {
                        out.append('z')
                        zerosMultiplier -= 27
                    }

                    if (zerosMultiplier > 0)
                    {
                        out.append(('_'.toInt() + zerosMultiplier).toChar())
                    }
                }
                else
                {
                    var mask = 0

                    for (p in 0..5)
                    {
                        mask = mask or if (collisionsIndex >= collisionsChain.size) 0 else (if (collisionsChain[collisionsIndex++]) (1 shl p) else 0)
                    }

                    out.append((32 + mask).toChar())
                }
            }

            return out.toString()
        }

    override val pvp: PvPStatus
        get() = when (this.getMetadata(MapPvP::class.java))
        {
            MapPvP.NO_PVP -> PvPStatus.NO_PVP
            MapPvP.CONDITIONAL -> PvPStatus.CONDITIONAL
            MapPvP.ARENAS -> PvPStatus.ARENAS
            MapPvP.UNCONDITIONAL -> PvPStatus.UNCONDITIONAL
            else -> PvPStatus.CONDITIONAL
        }

    @Suppress("UNCHECKED_CAST")
    fun <T : MetadataElement> getMetadata(clazz: Class<T>): T
    {
        val optional = this.metadata.stream().filter { clazz.isInstance(it) }.findAny()

        if (optional.isPresent)
        {
            return optional.get() as T
        }

        return MapData.mapMetadata.values.stream().filter { clazz.isInstance(it.defaultValue) }.findAny().map { it.defaultValue }.get() as T
    }

    fun updateNpcs()
    {
        for (npc in this.npcs_)
        {
            this.server.entityManager.unregisterEntity(npc)
        }
        this.npcs_.clear()

        for (mapObject in this.objects)
        {
            if (mapObject !is NpcMapObject)
            {
                continue
            }

            val script = if (mapObject.script == null) null else this.server.npcScriptParser.getNpcScript(mapObject.script!!)
            val npc = Npc(script, ImmutableLocation(this, mapObject.position.x, mapObject.position.y), NpcType.NPC) // TODO
            npc.loadData()

            npc.takeIf { mapObject.graphics != null }?.graphics = mapObject.graphics!!
            npc.takeIf { mapObject.name != null }?.name = mapObject.name!!
            npc.takeIf { mapObject.level != null }?.level = mapObject.level!!

            this.server.entityManager.registerEntity(npc)
        }
    }

    fun inBounds(point: Point): Boolean
    {
        return point.x >= 0 && point.y >= 0 && point.x < this.width && point.y < this.height
    }

    fun getObject(point: Point): MapObject<*>?
    {
        return this.objects.stream().filter { it.position == point }.findAny().orElse(null)
    }

    override fun toString(): String
    {
        return "TownImpl(id='$id', name='$name', width=$width, height=$height)"
    }
}