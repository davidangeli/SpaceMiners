package hu.andelsoft.spaceminers.gameobjects.spaceobjects;

public enum SpaceObjectType {

    NOTHING(true, 10.0, 5.0, false),
    TELEPORT_IN(true, 12.0, 4.0, false),
    SPACESHIP(false, 14.0, 4.0, false),
    TELEPORT_OUT(true, 14.0, 0.0, true),
    ASTEROID(false, 16.0, 3.0, false),
    MINERAL(false, 14.0, 2.0, false),
    STASH(false, 12.0, 4.0, false),
    PROJECTILE(false, 10.0, 10.0, false),
    EXPLOSION(true, 10.0, 1.0, true),
    DUST(true, 12.0, 2.0, true);

    static {
        NOTHING.nextType=NOTHING;
        TELEPORT_IN.nextType=SPACESHIP;
        SPACESHIP.nextType=TELEPORT_OUT;
        TELEPORT_OUT.nextType=NOTHING;
        ASTEROID.nextType=DUST;
        MINERAL.nextType=NOTHING;
        STASH.nextType=NOTHING;
        PROJECTILE.nextType=NOTHING;
        EXPLOSION.nextType=NOTHING;
        DUST.nextType=NOTHING;
    }

    private SpaceObjectType nextType;
    public final double defaultSize;
    public final double defaultSpeed;
    public final boolean ethereal;
    public final boolean blows;

    SpaceObjectType(boolean eth, double size, double speed, boolean blows){

        this.ethereal = eth;
        this.defaultSize = size;
        this.defaultSpeed = speed;
        this.blows = blows;
    }

    public SpaceObjectType getNextType() {
        return nextType;
    }
}
