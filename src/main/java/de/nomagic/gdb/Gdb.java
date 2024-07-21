package de.nomagic.gdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gdb
{
    private final Logger log =  LoggerFactory.getLogger(this.getClass().getName());

    // "arm-none-eabi-gdb"
    // "--interpreter=mi4"
    // select the interpreter
    // mi = machine oriented line based interface
    // 4 is the version
    // version 1 was in GDB version 5.1  (2002-01-31)
    // version 2 was in GDB version 6.0  (2003-10-06)
    // version 3 was in GDB version 9.1  (2020-02-08)
    // version 4 was in GDB version 13.1 (2023-02-19)
    //
    // "--nh"
    // Do not execute commands from ~/.config/gdb/gdbinit, ~/.gdbinit, ~/.config/gdb/gdbearlyinit, or ~/.gdbearlyinit
    // "--nx"
    // Do not execute commands from any .gdbinit or .gdbearlyinit initialization files.

    private String[] gdb_cmd = {"arm-none-eabi-gdb", "--interpreter=mi2", "--nh", "--nx"};
    private ProcessBuilder builder;
    private Process process = null;
    private BufferedWriter gdb_in;
    private BufferedReader gdb_out;
    private GdbState state;

    enum async {
        EXEC_ASYNC,
        STATUS_ASYNC,
        NOTIFY_ASYNC,
        CONSOLE_STREAM,
        TARGET_STREAM,
        LOG_STREAM
        }


    public Gdb()
    {
        builder = new ProcessBuilder(gdb_cmd);
        builder.redirectErrorStream(true); // stderr is merged with stdout
    }

    public void open()
    {
        state = new GdbState();
        try
        {
            process = builder.start();
            gdb_in = process.outputWriter(); // stdin of gdb
            gdb_out = process.inputReader();// stdout of gdb
            parseResponses(null);
        }
        catch(IOException e)
        {
            log.error("Failed to start gdb!");
            process = null;
        }
    }

    private GdbCommand parseResponses(GdbCommand cmd)
    {
        try
        {
            String line;
            String idToken = state.getActiveCommandId();
            while ((line = gdb_out.readLine()) != null)
            {
                if((null != idToken) && (true == line.startsWith(idToken)))
                {
                    // response to the currently active command
                    // starts with idToken + "^"
                    GdbResponse res = new GdbResponse(line);
                    String record = line.substring(idToken.length() + 1);
                    res.addResult(record);
                    if(null != cmd)
                    {
                        cmd.addResult(res);
                    }
                }
                else if(true == line.startsWith("~"))
                {
                    // console output
                    handleOutOfBand(line.substring(1), async.CONSOLE_STREAM);
                }
                else if(true == line.startsWith("@"))
                {
                    // target stream output
                    handleOutOfBand(line.substring(1), async.TARGET_STREAM);
                }
                else if(true == line.startsWith("&"))
                {
                    // log stream output
                    handleOutOfBand(line.substring(1), async.LOG_STREAM);
                }
                else if(true == line.startsWith("*"))
                {
                    // exec async output
                    handleOutOfBand(line.substring(1), async.EXEC_ASYNC);
                }
                else if(true == line.startsWith("+"))
                {
                    // status async output
                    handleOutOfBand(line.substring(1), async.STATUS_ASYNC);
                }
                else if(true == line.startsWith("="))
                {
                    // notify-async-output
                    handleOutOfBand(line.substring(1), async.NOTIFY_ASYNC);
                }
                else if(true == "(gdb) ".equals(line))
                {
                    log.trace("found prompt");
                    state.receivedPrompt();
                    return cmd;
                }
                else
                {
                    log.warn("unrecognized respoonse GDB[" + line + "]!(id=" + idToken + ")");
                }
            }

        }
        catch (IOException e)
        {
            log.info("IO Exception !");
            e.printStackTrace();
        }
        return cmd;
    }

    private void handleOutOfBand(String line, async type)
    {
        switch(type)
        {
        case EXEC_ASYNC:    log.info("aync exec: " + line);      break;
        case STATUS_ASYNC:  log.info("aync status: " + line);    break;
        case NOTIFY_ASYNC:  log.info("aync notify: " + line);    break;
        case CONSOLE_STREAM:log.info("gdb console: " + line);    break;
        case TARGET_STREAM: log.info("target console: " + line); break;
        case LOG_STREAM:    log.info("log console: " + line);    break;
        }
    }

    public void send_command(String command)
    {
        GdbCommand cmd = new GdbCommand(command);
        send_command(cmd);
    }

    public GdbCommand send_command(GdbCommand cmd)
    {
        if(null == process)
        {
            return cmd;
        }
        if(false == state.hasPrompt())
        {
            parseResponses(null);
            if(false == state.hasPrompt())
            {
                log.warn("sending command '" + cmd.getCommand() + "' while gdb is busy !");
            }
            // found prompt in parseResponses();
        }
        try
        {
            String commandIdToken = state.getNextToken();
            gdb_in.write(commandIdToken + "-" + cmd.getCommand() + "\r\n");
            gdb_in.flush();
            state.setSendCommand(commandIdToken);
            cmd = parseResponses(cmd);
        }
        catch(IOException e)
        {
            log.error("failed to send command '" + cmd.getCommand() + "' IO Exception !");
        }
        return cmd;
    }

    public void close()
    {
        if(null == process)
        {
            return;
        }
        send_command("gdb-exit");
        // wait
        for(int i = 0; i < 20; i++)
        {
            if(true == process.isAlive())
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    // is OK
                }
            }
        }
        if(true == process.isAlive())
        {
            log.warn("destroying gdb");
            process.destroy();
        }
        // wait
        for(int i = 0; i < 20; i++)
        {
            if(true == process.isAlive())
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    // is OK
                }
            }
        }
        if(true == process.isAlive())
        {
            log.warn("destroying gdb forcefully");
            process.destroyForcibly();
        }
        try
        {
            while(true == gdb_out.ready())
            {
                String line;
                line = gdb_out.readLine();
                log.trace(line);
            }
        }
        catch(IOException e)
        {
            log.error("IO Exception while closing gdb !");
        }

        int res = process.exitValue();
        if(0 != res)
        {
            log.warn("gdb exited with {} !", res);
        }
        else
        {
            // 0 == OK
            log.trace("gdb exited with 0 !");
        }
    }

}