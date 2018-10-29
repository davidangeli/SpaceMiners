package hu.andelsoft.spaceminers.gameobjects.spaceobjects;

import javafx.geometry.Point3D;
import lombok.Getter;

@Getter
public class Projectile extends SpaceObject {
    // damage multiplier in case of equal weapon power and ship shield power, scale 0-10
    private static final double BASE_DMG_FACTOR=4.0;
    private final SpaceShip shotBy;
    private final SpaceObject target;
    private final double power;
    private final double damage;
    

    public Projectile(SpaceShip ship){
        super(SpaceObjectType.PROJECTILE);
        position = new Point3D(ship.getPosition().getX(),ship.getPosition().getY(),ship.getPosition().getZ());

        shotBy = ship;
        target = ship.getTarget();
        power = ship.getProperty(SpaceShipProperty.WEAPON_POWER);
        damage = ship.getProperty(SpaceShipProperty.WEAPON_MAXDMG);
        updateDirection();
    }

    public void updateDirection(){
        direction = target.getPosition().subtract(position);
        direction = direction.subtract(0.0,2 * direction.getY(),0.0);
        direction = direction.normalize().multiply(maxspeed);
    }

    @Override
    public void moveInSpace(Point3D min, Point3D max) {
        if (target.getType().ethereal) {
            this.setDestroyed();
        }
        else {
            super.moveInSpace(min, max);
            if (this.meets(target)) this.hitSomething(target);
            else updateDirection();
        }
    }

    /**
     * Should only be called with it's target as argument.
     * @param otherSO The other
     */
    @Override
    public void hitSomething(SpaceObject otherSO) {

        switch (otherSO.getType()){
            case ASTEROID:
                otherSO.takeDamage(damage);
                break;
            case SPACESHIP:
                SpaceShip ss = (SpaceShip) otherSO;
                double factor = Math.max(Math.min(((BASE_DMG_FACTOR + (power - ss.getProperty(SpaceShipProperty.SHIELD_POWER))) / 10.0),10.0),0);
                otherSO.takeDamage(factor * damage);
                if (otherSO.getType()!=SpaceObjectType.SPACESHIP) {
                    ++shotBy.kills;
                    --ss.lives;
                }
                break;
            default:
                break;
        }
        this.setDestroyed();
    }

    @Override
    public void takeDamage(double dmg) {
        //not implemented, no need
    }
}
