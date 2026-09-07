package unq.losrecursionistas.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.model.Liga;

class LigaTest {

	@Test
	void normalizaElCodigo() {
		Liga liga = new Liga("Premier League", " premier ", true);
		assertEquals("PREMIER", liga.getCodigo());
	}
}