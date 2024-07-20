package de.nomagic.gdb;

import java.util.Arrays;

public class GdbResponse
{
    private final String unparsed;
    private String[] commandResult;

    public GdbResponse(String unparsed)
    {
        this.unparsed = unparsed;
    }

    public String getUnparsed()
    {
        return unparsed;
    }

    public void addResult(String[] parts)
    {
        commandResult = parts;
    }

    @Override
    public String toString()
    {
        return "GdbResponse [unparsed=" + unparsed + ", commandResult=" + Arrays.toString(commandResult) + "]";
    }

}
