/*
Copyright 2017 José Luis De la Cruz Morales joseluis.delacruz@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.jos.dem.vetlog.validator

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.validation.Validator
import org.springframework.validation.Errors
import org.springframework.stereotype.Component
import com.jos.dem.vetlog.service.LocaleService
import com.jos.dem.vetlog.command.ChangePasswordCommand

@Component
class ChangePasswordValidator implements Validator {

  @Autowired
  LocaleService localeService

  @Override
  boolean supports(Class<?> clazz) {
    ChangePasswordCommand.class.equals(clazz)
  }

  @Override
  void validate(Object target, Errors errors) {
    ChangePasswordCommand command = (ChangePasswordCommand) target
    validatePasswords(errors, command)
  }

  def validatePasswords(Errors errors, ChangePasswordCommand command) {
    if (!command.password.equals(command.passwordConfirmation)){
      errors.rejectValue('password', 'error.password', localeService.getMessage('user.validation.password.equals'))
    }
  }

}
