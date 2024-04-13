package org.example;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.*;

public class JsonParserTest {

    private JsonParser parser;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        parser = new JsonParser();
    }

    @Test(groups = "basicTests")
    public void parseValidJsonObject() throws JsonParser.ParseException {
        String json = "{\"name\":\"John\",\"age\":30, \"isMarried\":false, \"children\":[\"Anna\", \"Mike\"]}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals("John", resultMap.get("name"));
        Assert.assertEquals(new BigDecimal("30"), resultMap.get("age"));
        Assert.assertEquals(false, resultMap.get("isMarried"));
        Assert.assertTrue(Arrays.asList("Anna", "Mike").containsAll((Collection<?>) resultMap.get("children")));
    }

    @Test(groups = "basicTests")
    public void parseValidJsonArray() throws JsonParser.ParseException {
        String json = "[{\"fruit\":\"apple\"}, {\"fruit\":\"banana\"}]";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof List);

        List<Map<String, String>> resultList = (List<Map<String, String>>) result;
        Assert.assertEquals(2, resultList.size());
        Assert.assertTrue(resultList.stream().anyMatch(map -> "apple".equals(map.get("fruit"))));
        Assert.assertTrue(resultList.stream().anyMatch(map -> "banana".equals(map.get("fruit"))));
    }

    @Test(groups = "exceptionTests", expectedExceptions = JsonParser.ParseException.class)
    public void parseThrowsParseException() throws JsonParser.ParseException {
        String json = "{\"name\":\"John\"";
        parser.parse(json);
    }

    @DataProvider(name = "provideStringsForEscapedCharacters")
    public Object[][] provideStringsForEscapedCharacters() {
        return new Object[][]{
                {"\"string with \\\"quotes\\\"\"", "string with \"quotes\""},
                {"\"string with \\\\ backslashes\"", "string with \\ backslashes"}
        };
    }

    @Test(groups = "basicTests", dataProvider = "provideStringsForEscapedCharacters")
    public void parseStringsWithEscapedCharacters(String json, String expected) throws JsonParser.ParseException {
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(expected, result);
    }

    @Test(groups = "basicTests")
    public void prettyPrint() throws JsonParser.ParseException {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("name", "John");
        jsonMap.put("age", 30);
        jsonMap.put("phoneNumbers", Arrays.asList("123-456-7890", "456-789-0123"));

        String pretty = parser.prettyPrint(jsonMap);
        Assert.assertTrue(pretty.contains("name=John"));
        Assert.assertTrue(pretty.contains("age=30"));
        Assert.assertTrue(pretty.contains("phoneNumbers=[123-456-7890, 456-789-0123]"));
    }

    @Test(groups = "exceptionTests", expectedExceptions = JsonParser.ParseException.class)
    public void parseInvalidJsonThrowsParseException() throws JsonParser.ParseException {
        String json = "{ name: \"John\", age: 30 }";
        parser.parse(json);
    }

    @Test(groups = "basicTests")
    public void parseComplexJson() throws JsonParser.ParseException {
        String json = "{\"person\":{\"name\":\"John\",\"age\":30,\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}}}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> personMap = (Map<?, ?>) ((Map<?, ?>) result).get("person");
        Assert.assertNotNull(personMap.get("name"));
        Assert.assertNotNull(personMap.get("age"));
        Assert.assertNotNull(personMap.get("address"));

        Map<?, ?> addressMap = (Map<?, ?>) personMap.get("address");
        Assert.assertEquals("123 Main St", addressMap.get("street"));
        Assert.assertEquals("Anytown", addressMap.get("city"));
    }

    @Test(groups = "basicTests")
    public void parseNullValue() throws JsonParser.ParseException {
        String json = "{\"key\":null}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Assert.assertNull(resultMap.get("key"));
    }

    @Test(groups = "basicTests")
    public void parseBooleanValues() throws JsonParser.ParseException {
        String json = "{\"trueValue\":true, \"falseValue\":false}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Assert.assertTrue((Boolean) resultMap.get("trueValue"));
        Assert.assertFalse((Boolean) resultMap.get("falseValue"));
    }

    @Test(groups = "basicTests")
    public void parseNumbers() throws JsonParser.ParseException {
        String json = "{\"integer\":42, \"floatingPoint\":3.14}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Assert.assertEquals(new BigDecimal("42"), resultMap.get("integer"));
        Assert.assertEquals(new BigDecimal("3.14"), resultMap.get("floatingPoint"));
    }

    @Test(groups = "exceptionTests", expectedExceptions = JsonParser.ParseException.class)
    public void parseMalformedJsonThrowsParseException() throws JsonParser.ParseException {
        String json = "{\"key\":42,";
        parser.parse(json);
    }

    @Test(groups = "basicTests")
    public void parseEmptyJsonObject() throws JsonParser.ParseException {
        String json = "{}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);
        Assert.assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test(groups = "basicTests")
    public void parseEmptyJsonArray() throws JsonParser.ParseException {
        String json = "[]";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof List);
        Assert.assertTrue(((List<?>) result).isEmpty());
    }

    @Test(groups = "basicTests")
    public void parseNestedArrays() throws JsonParser.ParseException {
        String json = "[[1, 2], [3, 4]]";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof List);

        List<?> resultList = (List<?>) result;
        Assert.assertEquals(2, resultList.size());
        Assert.assertEquals(Arrays.asList(new BigDecimal("1"), new BigDecimal("2")), resultList.get(0));
        Assert.assertEquals(Arrays.asList(new BigDecimal("3"), new BigDecimal("4")), resultList.get(1));
    }

    @Test(groups = "basicTests")
    public void parseDifferentNumberTypes() throws JsonParser.ParseException {
        String json = "{\"longNumber\": 9223372036854775807, \"integerNumber\": 2147483647}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Assert.assertEquals(new BigDecimal("9223372036854775807"), resultMap.get("longNumber"));
        Assert.assertEquals(new BigDecimal("2147483647"), resultMap.get("integerNumber"));
    }

    @Test(groups = "basicTests")
    public void parseSpecialFloatingPointValues() throws JsonParser.ParseException {
        String json = "{\"nan\": \"NaN\", \"infinity\": \"Infinity\"}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Assert.assertEquals("NaN", resultMap.get("nan"));
        Assert.assertEquals("Infinity", resultMap.get("infinity"));
    }

    @Test(groups = "exceptionTests", expectedExceptions = JsonParser.ParseException.class)
    public void unexpectedEndOfJson() throws JsonParser.ParseException {
        String json = "{\"key\": 42";
        parser.parse(json);
    }

    @Test(groups = "basicTests")
    public void parseDeeplyNestedJson() throws JsonParser.ParseException {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"deep\"}}}}}}";
        Object result = parser.parse(json);
        Assert.assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        Map<?, ?> aMap = (Map<?, ?>) resultMap.get("a");
        Map<?, ?> bMap = (Map<?, ?>) aMap.get("b");
        Map<?, ?> cMap = (Map<?, ?>) bMap.get("c");
        Map<?, ?> dMap = (Map<?, ?>) cMap.get("d");
        Map<?, ?> eMap = (Map<?, ?>) dMap.get("e");
        Assert.assertEquals("deep", eMap.get("f"));
    }
}
