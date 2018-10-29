package hu.andelsoft.spaceminers.gameobjects;

import hu.andelsoft.spaceminers.gameobjects.spaceobjects.SpaceShipProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Item {
    private final int id, itemTypeId, shipClass, credits, chanceToFind;
    private final String name, description;
    private final HashMap<SpaceShipProperty,Double> effects;

    public Item (int iid, int itid, int sc, int cred, String name, String desc, int chance){
        id = iid;
        itemTypeId = itid;
        shipClass = sc;
        credits = cred;
        this.name = name;
        description = desc;
        chanceToFind = chance;
        effects = new HashMap<>();
    }

    public double getEffect(SpaceShipProperty key){
        return effects.get(key);
    }

    public void addEffect(SpaceShipProperty key, double value){
        effects.put(key, value);
    }


}
