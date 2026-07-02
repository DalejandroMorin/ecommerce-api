# Delta for JPA Entities

## ADDED Requirements

### Requirement: REQ-JPA-001 — Safe toString()

Every JPA entity MUST replace `@Data` with `@Getter @Setter @ToString`. All LAZY relationships (`@ManyToOne`, `@OneToOne`, `@OneToMany`) MUST have `@ToString.Exclude` to prevent recursive `toString()` that causes `StackOverflowError`.

#### Scenario: toString_on_bidirectional_relation_does_not_stack_overflow

- GIVEN a JPA entity with a bidirectional LAZY relationship
- WHEN `toString()` is called on either side
- THEN the LAZY-related fields are excluded from output
- AND no `StackOverflowError` or `LazyInitializationException` is thrown

#### Scenario: toString_excludes_all_lazy_relations

- GIVEN any JPA entity with LAZY-loaded relationships
- WHEN `toString()` is invoked
- THEN every `@ManyToOne`, `@OneToOne`, and `@OneToMany` field is omitted

### Requirement: REQ-JPA-002 — Optimistic Locking

Every JPA entity MUST declare `@Version private Long version;` to enable optimistic locking and prevent lost updates under concurrent writes.

#### Scenario: entity_has_version_field

- GIVEN any JPA entity class
- WHEN inspecting persistent fields
- THEN it MUST include `@Version private Long version;`

#### Scenario: concurrent_update_triggers_optimistic_lock_exception

- GIVEN two concurrent transactions reading the same entity
- WHEN both modify and flush
- THEN the second flush MUST throw `OptimisticLockException`

### Requirement: REQ-JPA-003 — Stable equals/hashCode

Entities MUST NOT base `equals()` / `hashCode()` on `id` (null before persist, changes after). They MUST use a stable business key: `nombre` for products, `email` for users. Relations and `version` MUST have `@EqualsAndHashCode.Exclude`.

#### Scenario: equals_based_on_business_key_not_id

- GIVEN an entity before persistence (`id == null`)
- WHEN compared with another of same business key
- THEN `equals()` returns `true` regardless of `id`
- AND adding both to a `HashSet` results in one entry

#### Scenario: equals_stable_after_persist

- GIVEN a transient entity
- WHEN persisted (JPA assigns `id`)
- THEN `hashCode()` remains the same before and after persist
- AND `equals()` with another entity of same business key still returns `true`

### Requirement: REQ-JPA-004 — Safe Constructors

Entities MUST NOT use `@AllArgsConstructor` (allows constructing with pre-existing `id`, bypassing JPA identity management). Only `@NoArgsConstructor` (required by JPA) and explicit constructors that omit `id` are allowed.

#### Scenario: entity_without_all_args_constructor

- GIVEN any JPA entity class
- WHEN inspecting constructors
- THEN no constructor accepts `id` as parameter
- AND `@NoArgsConstructor` is present

#### Scenario: entity_construction_without_id

- GIVEN code creating a new entity
- WHEN calling the constructor
- THEN `id` is NOT passed as an argument
- AND `id` is `null` until JPA assigns it

### Requirement: REQ-JPA-005 — Compiler Safety

The project MUST compile and all existing tests MUST pass after entity changes, confirming zero behavioral regression.

#### Scenario: build_compiles_without_errors

- GIVEN the refactored entity classes
- WHEN `./mvnw clean compile` executes
- THEN build succeeds with zero compilation errors

#### Scenario: existing_tests_pass

- GIVEN the refactored entity classes
- WHEN `./mvnw test` executes
- THEN all existing tests pass with identical results
