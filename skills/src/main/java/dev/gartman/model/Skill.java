package dev.gartman.model;

import io.micronaut.data.annotation.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@MappedEntity
public class Skill {

  @Id @AutoPopulated private Long id;

  @NotEmpty private String name;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  @NotNull
  private SkillCategory category;
}
