package cz.muni.fi.jboss.migration.conf;

import java.io.File;

/**
 * AS 7 configuration.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7Config {

    private String as7dir;
    private String as7configPath = "standalone/configuration/standalone.xml";
    private String configDir = "standalone/configuration";


    public String getConfigFilePath() {
        return new File(getDir(), getConfigPath()).getPath();  // TODO: Return File and use that.
    }

    public String getConfigDir(){
        return new File(getDir(), configDir).getPath();
    }

    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getDir() {
        return as7dir;
    }

    public void setDir(String dirAS7) {
        this.as7dir = dirAS7;
    }

    public String getConfigPath() {
        return as7configPath;
    }

    public void setConfigPath(String confPathAS7) {
        this.as7configPath = confPathAS7;
    }
    //</editor-fold>

}
