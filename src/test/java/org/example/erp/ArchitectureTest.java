package org.example.erp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.example.erp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    private static final List<String> MODULES = List.of("identity", "partners", "accounting", "inventory", "sales", "purchasing", "audit");

    private static String pkg(String module, String layer) {
        return "org.example.erp.modules." + module + "." + layer + "..";
    }

    @ArchTest
    void moduleBoundaries(JavaClasses classes) {
        for (String module : MODULES) {
            String[] others = MODULES.stream().filter(m -> !m.equals(module))
                    .map(m -> "org.example.erp.modules." + m + "..").toArray(String[]::new);
            noClasses().that().resideInAPackage("org.example.erp.modules." + module + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(others)
                    .because(module + " must not depend on other modules").check(classes);
            noClasses().that().resideInAPackage("org.example.erp.modules." + module + "..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.example.erp.composition..", "org.example.erp.shell..")
                    .check(classes);

            noClasses().that().resideInAPackage(pkg(module, "domain")).should().dependOnClassesThat()
                    .resideInAnyPackage(pkg(module, "application"), pkg(module, "infrastructure"), pkg(module, "presentation"),
                            "org.springframework..", "jakarta.persistence..", "com.fasterxml..")
                    .because("the domain is pure business code").check(classes);

            noClasses().that().resideInAPackage(pkg(module, "application")).should().dependOnClassesThat()
                    .resideInAnyPackage(pkg(module, "infrastructure"), pkg(module, "presentation"), "org.springframework..", "jakarta.persistence..")
                    .because("application code is framework independent and depends inward").check(classes);

            noClasses().that().resideInAPackage(pkg(module, "infrastructure")).should().dependOnClassesThat()
                    .resideInAPackage(pkg(module, "presentation")).check(classes);

            noClasses().that().resideInAPackage(pkg(module, "presentation")).should().dependOnClassesThat()
                    .resideInAnyPackage(pkg(module, "domain"), pkg(module, "infrastructure"))
                    .because("controllers talk to application use cases and DTOs only").check(classes);
        }
    }
}
