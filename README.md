# It is still in development

## android-email
Android library for sending emails.

## Usage

1) Compose email :

    public interface EmailServiceTest {

       @To("jokatavr@gmail.com")
       @Cc("{cc}")
       @Subject("Android Test")
       @Email("Hello {username}. This is sparta.")
       void spartaEmail(@Param("username") final String username, @Param("cc") final String cc, final Callback callback);
    }
    
2) Send email :
    
    test.spartaEmail("Georgi", "opelastra100@gmail.com", new CallbackImpl());
    
3) Result :

![email](https://github.com/geftimov/android-email/blob/master/art/email.png)



## Licence

    Copyright 2015 Georgi Eftimov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
