package com.eftimoff.androidemail;

import com.eftimoff.androidemail.annotations.Cc;
import com.eftimoff.androidemail.annotations.Email;
import com.eftimoff.androidemail.annotations.Param;
import com.eftimoff.androidemail.annotations.Subject;
import com.eftimoff.androidemail.annotations.To;

public interface EmailServiceTest {

    @To("jokatavr@gmail.com")
    @Cc("{cc}")
    @Subject("Android Test")
    @Email("Hello {username}. This is sparta.")
    void spartaEmail(@Param("username") final String username, @Param("cc") final String cc, final Callback callback);
}
