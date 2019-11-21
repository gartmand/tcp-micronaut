package dev.gartman.controller;

import dev.gartman.model.Skill;
import dev.gartman.persistence.SkillRepository;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

@Controller("/skills")
public class SkillController {

  private SkillRepository repository;

  public SkillController(SkillRepository repository) {
    this.repository = repository;
  }

  @Get
  public Flowable<Skill> getAll() {
    return repository.findAll();
  }

  @Get("/{id}")
  public Maybe<Skill> getOne(Long id) {
    return repository.findById(id);
  }

  @Post
  public Single<Skill> insertOne(@NotNull @Body Skill skill) {
    if (skill.getId() != null) {
      throw new ConstraintViolationException(
          "Attempt to POST with ID set. Did you mean to PUT?", Collections.emptySet());
    }

    return repository.save(skill);
  }

  @Put("/{id}")
  public Single<Skill> replaceOne(@PathVariable("id") Long id, @NotNull @Body Skill skill) {
    return repository
        .findById(id)
        .map(
            r -> {
              r.setId(id);
              return r;
            })
        .flatMapSingle(repository::save);
  }

  @Delete("/{id}")
  public Maybe<Skill> deleteOne(Long id) {
    return repository.findById(id).flatMap(c -> repository.delete(c).andThen(Maybe.just(c)));
  }
}
