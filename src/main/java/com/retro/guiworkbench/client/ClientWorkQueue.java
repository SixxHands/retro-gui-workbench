package com.retro.guiworkbench.client;

import java.util.Optional;

// Commands run on the server thread, but exporting needs the client resource manager. Drop flags here and let the next client tick pick them up.
public final class ClientWorkQueue
{
    private volatile boolean _exportAllRequested;
    private volatile boolean _exportOpenedRequested;
    private volatile Boolean _loggingStateRequested;
    private volatile boolean _loggingToggleRequested;

    ClientWorkQueue()
    {
    }

    public void requestExportAll()
    {
        _exportAllRequested = true;
    }

    public void requestExportOpened()
    {
        _exportOpenedRequested = true;
    }

    public void requestLoggingState(boolean enabled)
    {
        _loggingStateRequested = enabled;
    }

    public void requestLoggingToggle()
    {
        _loggingToggleRequested = true;
    }

    public boolean consumeExportAll()
    {
        boolean requested = _exportAllRequested;
        _exportAllRequested = false;
        return requested;
    }

    public boolean consumeExportOpened()
    {
        boolean requested = _exportOpenedRequested;
        _exportOpenedRequested = false;
        return requested;
    }

    public Optional<Boolean> consumeRequestedLoggingState()
    {
        Boolean requested = _loggingStateRequested;
        _loggingStateRequested = null;
        return Optional.ofNullable(requested);
    }

    public boolean consumeLoggingToggle()
    {
        boolean requested = _loggingToggleRequested;
        _loggingToggleRequested = false;
        return requested;
    }
}
