package com.danhuy.common_service.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

  private List<String> enumValues;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    enumValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants()).map(Enum::name)
        .toList();
  }

  @Override
  public boolean isValid(CharSequence charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null) {
      return true;
    }
    
    return enumValues.contains(charSequence.toString().toUpperCase());
  }
}
