package app.jer.axon;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Utils {
    public static MutableText prefixText(String prefix) {
        return Text.empty().append(Text.literal("[Axon AI] ").formatted(Formatting.GRAY))
                .append(Text.literal(prefix + ": ").formatted(Formatting.AQUA));
    }
}
