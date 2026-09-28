package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.impl.LigaServiceImpl;


@ExtendWith(MockitoExtension.class)
public class LigaServiceTest {

    @Mock
    private RepositorioLiga repositorioLiga;

    @InjectMocks
    private LigaServiceImpl ligaService;

    @BeforeEach
    void setUp() {
        //TODO: hacerlo

    }
}
