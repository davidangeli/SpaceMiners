package hu.andelsoft.spaceminers.servers;

import hu.andelsoft.spaceminers.gameobjects.Game;

import java.util.HashMap;
import java.util.Properties;

public class GameServer extends Server {
    private final HashMap<Integer, Game> Games;

    private static GameServer ourInstance = new GameServer("GameServer");

    public static GameServer getInstance() {
        return ourInstance;
    }

    private GameServer(String servername) {
        super(servername);
        Games = new HashMap<>();
    }

    @Override
    protected void init(Properties props, int msec) {
        updateMsc=Integer.parseInt(props.getProperty("server_game_updatesmsc"));
        ServerReady = true;
    }

    @Override
    protected void setup() {

    }

    @Override
    protected void mainFunction() {
        for (Game gm : Games.values()){

        }

    }

    @Override
    protected void cleanup() {

    }
}
