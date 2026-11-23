package com.retro.guiworkbench.client;

public final class ClientWorkQueueFactory
{
    private ClientWorkQueueFactory()
    {
    }

    public static ClientWorkQueue create()
    {
        return new ClientWorkQueue();
    }
}
