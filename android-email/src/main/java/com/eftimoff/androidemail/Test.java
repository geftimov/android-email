package com.eftimoff.androidemail;

import com.eftimoff.androidemail.annotations.Email;
import com.eftimoff.androidemail.annotations.Param;
import com.eftimoff.androidemail.annotations.Sender;
import com.eftimoff.androidemail.annotations.Subject;

public interface Test {

    @Sender("jokatavr@gmail.com")
    @Subject("{subject}")
    @Email("Hello World {subject} {asds} {subject}")
    void firstMethod(@Param("subject") final String asd, @Param("asds") final String asds, final Callback callback);
}
