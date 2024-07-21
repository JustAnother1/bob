package de.nomagic.bob;

public class Configuration
{

    public Configuration()
    {

    }

    public String getWorkingDirectory()
    {
        return "/home/lars/prj/raspberrypi_pico/blinky/RP2040_blinky";
    }

    public String getElfFileAbsolute()
    {
        return "/home/lars/prj/raspberrypi_pico/blinky/RP2040_blinky/blinky_pico.elf";
    }

    public String getConnectionString()
    {
        return "remote 192.168.42.1:54321";
    }

}
