package de.nomagic.gdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.bob.Configuration;

public class MiProtocol
{
    private final Logger log =  LoggerFactory.getLogger(this.getClass().getName());
    private final Gdb gdb;
    private final Configuration cfg;

    public MiProtocol(Gdb gdb, Configuration cfg)
    {
        this.gdb = gdb;
        this.cfg = cfg;
        initializeConnection();
    }

    public void close()
    {

    }

    private String[] executeConnectedCommand(String command)
    {
        GdbCommand cmd = new GdbCommand(command);
        cmd = gdb.send_command(cmd);
        GdbResponse[] res = cmd.getResult();
        if(1 != res.length)
        {
            log.error("received invalid response to command {} !", cmd);
            return null;
        }
        String type = res[0].getResultClass();
        if(false == "connected".equals(type))
        {
            log.error("Failed to receive response to command {} !", cmd);
            return null;
        }
        return res[0].getResultValue();
    }

    private String[] executeDoneCommand(String command)
    {
        GdbCommand cmd = new GdbCommand(command);
        cmd = gdb.send_command(cmd);
        GdbResponse[] res = cmd.getResult();
        if(1 != res.length)
        {
            log.error("received invalid response to command {} !", cmd);
            return null;
        }
        String type = res[0].getResultClass();
        if(false == "done".equals(type))
        {
            log.error("Failed to receive response to command {} !", cmd);
            return null;
        }
        return res[0].getResultValue();
    }

    private void initializeConnection()
    {
        String[] res = executeDoneCommand("list-features");
        log.info("features : {}", res[0]);

        res = executeDoneCommand("gdb-show language");
        log.info("lang : {}", res[0]);

        res = executeDoneCommand("data-evaluate-expression \"sizeof (void*)\"");
        log.info("sizeof void* : {}", res[0]);

        executeDoneCommand("interpreter-exec console \"show endian\""); // goes to console -> no Result
        executeDoneCommand("environment-cd " + cfg.getWorkingDirectory()); // no result -> just "done"
        executeDoneCommand("gdb-set breakpoint pending on"); // no result -> just "done"
        executeDoneCommand("enable-pretty-printing"); // no result -> just "done"
        executeDoneCommand("gdb-set python print-stack none"); // no result -> just "done"
        executeDoneCommand("gdb-set print object on"); // no result -> just "done"
        executeDoneCommand("gdb-set print sevenbit-strings on"); // no result -> just "done"
        executeDoneCommand("gdb-set charset UTF-8"); // no result -> just "done"
        executeDoneCommand("gdb-set auto-solib-add on"); // no result -> just "done"
        executeDoneCommand("file-exec-file " + cfg.getElfFileAbsolute()); // no result -> just "done"
        res = executeDoneCommand("gdb-show --thread-group i1 language");
        log.info("lang tg : {}", res[0]);

        executeConnectedCommand("target-select " + cfg.getConnectionString());

        executeDoneCommand("monitor reset init");
        executeDoneCommand("monitor halt");
    }

}
