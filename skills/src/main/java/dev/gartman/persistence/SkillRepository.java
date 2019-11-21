package dev.gartman.persistence;

import dev.gartman.model.Skill;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.RxJavaCrudRepository;

@Repository
public interface SkillRepository extends RxJavaCrudRepository<Skill, Long> {}
