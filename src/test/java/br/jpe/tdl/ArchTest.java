package br.jpe.tdl;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("br.jpe.tdl");

        noClasses()
            .that()
            .resideInAnyPackage("br.jpe.tdl.service..")
            .or()
            .resideInAnyPackage("br.jpe.tdl.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..br.jpe.tdl.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
