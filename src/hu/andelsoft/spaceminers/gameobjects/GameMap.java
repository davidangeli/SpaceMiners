package hu.andelsoft.spaceminers.gameobjects;

import hu.andelsoft.spaceminers.Player;
import hu.andelsoft.spaceminers.gameobjects.spaceobjects.SpaceShip;
import lombok.Getter;

import java.net.InetAddress;
import java.util.HashMap;

public class GameMap {

    private final HashMap<InetAddress,SpaceShip> incoming;
    private final HashMap<Player,Integer> outgoing;

    public GameMap() {
        incoming = new HashMap<>();
        outgoing = new HashMap<>();
    }

    public void put (Player pl, int port) {
        outgoing.put(pl, port);
    }

    public void put (InetAddress ip, SpaceShip ss) {
        incoming.put(ip, ss);
    }

    public void remove (Player pl) {
        incoming.remove(pl.getIp());
        outgoing.remove(pl);
    }

    public SpaceShip getShip(InetAddress ip){
        return incoming.get(ip);
    }

    public int getPort(Player pl){
        return outgoing.get(pl);
    }

    public HashMap<Player, Integer> getPlayers(){
        return outgoing;
    }
}
