package dev.gartman.controller;

import dev.gartman.model.SkillCategory;
import dev.gartman.persistence.SkillCategoryRepository;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

@Controller("/categories")
public class SkillCategoryController {

  private SkillCategoryRepository repository;

  public SkillCategoryController(SkillCategoryRepository repository) {
    this.repository = repository;
  }

  @Get
  public Flowable<SkillCategory> getAll() {
    return repository.findAll();
  }

  @Get("/{id}")
  public Maybe<SkillCategory> getOne(Long id) {
    return repository.findById(id);
  }

  @Post
  public Single<SkillCategory> insertOne(@NotNull @Body SkillCategory category) {
    if (category.getId() != null) {
      throw new ConstraintViolationException(
          "Attempt to POST with ID set. Did you mean to PUT?", Collections.emptySet());
    }

    return repository.save(category);
  }

  @Put("/{id}")
  public Single<SkillCategory> replaceOne(
      @PathVariable("id") Long id, @NotNull @Body SkillCategory category) {
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
  public Maybe<SkillCategory> deleteOne(Long id) {
    return repository.findById(id).flatMap(c -> repository.delete(c).andThen(Maybe.just(c)));
  }
}
