package hu.andelsoft.spaceminers.gameobjects;

import hu.andelsoft.spaceminers.ItemManager;
import hu.andelsoft.spaceminers.Player;
import hu.andelsoft.spaceminers.gameobjects.spaceobjects.SpaceShip;
import hu.andelsoft.spaceminers.network.UDPNode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Game class represents one game between the Players. Implementing the Callable interface,
 * upon calling or submitting the task, it returns with an array of the results.
 */
@Getter
@Setter
public class Game implements Callable<Optional<int[][]>> {
    private static int MSGDATALENTORECEIVE;
    private static int CREDS_PERMINERAL;
    private static int CREDS_ADDPERRANK;

    private final int id;
    private final String name;
    private final int rank, port, minpl, maxpl;
    private Space space;
    private boolean gameRuns=false;
    private final boolean closed;
    private final GameMap Players;
    private final ItemManager IM;
    private final ConcurrentHashMap<SpaceShip,ByteBuffer> shipControls;
    private UDPNode udpNode;

    /**
     * Constuctor.
     * @param props A Porperties file containing parameters.
     * @param id Game's id.
     * @param name Game's short name.
     * @param rank The minimum rank of Players.
     * @param closed True if only it's not open to anybody.
     * @param minpl Minimum number of players.
     * @param maxpl Maximum number of players.
     * @param port Port number for the Game's DatagramSocket.
     * @param createdby The player who created this game.
     * @param im an ItemManager.
     */
    public Game(Properties props, int id, String name, int rank, boolean closed, int minpl, int maxpl, int port, Player createdby, ItemManager im) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.port = port;
        this.minpl = minpl;
        this.maxpl = maxpl;
        this.closed = closed;
        this.space = new Space (props, rank);
        this.Players = new GameMap();
        this.IM = im;
        this.shipControls = new ConcurrentHashMap<>();
    }

    /**
     * The Game's call method sets up Space, UDPNode, and starts the game.
     * It will run until the Space has minerals, normally.
     * @return In a normal ending, and array of the results will be returned. Null otherwise.
     * @throws IOException
     */
    @Override
    public Optional<int[][]> call() throws IOException {

        //player-space setup
        Players.getPlayers().forEach((pl, port) -> {
            SpaceShip ss = new SpaceShip(IM.getEffects(pl.getInventory()));
            Players.put(pl.getIp(),ss);
            space.add(ss);
        });

        //udpnode setup
        udpNode = null;
        //throws SocketException
        udpNode = new UDPNode(port, MSGDATALENTORECEIVE, (ip, data)->{
            shipControls.put(Players.getShip(ip),ByteBuffer.wrap(data));
        });
        Thread udpnodeThread = new Thread(udpNode);
        udpnodeThread.run();

        //main cycle
        while (!space.isEmpty() && gameRuns && udpNode.isOpen()){
            //maybe these should run in different threads
            space.update(shipControls);
            sendData(space.getSpaceData());
        }

        //normal stop
        if (gameRuns && udpNode.isOpen()){
            gameRuns = false;
            //throws IOException
            udpNode.stop();
            return Optional.of(this.getResultSet());
        }
        else {
            gameRuns = false;
            udpNode.stop();
            return Optional.empty();
        }
    }

    /**
     * Adds a player to this game, and sets it's inGame accordingly.
     * If the player is already in a game, returns false.
     * @param pl Player instance.
     * @param port Port of the player where it will accept Datagram packets.
     * @return True, if the player was added to this game.
     */
    public boolean addPlayer(Player pl, int port){
        if (pl.getInGame() != 0 ) return false;

        pl.setInGame(this.id);
        Players.put(pl, port);
        return true;
    }

    /**
     * Removes a player from this game. Returns false if the player belonged ot a different game.
     * @param pl Player instance.
     * @return True if the removal was successful.
     */
    public boolean removePlayer(Player pl){
        if (pl.getInGame() != this.id ) return false;

        Players.remove(pl);
        pl.setInGame(0);
        return true;
    }

    /**
     * Sends data to all this game's players, through it's DatagramSocket.
     * @param bbuff The data.
     * @throws IOException
     */
    private void sendData(ByteBuffer bbuff) throws IOException{
        for (Player pl : Players.getPlayers().keySet()){
            udpNode.sendMsg(bbuff.array(), pl.getIp(), Players.getPort(pl));
        }
    }

    /**
     * When the game is finished, this function provides the resulting array,
     * that contains each player's score.
     * @return A players x 4 integer array: gameid, playerid, minerals, credits per each Player.
     */
    private int[][] getResultSet(){
        int[][] ResultSet=new int[Players.getPlayers().size()][3];
        int i=0;

        for (Player pl : Players.getPlayers().keySet()) {
            double rankmod=Math.max(1.0,pl.getRank()-this.rank+1);
            ResultSet[i][0] = pl.getPlayerId();
            ResultSet[i][1] = Players.getShip(pl.getIp()).getMinerals();
            ResultSet[i][2] = (int) (ResultSet[i][2] * (CREDS_PERMINERAL + CREDS_ADDPERRANK * this.rank)/rankmod);
            ++i;
        };
        return ResultSet;
    }

    private void setProps(Properties props){
        MSGDATALENTORECEIVE = Integer.parseInt(props.getProperty("game_msgdatalen_toreceive"));
        CREDS_PERMINERAL = Integer.parseInt(props.getProperty("game_creds_permineral"));
        CREDS_ADDPERRANK = Integer.parseInt(props.getProperty("game_creds_additionperrank"));

    }
}
