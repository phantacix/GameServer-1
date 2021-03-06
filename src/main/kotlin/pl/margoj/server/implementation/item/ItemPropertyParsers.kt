package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperty
import pl.margoj.mrf.item.ItemRarity
import pl.margoj.mrf.item.properties.*
import pl.margoj.mrf.item.properties.special.*
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject

interface ItemPropertyParser<T, P : ItemProperty<T>>
{
    val propertyType: Class<P>

    fun apply(property: P, itemObject: ItemObject, value: T, itemStack: ItemStack, item: ItemImpl)

    companion object
    {
        val ALL = listOf<ItemPropertyParser<*, *>>(
                StringPropertyParser(),
                IntPropertyParser(),
                IntRangePropertyParser(),
                DoublePropertyParser(),
                LongPropertyParser(),
                BooleanPropertyParser(),
                NamePropertyParser(),
                CategoryPropertyParser(),
                RarityPropertyParser(),
                IconPropertyParser(),
                PricePropertyParser(),
                ProfessionRequirementPropertyParser(),
                CooldownPropertyParser(),
                TeleportPropertyParser()
        )
    }
}

class StringPropertyParser : ItemPropertyParser<String, StringProperty>
{
    override val propertyType: Class<StringProperty> = StringProperty::class.java

    override fun apply(property: StringProperty, itemObject: ItemObject, value: String, itemStack: ItemStack, item: ItemImpl)
    {
        if (value == property.default)
        {
            return
        }

        itemObject.statistics += "${property.propertyName}=$value;"
    }
}

class IntPropertyParser : ItemPropertyParser<Int, IntProperty>
{
    override val propertyType: Class<IntProperty> = IntProperty::class.java

    override fun apply(property: IntProperty, itemObject: ItemObject, value: Int, itemStack: ItemStack, item: ItemImpl)
    {
        if (value == property.default)
        {
            return
        }
        itemObject.statistics += "${property.propertyName}=$value;"
    }
}

class IntRangePropertyParser : ItemPropertyParser<IntRange, IntRangeProperty>
{
    override val propertyType: Class<IntRangeProperty> = IntRangeProperty::class.java

    override fun apply(property: IntRangeProperty, itemObject: ItemObject, value: IntRange, itemStack: ItemStack, item: ItemImpl)
    {
        if (value == property.default)
        {
            return
        }

        itemObject.statistics += "${property.propertyName}=${value.first}-${value.endInclusive}"
    }
}

class DoublePropertyParser : ItemPropertyParser<Double, DoubleProperty>
{
    override val propertyType: Class<DoubleProperty> = DoubleProperty::class.java

    override fun apply(property: DoubleProperty, itemObject: ItemObject, value: Double, itemStack: ItemStack, item: ItemImpl)
    {
        if (value == property.default)
        {
            return
        }

        itemObject.statistics += "${property.propertyName}=$value;"
    }
}

class LongPropertyParser : ItemPropertyParser<Long, LongProperty>
{
    override val propertyType: Class<LongProperty> = LongProperty::class.java

    override fun apply(property: LongProperty, itemObject: ItemObject, value: Long, itemStack: ItemStack, item: ItemImpl)
    {
        if (value == property.default)
        {
            return
        }
        itemObject.statistics += "${property.propertyName}=$value;"
    }
}

class BooleanPropertyParser : ItemPropertyParser<Boolean, BooleanProperty>
{
    override val propertyType: Class<BooleanProperty> = BooleanProperty::class.java

    override fun apply(property: BooleanProperty, itemObject: ItemObject, value: Boolean, itemStack: ItemStack, item: ItemImpl)
    {
        if (value)
        {
            itemObject.statistics += property.propertyName + ";"
        }
    }
}

class NamePropertyParser : ItemPropertyParser<String, NameProperty>
{
    override val propertyType: Class<NameProperty> = NameProperty::class.java

    override fun apply(property: NameProperty, itemObject: ItemObject, value: String, itemStack: ItemStack, item: ItemImpl)
    {
        itemObject.name = value
    }
}

class CategoryPropertyParser : ItemPropertyParser<ItemCategory, CategoryProperty>
{
    override val propertyType: Class<CategoryProperty> = CategoryProperty::class.java

    override fun apply(property: CategoryProperty, itemObject: ItemObject, value: ItemCategory, itemStack: ItemStack, item: ItemImpl)
    {
        itemObject.itemCategory = value.margoId
    }
}

class RarityPropertyParser : ItemPropertyParser<ItemRarity, RarityProperty>
{
    override val propertyType: Class<RarityProperty> = RarityProperty::class.java

    override fun apply(property: RarityProperty, itemObject: ItemObject, value: ItemRarity, itemStack: ItemStack, item: ItemImpl)
    {
        if (value.statType != null)
        {
            itemObject.statistics += "${value.statType};"
        }
    }
}

class IconPropertyParser : ItemPropertyParser<String, IconProperty>
{
    override val propertyType: Class<IconProperty> = IconProperty::class.java

    override fun apply(property: IconProperty, itemObject: ItemObject, value: String, itemStack: ItemStack, item: ItemImpl)
    {
        if (value.isEmpty())
        {
            return
        }

        itemObject.icon = value
    }
}

class PricePropertyParser : ItemPropertyParser<Long, PriceProperty>
{
    override val propertyType: Class<PriceProperty> = PriceProperty::class.java

    override fun apply(property: PriceProperty, itemObject: ItemObject, value: Long, itemStack: ItemStack, item: ItemImpl)
    {
        itemObject.price = value
    }
}

class ProfessionRequirementPropertyParser : ItemPropertyParser<ProfessionRequirementProperty.ProfessionRequirement, ProfessionRequirementProperty>
{
    override val propertyType: Class<ProfessionRequirementProperty> = ProfessionRequirementProperty::class.java

    override fun apply(property: ProfessionRequirementProperty, itemObject: ItemObject, value: ProfessionRequirementProperty.ProfessionRequirement, itemStack: ItemStack, item: ItemImpl)
    {
        if (!value.any)
        {
            return
        }

        val builder = StringBuilder()

        builder.takeIf { value.warrior }?.append("w")
        builder.takeIf { value.paladin }?.append("p")
        builder.takeIf { value.bladedancer }?.append("b")
        builder.takeIf { value.mage }?.append("m")
        builder.takeIf { value.hunter }?.append("h")
        builder.takeIf { value.tracker }?.append("t")

        itemObject.statistics += "${property.propertyName}=$builder;"
    }
}

class CooldownPropertyParser : ItemPropertyParser<CooldownProperty.Cooldown, CooldownProperty>
{
    override val propertyType: Class<CooldownProperty> = CooldownProperty::class.java

    override fun apply(property: CooldownProperty, itemObject: ItemObject, value: CooldownProperty.Cooldown, itemStack: ItemStack, item: ItemImpl)
    {
        if(value.cooldown == 0)
        {
            return
        }

        val out = StringBuffer(property.propertyName).append("=").append(value.cooldown)
        if(value.nextUse != 0L)
        {
            out.append(",").append(value.nextUse / 1000L)
        }
        out.append(";")

        itemObject.statistics += out.toString()
    }
}

class TeleportPropertyParser : ItemPropertyParser<TeleportProperty.Teleport, TeleportProperty>
{
    override val propertyType: Class<TeleportProperty> = TeleportProperty::class.java

    override fun apply(property: TeleportProperty, itemObject: ItemObject, value: TeleportProperty.Teleport, itemStack: ItemStack, item: ItemImpl)
    {
        if(value.map.isEmpty())
        {
            return
        }

        itemObject.statistics += "teleport=1,1,1;"
    }
}