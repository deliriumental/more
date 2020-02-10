package msifeed.mc.more.crabs.character;

import msifeed.mc.commons.traits.Trait;
import msifeed.mc.commons.traits.TraitRegistry;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class Character {
    public String name = "";
    public String wikiPage = "";

    public EnumMap<Ability, Integer> abilities = new EnumMap<>(Ability.class);
    public Set<Trait> traits = new HashSet<>();
    public Illness illness = new Illness();

    public Character() {
        for (Ability f : Ability.values())
            abilities.put(f, 7);
    }

    public Character(Character c) {
        name = c.name;
        wikiPage = c.wikiPage;
        for (EnumMap.Entry<Ability, Integer> e : c.abilities.entrySet())
            abilities.put(e.getKey(), e.getValue());
        traits.addAll(c.traits);
        illness.unpack(c.illness.pack());
    }

    public Set<Trait> traits() {
        return traits;
    }

    public boolean has(Trait trait) {
        return traits.contains(trait);
    }

    public NBTTagCompound toNBT() {
        final NBTTagCompound c = new NBTTagCompound();

        c.setString(Tags.name, name);
        c.setString(Tags.wiki, wikiPage);

        final int[] abilitiesArr = new int[Ability.values().length];
        for (Ability f : Ability.values())
            abilitiesArr[f.ordinal()] = abilities.getOrDefault(f, 0);
        c.setIntArray(Tags.abilities, abilitiesArr);

        c.setIntArray(Tags.traits, traits.stream().mapToInt(t -> t.code).toArray());
        c.setInteger(Tags.illness, illness.pack());

        return c;
    }

    public void fromNBT(NBTTagCompound c) {
        name = c.getString(Tags.name);
        wikiPage = c.getString(Tags.wiki);

        final int[] abilitiesArr = c.getIntArray(Tags.abilities);
        for (Ability f : Ability.values())
            abilities.put(f, abilitiesArr[f.ordinal()]);

        traits = TraitRegistry.decode(c.getIntArray(Tags.traits));
        illness.unpack(c.getInteger(Tags.illness));
    }

    private static class Tags {
        static final String name = "name";
        static final String wiki = "wiki";
        static final String abilities = "abs";
        static final String traits = "traits";
        static final String illness = "illness";
    }
}
