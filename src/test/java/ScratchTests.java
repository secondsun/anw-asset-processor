import dev.secondsun.games.aworld.BitplaneAdjuster;
import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.MemEntry;
import org.junit.jupiter.api.Test;

public class ScratchTests {

    @Test
    public void test() {
        var result = new ResourceReader().readAllResources();
        var data = result.resourcesBin();
        var memEntryList = result.memList();

        for (int[] memlistPart : ResourceReader.MEM_LIST_PARTS) {
            var paletteEntry = memEntryList.get(memlistPart[0]);
            System.out.println(paletteEntry.size/32);
        }

        var adjuster = new BitplaneAdjuster();
        var videoMemory = adjuster.convertFromAmigaBitplaneToIndexedBitmap(memEntryList.get(18).bufPtr, data);
        //var palette = adjuster.createPalette(memEntry.get(0x17).bufPtr, data);



    }




}
