package net.glasslauncher.mods.glassinventorytweaks.impl;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * A basic OutputStream wrapper that can be easily reset and reused for tracking written data sizes.
 */
public class TrackingOutputStream extends DataOutputStream {

    public TrackingOutputStream() {
        super(null);
    }

    public void reset(OutputStream out) {
        written = 0;
        this.out = out;
    }
}
