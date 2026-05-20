package com.company.common.result;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void okShouldReturnSuccessResult() {
        Result<String> r = Result.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
    }

    @Test
    void okShouldHandleNullData() {
        Result<Void> r = Result.ok(null);
        assertEquals(200, r.getCode());
        assertNull(r.getData());
    }

    @Test
    void failWithCodeShouldReturnErrorResult() {
        Result<Void> r = Result.fail(404, "资源不存在");
        assertEquals(404, r.getCode());
        assertEquals("资源不存在", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void failWithoutCodeShouldDefaultTo500() {
        Result<Void> r = Result.fail("服务器错误");
        assertEquals(500, r.getCode());
        assertEquals("服务器错误", r.getMessage());
    }
}

class PageResultTest {

    @Test
    void ofShouldCreatePageResult() {
        List<String> records = List.of("a", "b", "c");
        PageResult<String> pr = PageResult.of(records, 100, 2, 10);

        assertEquals(3, pr.getRecords().size());
        assertEquals("a", pr.getRecords().get(0));
        assertEquals(100, pr.getTotal());
        assertEquals(2, pr.getPage());
        assertEquals(10, pr.getSize());
    }

    @Test
    void ofShouldHandleEmptyList() {
        PageResult<Object> pr = PageResult.of(List.of(), 0, 1, 10);
        assertTrue(pr.getRecords().isEmpty());
        assertEquals(0, pr.getTotal());
    }
}
