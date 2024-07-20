package de.nomagic.gdb;

public class GdbState
{
    private boolean hasPrompt;
    private int cmd_num;
    private String activeCommandIdToken;

    public GdbState()
    {
        hasPrompt = false;
        cmd_num = 1;
        activeCommandIdToken = null;
    }

    public void receivedPrompt()
    {
        hasPrompt = true;
    }

    public boolean hasPrompt()
    {
        return hasPrompt;
    }

    public String getNextToken()
    {
        // TODO Auto-generated method stub
        String res = "" + cmd_num;
        cmd_num ++;
        return res;
    }

    public void setSendCommand(String commandIdToken)
    {
        activeCommandIdToken = commandIdToken;
    }

    public String getActiveCommandId()
    {
        return activeCommandIdToken;
    }

}
