package com.rex.linebotdemo.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RagServiceTest {

    @Test
    void shouldLoadKnowledgeAndRetrieveContext() {
        RagProperties props = new RagProperties();
        props.setEnabled(true);
        props.setKnowledgeClasspathDir("knowledge");
        props.setChunkSize(500);
        props.setChunkOverlap(50);
        props.setTopK(3);
        props.setMinScore(0.01);
        props.setMaxContextChars(2000);

        RagService service = new RagService(props);
        service.init();

        assertTrue(service.chunkCount() > 0, "should load at least one chunk from classpath knowledge");

        String ctx = service.buildContext("RAG 是什麼");
        assertNotNull(ctx);
        assertFalse(ctx.isBlank(), "context should not be blank when query matches knowledge base");
        assertTrue(ctx.contains("RAG"), "context should contain term RAG");
    }

    @Test
    void shouldRetrieveChineseQueryYanYaDate() {
        RagProperties props = new RagProperties();
        props.setEnabled(true);
        props.setKnowledgeClasspathDir("knowledge");
        props.setChunkSize(800);
        props.setChunkOverlap(120);
        props.setTopK(5);
        props.setMinScore(0.01);
        props.setMaxContextChars(4000);

        RagService service = new RagService(props);
        service.init();

        String ctx = service.buildContext("尾牙日期");
        assertNotNull(ctx);
        assertFalse(ctx.isBlank(), "context should not be blank for chinese query '尾牙日期'");
        assertTrue(ctx.contains("2026-01-21"), "context should include the dinner date from tist.md");
    }
}
