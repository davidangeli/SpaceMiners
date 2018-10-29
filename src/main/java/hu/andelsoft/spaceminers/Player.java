package hu.andelsoft.spaceminers;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.util.HashMap;


@Getter
@Setter
public class Player {
    int playerId;
    int inGame;
    int rank;
    private final HashMap<Integer, Boolean> Inventory;
    private InetAddress ip;

    public Player (){

        Inventory = new HashMap<>();

    }

}
