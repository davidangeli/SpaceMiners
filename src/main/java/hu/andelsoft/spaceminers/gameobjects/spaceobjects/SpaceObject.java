package hu.andelsoft.spaceminers.gameobjects.spaceobjects;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import javafx.geometry.Point3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SpaceObject {
    protected static final double BLOWUPRATE=10.0;
    protected static final int BLOWUPMSEC=200;

    protected static int idc;
    protected static final Random rand = new Random();
    protected int id;
    protected SpaceObjectType type;
    protected Point3D position = Point3D.ZERO;
    protected Point3D direction = Point3D.ZERO;
    protected double size;
    protected double maxspeed;
    protected Instant lastChange;

    public SpaceObject(SpaceObjectType type){
        id =++ idc;
        this.type=type;
        this.size=type.defaultSize;
        this.maxspeed=type.defaultSpeed;
    }

    public void changeDirection(Point3D dd){
        changeDirection(dd.getX(), dd.getY(), dd.getZ());
    }

    public void changeDirection(double x, double y, double z){
        direction=direction.add(x,y,z);

        double m = direction.magnitude();
        if (Math.abs(m)>maxspeed){
            direction = direction.multiply(maxspeed/m);
        }
    }

    public void moveInSpace(Point3D min, Point3D max) {
        position.add(direction);
        Point3D p=position;
        if (p.getX()>max.getX() || p.getY()>max.getY() || p.getZ()>max.getZ() || p.getX()<min.getX() || p.getY()<min.getY() || p.getZ()<min.getZ())
        this.type=SpaceObjectType.NOTHING;

        if (this.type.blows) blowup();
    }

    public boolean meets(SpaceObject otherSO) {
        if (equals(otherSO)) {return false;}
        double sd = this.size + otherSO.size;
        return(position.distance(otherSO.position) <= sd);
    }

    //if we already have our distance
    public boolean meetsAtDistance(SpaceObject otherSO, double d) {
        if (equals(otherSO)) {return false;}
        double sd = this.size+otherSO.size;
        return(d <= sd);
    }

    public void setDestroyed() {
        type = type.getNextType();
        lastChange = Instant.now();
        maxspeed = type.defaultSpeed;
        changeDirection(new Point3D(0,0,0));
    }

    public void blowup() {
        Instant currentInstant = Instant.now();
        if (Duration.between(lastChange,currentInstant).toMillis() < BLOWUPMSEC) return;

        double dsize = type.defaultSize / BLOWUPRATE;
        size = Math.max(size-dsize,0.0);
        lastChange=currentInstant;
        if (size == 0.0) setDestroyed();
    }

    public abstract void hitSomething (SpaceObject otherSO);
    public abstract void takeDamage(double dmg);

    /**
     * Writes 10 bytes to the buffer: Id, type, size, position
     * @param bbuff The ByteBuffer we need to write in.
     */
    public void toByte(ByteBuffer bbuff) {
        //2 byte for id
        bbuff.putShort((short)id);
        //1 byte for type
        bbuff.put((byte)(type.ordinal() + 1));
        //1 byte for size
        bbuff.put((byte)size);
        //6 bytes for position
        bbuff.putShort((short) position.getX());
        bbuff.putShort((short) position.getY());
        bbuff.putShort((short) position.getZ());
    }

    @Override
    public boolean equals (Object other) {
        if (!(other instanceof SpaceObject)) return false;
        return ((SpaceObject) other).getId() == this.id;
    }

}
