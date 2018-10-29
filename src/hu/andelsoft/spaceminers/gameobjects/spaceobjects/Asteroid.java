package hu.andelsoft.spaceminers.gameobjects.spaceobjects;

import javafx.geometry.Point3D;

import java.nio.ByteBuffer;

public class Asteroid extends SpaceObject {
    private final static double SO_ASTEROID_DIRCHANGECHANCE=10.0;
    private final static double SO_ASTEROID_DIRMAXCHANGE=5.0;
    private final static double SO_ASTEROID_SIZEMAXCHANGE=50.0;

    public Asteroid (SpaceObjectType type, Point3D position){
        super(type);

        this.position = position;

        //direction, randomization
        direction = new Point3D(0,-maxspeed,0);
        int r = rand.nextInt(100);
        if (r < SO_ASTEROID_DIRCHANGECHANCE){
            double x = ((rand.nextDouble()*SO_ASTEROID_DIRMAXCHANGE)-SO_ASTEROID_DIRMAXCHANGE/2.0);
            changeDirection(x,0,0);
        }

        //size randomization
        double d = size * (SO_ASTEROID_SIZEMAXCHANGE / 100.0);
        size=size + ((rand.nextDouble() * d - (d / 2)));

    }

    @Override
    public void hitSomething(SpaceObject otherSO) {
        //not implemented here; the idea is that other SO-s hit Asteriods
    }

    @Override
    public void takeDamage(double dmg) {
        size = Math.max(size - dmg, 0.0);
        if (size < 2.0) setDestroyed();
    }
}
