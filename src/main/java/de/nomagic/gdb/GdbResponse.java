package de.nomagic.gdb;

import java.util.Vector;

public class GdbResponse
{
    private final String unparsed;
    private String ResultClass;
    private Vector<String> commandResult = new Vector<String>();

    public GdbResponse(String unparsed)
    {
        this.unparsed = unparsed;
    }

    public String getUnparsed()
    {
        return unparsed;
    }

    public void addResult(String res)
    {
        // result can be one or more parts.
        // parts can be:
        //   - Strings
        //   - tuple {}
        //   - lists []
        String[] parts = res.split(",");
        ResultClass = parts[0];
        StringBuffer collect = null;
        int inTuple = 0;
        int inList = 0;
        for(int i = 1; i < parts.length; i++)
        {
            for(int k = 0; k < parts[i].length(); k++)
            {
                if('{' == parts[i].charAt(k))
                {
                    inTuple++;
                }
                else if('[' == parts[i].charAt(k))
                {
                    inList++;
                }
            }
            if((0 == inTuple) && (0 == inList))
            {
                // single result
                commandResult.add(parts[i]);
            }
            else
            {
                // this part belongs to a List or Tuple
                if(null == collect)
                {
                    // first part
                    collect = new StringBuffer();
                    collect.append(parts[i]);
                }
                else
                {
                    collect.append(",");
                    collect.append(parts[i]);
                }
                for(int k = 0; k < parts[i].length(); k++)
                {
                    if('}' == parts[i].charAt(k))
                    {
                        inTuple--;
                    }
                    else if(']' == parts[i].charAt(k))
                    {
                        inList--;
                    }
                }
                if((0 == inTuple) && (0 == inList))
                {
                    // end of this result reached
                    commandResult.add(collect.toString());
                    collect = new StringBuffer();
                }
            }
        }
    }

    public String getResultClass()
    {
        return ResultClass;
    }

    @Override
    public String toString()
    {
        return "GdbResponse [unparsed=" + unparsed + ", commandResult=" + commandResult + "]";
    }

    public String[] getResultValue()
    {
        return commandResult.toArray(new String[0]);
    }

    public static String dumpStringArray(String[] arr)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if(null != arr)
        {
            for(int i = 0; i < arr.length; i++)
            {
                sb.append(arr[i]);
                sb.append("\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
