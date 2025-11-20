import dev.secondsun.games.aworld.ResourceReader;
import org.junit.jupiter.api.Test;

public class ScratchTests {

    @Test
    public void test() {
        var result = new ResourceReader().readAllResources();
        for (int i = 0; i < result.resourcesBin().length; i++) {
            if (i > 0 && i % 16 == 0) {
                System.out.println();
            }
            if (result.resourcesBin()[i] > 255) {
                throw new RuntimeException("Value over 255");
            } else {
                System.out.printf("%4d ", result.resourcesBin()[i]);
            }
        }
        System.out.println();
    }
}
