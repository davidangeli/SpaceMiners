package hu.andelsoft.spaceminers.gameobjects.spaceobjects;

import hu.andelsoft.spaceminers.Player;
import javafx.geometry.Point3D;
import lombok.Getter;
import lombok.Setter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class SpaceShip extends SpaceObject {
    private HashMap<SpaceShipProperty,Double> shipProperties;
    private double shieldState;
    private Instant lastShot;
    private SpaceObject target;
    protected int minerals, lives, kills;
    public AtomicBoolean wantsToShoot;

    public SpaceShip (HashMap<SpaceShipProperty,Double> shipProperties){
        super(SpaceObjectType.TELEPORT_IN);

        position = new Point3D(0,0,0);
        direction = new Point3D(0,0,0);
        this.shipProperties = shipProperties;
        maxspeed = shipProperties.get(SpaceShipProperty.DRIVE_MAXSPEED);
        lastShot = Instant.now();
        target = null;
        shieldState = shipProperties.get(SpaceShipProperty.SHIELD_DMGTRESHOLD);
        wantsToShoot.set(false);
    }

    /**
     * Returns one of the properties of the spaceship.
     * @param key
     * @return double
     */
    public double getProperty(SpaceShipProperty key){
        return shipProperties.get(key);
    }

    /**
     * Adds to a property of the ship.
     * @param key The property element.
     * @param value The value to be added to the original value.
     */
    public void addProperty(SpaceShipProperty key, double value){
        shipProperties.merge(key, value, (v1, v2) -> v1 + v2);
    }

    /**
     * If the ship wants to shoot, can do so, and also has target, creates a projectile at the targeted SpaceObject.
     * @return Optional<Projectile>
     */
    public Optional<Projectile> shoot(){
        if (!wantsToShoot.getAndSet(false)) return Optional.empty();
        if (target == null) return Optional.empty();

        Instant currentInstant = Instant.now();
        if (Duration.between(lastShot,currentInstant).toSeconds()<shipProperties.get(SpaceShipProperty.WEAPON_FIRERATE)) {
            return Optional.empty();
        }

        lastShot = currentInstant;
        return Optional.of(new Projectile(this));
    }

    /**
     * Checks a SpaceObject if it is a better target for this SpaceShip.
     * @param so SpaceObject in question.
     * @param targetd Distance between the current target and this SpaceShip.
     * @param newd Distance between the so and this SpaceShip.
     * @return The actual target's distance, whether it's changed or not.
     */
    public double selectTarget(SpaceObject so, double targetd, double newd){
        //out of range
        if (newd > shipProperties.get(SpaceShipProperty.TARGETING_RANGE)) return targetd;
        //not targetable
        if (so.type != SpaceObjectType.SPACESHIP && so.type != SpaceObjectType.ASTEROID) return targetd;
        //not closer than current
        if (newd > targetd) return targetd;
        //itself
        if(so.equals(this)) return targetd;
        //behind
        if(so.position.getY()>position.getY()) return targetd;
        //not in the right angle range
        if (position.angle(so.position,position.subtract(0,-50,0))>shipProperties.get(SpaceShipProperty.TARGETING_ANGLE)) return targetd;

        //new target
        target = so;
        return newd;
    }

    public void changeShipDirection(double x, double y, double z){
        Point3D dv = new Point3D(x,y,z);
        maxspeed = shipProperties.get(SpaceShipProperty.DRIVE_MAXSPEED);
        dv = dv.normalize().multiply(shipProperties.get(SpaceShipProperty.DRIVE_ACCELERATION));
        super.changeDirection(dv.getX(), dv.getY(), dv.getZ());
    }

    @Override
    public void moveInSpace(Point3D min, Point3D max) {
        double dx = Math.min(Math.max(position.getX() + direction.getX(),min.getX()),max.getX());
        double dy = Math.min(Math.max(position.getY() + direction.getY(),min.getY()),max.getY());
        double dz = Math.min(Math.max(position.getZ() + direction.getZ(),min.getZ()),max.getZ());
        position = position.subtract(position).add(dx,dy, dz);
    }

    @Override
    public void hitSomething(SpaceObject otherSO) {
        if (type.ethereal){return;}

        switch(otherSO.getType()){
            case MINERAL:
                minerals += (int)otherSO.getSize();
                otherSO.setDestroyed();
                break;
            case STASH:
                otherSO.setDestroyed();
                break;
            case ASTEROID:
                collide(otherSO);
                break;
            case SPACESHIP:
                collide(otherSO);
                break;
            default:
                break;
        }
    }

    @Override
    public void takeDamage(double dmg) {
        shieldState -= dmg;
        if (shieldState <= 0) setDestroyed();
    }

    private void collide(SpaceObject otherSO){
        //helpers
        Point3D dv = otherSO.getPosition().subtract(getPosition());
        double mdv = dv.magnitude();
        if (mdv < getSize() + otherSO.getSize()){dv.multiply((getSize() + otherSO.getSize()) / mdv);}
        double isum = otherSO.getSize() * otherSO.getDirection().magnitude() + getSize()*getDirection().magnitude();
        double i1 = (otherSO.getSize() * otherSO.getDirection().magnitude()) / isum;

        //simple changedirection
        changeDirection(dv.multiply(1 - i1));
        otherSO.changeDirection(dv.multiply(i1));

        //damage assessment
        otherSO.takeDamage(shipProperties.get(SpaceShipProperty.SHIELD_POWER));
        switch(otherSO.getType()){
            case ASTEROID:
                takeDamage(Math.max(0.0,otherSO.getSize() - shipProperties.get(SpaceShipProperty.SHIELD_POWER)));
                break;
            case SPACESHIP:
                takeDamage(((SpaceShip)otherSO).getProperty(SpaceShipProperty.SHIELD_POWER));
                break;
            default:
                break;
        }
    }

    /**
     * Writes +5 bytes to the buffer: Shield percentage, target Id, minerals
     * @param bbuff The ByteBuffer we need to write in.
     */
    @Override
    public void toByte(ByteBuffer bbuff){
        super.toByte(bbuff);

        //1 byte for shield percentage
        bbuff.put((byte)((shieldState/shipProperties.get(SpaceShipProperty.SHIELD_DMGTRESHOLD))*100));
        //2 bytes for target
        if (target != null){
            bbuff.putShort((short)target.getId());
        }
        else{
            bbuff.putShort((short)0);
        }
        //2bytes for minerals
        bbuff.putShort((short)minerals);
    }
}
