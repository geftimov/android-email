package com.eftimoff.androidemail;

public interface Callback {

    public void success();

    public void failure(final EmailError emailError);
}
