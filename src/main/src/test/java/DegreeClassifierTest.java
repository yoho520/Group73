import org.junit.Test;
import static org.junit.Assert.*;

public class DegreeClassifierTest {

    private DegreeClassifier classifier = new DegreeClassifier();

    // 无荣誉学位划分（低于40%）
    @Test
    public void testNoHonoursBoundaryLower() {
        assertEquals("无荣誉学位", classifier.classify(0));
    }

    @Test
    public void testNoHonoursBoundaryUpper() {
        assertEquals("无荣誉学位", classifier.classify(39.99));
    }

    @Test
    public void testNoHonoursTypical() {
        assertEquals("无荣誉学位", classifier.classify(20));
    }

    // 三等荣誉学位划分（40%至49.99%）
    @Test
    public void testThirdClassBoundaryLower() {
        assertEquals("三等荣誉学位", classifier.classify(40));
    }

    @Test
    public void testThirdClassBoundaryUpper() {
        assertEquals("三等荣誉学位", classifier.classify(49.99));
    }

    @Test
    public void testThirdClassTypical() {
        assertEquals("三等荣誉学位", classifier.classify(45));
    }

    // 下二等荣誉学位(2:2)划分（50%至59.99%）
    @Test
    public void testLowerSecondClassBoundaryLower() {
        assertEquals("下二等荣誉学位(2:2)", classifier.classify(50));
    }

    @Test
    public void testLowerSecondClassBoundaryUpper() {
        assertEquals("下二等荣誉学位(2:2)", classifier.classify(59.99));
    }

    @Test
    public void testLowerSecondClassTypical() {
        assertEquals("下二等荣誉学位(2:2)", classifier.classify(55));
    }

    // 上二等荣誉学位(2:1)划分（60%至69.99%）
    @Test
    public void testUpperSecondClassBoundaryLower() {
        assertEquals("上二等荣誉学位(2:1)", classifier.classify(60));
    }

    @Test
    public void testUpperSecondClassBoundaryUpper() {
        assertEquals("上二等荣誉学位(2:1)", classifier.classify(69.99));
    }

    @Test
    public void testUpperSecondClassTypical() {
        assertEquals("上二等荣誉学位(2:1)", classifier.classify(65));
    }

    // 一等荣誉学位划分（70%及以上）
    @Test
    public void testFirstClassBoundaryLower() {
        assertEquals("一等荣誉学位", classifier.classify(70));
    }

    @Test
    public void testFirstClassBoundaryUpper() {
        assertEquals("一等荣誉学位", classifier.classify(100));
    }

    @Test
    public void testFirstClassTypical() {
        assertEquals("一等荣誉学位", classifier.classify(85));
    }

    // 无效输入
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMarks() {
        classifier.classify(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAboveMaximumMarks() {
        classifier.classify(100.01);
    }
}