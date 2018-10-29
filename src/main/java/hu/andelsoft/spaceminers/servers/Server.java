package hu.andelsoft.spaceminers.servers;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Server implements Runnable {

    protected boolean ServerRuns=false, ServerReady=false;
    protected final String ServerName;
    protected final Logger logger;
    protected int updateMsc;

    public Server(String servername){

        this.ServerName=servername;
        logger=Logger.getLogger("SpaceMiners."+ServerName);
        logger.log(Level.INFO, "{0}: created.", ServerName);
    }

    @Override
    public void run() {
        if (!ServerReady || updateMsc == 0) {
            logger.log(Level.INFO, "{0}: not initialized.",ServerName);
            return;
        }

        ServerRuns=true;
        logger.log(Level.INFO, "{0}: started.",ServerName);

        setup();
        while(ServerRuns){

            mainFunction();

            try {
                Thread.sleep(updateMsc);
            } catch (InterruptedException ex) {
                logger.log(Level.INFO, "{0}: interrupted.",new Object[]{ServerName,ex});
                if(ServerRuns){stopServer();}
            }
        }
        cleanup();
        logger.log(Level.INFO, "{0}: done.",ServerName);
    }

    public boolean isRunning(){
        return ServerRuns;
    }

    public void stopServer(){
        logger.log(Level.INFO, "{0}: stopping.",ServerName);
        ServerRuns=false;
    }

    public String[] getServerInfo(){
        String[] str=new String[1];
        str[0]=ServerName+" running: " + ServerRuns;
        return str;
    }

    /**
     * Must be called before Run.
     * @param props Properties parameter.
     * @param msec Millisecs to define run frequency.
     */
    protected abstract void init (Properties props, int msec);

    /**
     * This function is called once, in Run, before the while(ServerRuns) cycle.
     */
    protected abstract void setup ();

    /**
     * This function is called in the Run, inside the while(ServerRuns) cycle.
     */
    protected abstract void mainFunction();

    /**
     * This function is called once, in the Run, after the while(ServerRuns) cycle.
     */
    protected abstract void cleanup();
}
