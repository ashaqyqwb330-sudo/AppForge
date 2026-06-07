package com.appforge

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testTemplateFromId() {
        val template = com.appforge.domain.model.Template.fromId(1)
        assertEquals(com.appforge.domain.model.Template.ELEGANT_GALLERY, template)
    }
}
