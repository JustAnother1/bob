package de.nomagic.gdb;

import java.util.Vector;

public class GdbCommand
{
    private final String command;
    private final Vector<GdbResponse> results = new Vector<GdbResponse>();

    public GdbCommand(String command)
    {
        this.command = command;
    }

    public String getCommand()
    {
        return command;
    }

    public void addResult(GdbResponse res)
    {
        results.add(res);
    }

    @Override
    public String toString()
    {
        return "GdbCommand [command=" + command + ", results=" + results + "]";
    }

}
