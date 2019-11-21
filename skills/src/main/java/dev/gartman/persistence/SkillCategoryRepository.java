package dev.gartman.persistence;

import dev.gartman.model.SkillCategory;
import io.micronaut.data.repository.reactive.RxJavaCrudRepository;

public interface SkillCategoryRepository extends RxJavaCrudRepository<SkillCategory, Long> {}
