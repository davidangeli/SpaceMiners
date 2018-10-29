package hu.andelsoft.spaceminers;

import hu.andelsoft.spaceminers.gameobjects.spaceobjects.SpaceShipProperty;

import java.util.HashMap;

public class ItemManager {

    private static ItemManager ourInstance = new ItemManager();

    public static ItemManager getInstance() { return ourInstance; }


    private ItemManager () {

    }


    public HashMap<SpaceShipProperty,Double> getEffects(HashMap<Integer,Boolean> Inventory){


        return new HashMap<SpaceShipProperty,Double>();
    }
}
