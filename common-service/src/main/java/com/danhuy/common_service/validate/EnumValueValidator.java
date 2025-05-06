package com.danhuy.common_service.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

  private String fieldName;
  private List<String> enumValues;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    this.fieldName = constraintAnnotation.name();
    this.enumValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants()).map(Enum::name)
        .toList();
  }

  @Override
  public boolean isValid(CharSequence charSequence,
      ConstraintValidatorContext constraintValidatorContext) {
    if (charSequence == null) {
      return true;
    }

    boolean isValid = enumValues.contains(charSequence.toString());
    if (!isValid) {
      String enumValuesDisplay = String.join(", ", enumValues);

      // custom message
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext.buildConstraintViolationWithTemplate(
          String.format("%s must be one of the following values: [%s]", fieldName,
              enumValuesDisplay)
      ).addConstraintViolation();
    }

    return isValid;
  }
}
