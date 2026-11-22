package dev.damda;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.damda", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule layersFollowDependencyDirection = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Api")
            .definedBy("..api..")
            .layer("Application")
            .definedBy("..application..")
            .layer("Domain")
            .definedBy("..domain..")
            .layer("Infrastructure")
            .definedBy("..infrastructure..")
            .whereLayer("Api")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Api")
            .whereLayer("Infrastructure")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Api", "Application", "Infrastructure")
            .because("ADR-0004 결정 2: 바깥 층만 안쪽을 부른다");

    @ArchTest
    static final ArchRule apiDoesNotUseRepository = noClasses()
            .that()
            .resideInAPackage("..api..")
            .should()
            .dependOnClassesThat()
            .haveSimpleNameEndingWith("Repository")
            .because("컨트롤러는 서비스를 거쳐 저장소를 쓴다");

    @ArchTest
    static final ArchRule domainUsesOnlyGlobalError = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat(
                    resideInAPackage("dev.damda.global..").and(resideOutsideOfPackage("dev.damda.global.error..")))
            .because("2-1: domain은 에러 코드 외의 공통 설정을 모른다");

    @ArchTest
    static final ArchRule featuresAreFreeOfCycles =
            slices().matching("dev.damda.(*)..").should().beFreeOfCycles().because("기능끼리 순환이 생기면 한 기능만 떼어 고칠 수 없다");
}
