package com.eftimoff.androidemail;

import com.eftimoff.androidemail.annotations.Bcc;
import com.eftimoff.androidemail.annotations.Cc;
import com.eftimoff.androidemail.annotations.Email;
import com.eftimoff.androidemail.annotations.Param;
import com.eftimoff.androidemail.annotations.Subject;
import com.eftimoff.androidemail.annotations.To;

public interface EmailServiceTest {

    @To("jokatavr@gmail.com")
    @Bcc("jokatavr@gmail.com")
    @Cc("jokatavr@gmail.com")
    @Subject("Android Test")
    @Email("Hello World {subject} {asds}. This is sparta.")
    void spartaEmail(@Param("subject") final String asd, @Param("asds") final String asds, final Callback callback);
}
