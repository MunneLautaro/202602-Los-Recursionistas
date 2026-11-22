package unq.losrecursionistas.backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Reglas de arquitectura por capas del backend.
 *
 * <pre>
 *   Controller ─┐
 *   Scheduler  ─┼─► Service ─┬─► Persistence ─► Model
 *   Security   ─┘            └─► Adapter
 *   Configuration (puede acceder a cualquier capa para cablear la aplicación)
 * </pre>
 *
 * {@code model} y {@code exceptions} son transversales: cualquier capa puede usarlos.
 */
@AnalyzeClasses(packages = "unq.losrecursionistas.backend",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaCapasTest {

    private static final String CONTROLLER = "..backend.controller..";
    private static final String SERVICE = "..backend.service..";
    private static final String SERVICE_IMPL = "..backend.service.impl..";
    private static final String PERSISTENCE = "..backend.persistence..";
    private static final String DAO = "..backend.persistence.sql..";
    private static final String MODEL = "..backend.model..";
    private static final String ADAPTER = "..backend.adapter..";
    private static final String SCHEDULER = "..backend.scheduler..";
    private static final String SECURITY = "..backend.security..";
    private static final String CONFIGURATION = "..backend.configuration..";

    @ArchTest
    static final ArchRule capasRespetanSusDependencias = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy(CONTROLLER)
            .layer("Service").definedBy(SERVICE)
            .layer("Persistence").definedBy(PERSISTENCE)
            .layer("Model").definedBy(MODEL)
            .layer("Adapter").definedBy(ADAPTER)
            .layer("Scheduler").definedBy(SCHEDULER)
            .layer("Security").definedBy(SECURITY)
            .layer("Configuration").definedBy(CONFIGURATION)

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Scheduler").mayNotBeAccessedByAnyLayer()
            .whereLayer("Configuration").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers(
                    "Controller", "Scheduler", "Security", "Configuration")
            .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Configuration")
            .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Service", "Configuration")
            .whereLayer("Security").mayOnlyBeAccessedByLayers("Configuration")
            .because("cada capa solo debe depender de las capas inferiores a través de sus interfaces");

    @ArchTest
    static final ArchRule soloElServiceConoceSusImplementaciones = noClasses()
            .that().resideOutsideOfPackage(SERVICE)
            .should().dependOnClassesThat().resideInAPackage(SERVICE_IMPL)
            .because("las demás capas deben depender de service.interfaces, no de las implementaciones");

    @ArchTest
    static final ArchRule losDaosSoloSeUsanDesdePersistencia = classes()
            .that().resideInAPackage(DAO)
            .should().onlyBeAccessed().byAnyPackage(PERSISTENCE)
            .because("los services acceden a datos mediante repositorios, no mediante los DAOs de Spring Data");

    @ArchTest
    static final ArchRule elModeloNoDependeDeOtrasCapas = noClasses()
            .that().resideInAPackage(MODEL)
            .should().dependOnClassesThat().resideInAnyPackage(
                    CONTROLLER, SERVICE, PERSISTENCE, ADAPTER, SCHEDULER, SECURITY, CONFIGURATION)
            .because("el dominio es el núcleo y no conoce a las capas que lo usan");

    @ArchTest
    static final ArchRule losControllersEstanEnSuCapa = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage(CONTROLLER)
            .andShould().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule lasEntidadesEstanEnElModelo = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage(MODEL);

    @ArchTest
    static final ArchRule losRepositoriosDeSpringDataEstanEnPersistencia = classes()
            .that().areAssignableTo(Repository.class)
            .should().resideInAPackage(DAO);

    @ArchTest
    static final ArchRule noHayCiclosEntrePaquetes = slices()
            .matching("unq.losrecursionistas.backend.(*)..")
            .should().beFreeOfCycles();
}
