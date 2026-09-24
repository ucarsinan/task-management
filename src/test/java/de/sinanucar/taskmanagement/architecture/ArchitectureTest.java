package de.sinanucar.taskmanagement.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "de.sinanucar.taskmanagement",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest
    static final ArchRule DOMAIN_IS_INDEPENDENT =
            noClasses()
                    .that()
                    .resideInAPackage("..task.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..task.web..",
                            "..task.application..",
                            "..task.persistence..",
                            "org.springframework..");

    @ArchTest
    static final ArchRule PERSISTENCE_DOES_NOT_DEPEND_ON_UPPER_LAYERS =
            noClasses()
                    .that()
                    .resideInAPackage("..task.persistence..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..task.application..", "..task.web..");

    @ArchTest
    static final ArchRule WEB_DOES_NOT_ACCESS_PERSISTENCE =
            noClasses()
                    .that()
                    .resideInAPackage("..task.web..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..task.persistence..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule APPLICATION_HAS_NO_WEB_DEPENDENCIES =
            noClasses()
                    .that()
                    .resideInAPackage("..task.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..task.web..",
                            "jakarta.servlet..",
                            "org.springframework.web..",
                            "org.springframework.ui..");

    @ArchTest
    static final ArchRule TASK_PACKAGES_HAVE_NO_CYCLES =
            slices().matching("de.sinanucar.taskmanagement.task.(*)..").should().beFreeOfCycles();
}
