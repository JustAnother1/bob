package de.nomagic.gdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gdb
{
    private final Logger log =  LoggerFactory.getLogger(this.getClass().getName());

    private String[] gdb_cmd = {"arm-none-eabi-gdb", "--interpreter=mi4", "--nh", "--nx"};
    private ProcessBuilder builder;
    private Process process;
    private BufferedWriter gdb_in;
    private BufferedReader gdb_out;

    public Gdb()
    {
        builder = new ProcessBuilder(gdb_cmd);
        builder.redirectErrorStream(true); // STDerr is merged with STDout
    }

    public void open() throws IOException
    {
        process = builder.start();
        gdb_in = process.outputWriter(); // STDin of gdb
        gdb_out = process.inputReader();// STDout of gdb
        readResponses();
    }

    public void send_command(String command) throws IOException
    {
        gdb_in.write("-" + command + "\r\n");
        gdb_in.flush();
    }

    public void close() throws IOException
    {
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
        while(true == gdb_out.ready())
        {
            String line;
            line = gdb_out.readLine();
            log.trace(line);
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

    private void readResponses() throws IOException
    {
        String line;

        while ((line = gdb_out.readLine()) != null)
        {
            if(true == line.startsWith("~"))
            {
                System.out.println(line.substring(1));
            }
            else if(true == line.startsWith("="))
            {
                // notify-async-output
                log.info("aync notify: " + line.substring(1));
                // TODO parse
            }
            else if(true == "(gdb) ".equals(line))
            {
                log.trace("found prompt");
                return;
            }
            else
            {
                log.info("unrecognized respoonse GDB[" + line + "]!");
            }
        }
    }


}