package hu.andelsoft.spaceminers.gameobjects;

import hu.andelsoft.spaceminers.gameobjects.spaceobjects.*;
import javafx.geometry.Point3D;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Space extends HashSet<SpaceObject> {
    public static final double SAFESPACE = 20;
    private static int SPACEDATALEN;
    private static int CHANCEOFASTEROID;
    private static int CHANCEOFMINERAL;
    private static int MAXOBJECTS;
    private static Point3D SPACEMIN, SPACEMAX;

    private final HashSet<SpaceObject> NewObjects, Garbage;
    private final Object outputLock=new Object();
    private double scoreLeft;
    private final Random rand;
    private ByteBuffer SpaceData;
    private boolean empty;

    /**
     * The Space class represents one field or space for playing.
     * @param props A Porperties file containing parameters.
     * @param score Total score availabel in this field.
     */
    public Space(Properties props, int score){
        setProps(props);

        Garbage = new HashSet<>();
        NewObjects = new HashSet<>();
        rand = new Random();
        this.scoreLeft=score;
        this.SpaceData=ByteBuffer.allocate(SPACEDATALEN);
        this.empty=false;
    }

    /**
     * Updates this Space instance; moves all objects and handles collision, shooting, targeting.
     * @param shipControls CHashMap having each ship's incoming controls.
     */
    public void update(ConcurrentHashMap<SpaceShip, ByteBuffer> shipControls){
        ByteBuffer sobuff = ByteBuffer.allocate(SPACEDATALEN);
        sobuff.putInt(this.size());
        boolean hasMinerals = false;

        //update ships
        shipControls.forEach((ss,b) -> {
            double x = (double)b.getShort();
            double y = (double)b.getShort();
            boolean sp = b.get() != 0;
            ss.changeShipDirection(x,y,0);
            ss.wantsToShoot.set(sp);
        });

        //move, update, shoot, etc
        for (SpaceObject so : this) {
            so.moveInSpace(SPACEMIN,SPACEMAX);

            switch (so.getType()){
                case SPACESHIP: {
                    SpaceShip ss=(SpaceShip) so;
                    ss.shoot().ifPresent(p -> NewObjects.add(p));
                    //targeting minselect and collision
                    double targetd=Double.MAX_VALUE;
                    ss.setTarget(null);
                    for (SpaceObject so2 : this){
                        double newd=ss.getPosition().distance(so2.getPosition());
                        targetd = ss.selectTarget(so2, targetd, newd);
                        if (so.meetsAtDistance(so2,newd)){so.hitSomething(so2);}
                    }
                    break;
                }
                case MINERAL:{
                    hasMinerals = true;
                }
                case TELEPORT_IN:{
                    SpaceShip ss=(SpaceShip) so;
                    if (ss.wantsToShoot.get()) ss.setDestroyed();
                }
                case NOTHING:{
                    Garbage.add(so);
                    break;
                }
            }

            //write objectdata to buffer
            so.toByte(sobuff);
        }

        //write bufferdata to output
        synchronized (outputLock){
            SpaceData=sobuff;
        }

        //remove and add stuff from and to space
        this.removeAll(Garbage);
        Garbage.clear();
        this.addAll(NewObjects);
        NewObjects.clear();
        if (this.size() < MAXOBJECTS){
            if (rand.nextInt(1000) < CHANCEOFASTEROID) {
                double x = rand.nextDouble() * (SPACEMAX.getX() - SAFESPACE);
                this.add(new Asteroid(SpaceObjectType.ASTEROID, new Point3D(x, 0, 0)));
            }
            if (rand.nextInt(1000) < CHANCEOFMINERAL) {
                double x = rand.nextDouble() * (SPACEMAX.getX() - SAFESPACE);
                SpaceObject so= new Asteroid(SpaceObjectType.MINERAL, new Point3D(x, 0, 0));
                this.add(so);
                scoreLeft -= (int) so.getSize();
            }
        }

        //is empty?
        empty = hasMinerals || (scoreLeft > 0);
    }

    /**
     * Returns with the ByteBuffer having all SpaceObjects' actual data.
     * @return
     */
    public ByteBuffer getSpaceData(){
        synchronized (outputLock){
            return SpaceData;
        }
    }

    /**
     * Checks if this Space has any minerals on the field or waiting to get there.
     * @return True if there is and will be no more mineral to pick up.
     */
    public boolean isEmpty () {
        return empty;
    }

    private void setProps(Properties props){
        double x = Double.parseDouble(props.getProperty("space.maxx"));
        double y = Double.parseDouble(props.getProperty("space.maxy"));
        double z = Double.parseDouble(props.getProperty("space.maxz"));
        SPACEMAX = new Point3D(x,y,z);
        x = Double.parseDouble(props.getProperty("space.minx"));
        y = Double.parseDouble(props.getProperty("space.miny"));
        z = Double.parseDouble(props.getProperty("space.minz"));
        SPACEMIN = new Point3D(x,y,z);

        SPACEDATALEN = Integer.parseInt(props.getProperty("space.datalength"));
        CHANCEOFASTEROID = Integer.parseInt(props.getProperty("space.chanceofasteroid"));
        CHANCEOFMINERAL = Integer.parseInt(props.getProperty("space.chanceofmineral"));
        MAXOBJECTS = Integer.parseInt(props.getProperty("space.maxobjects"));
    }


}
